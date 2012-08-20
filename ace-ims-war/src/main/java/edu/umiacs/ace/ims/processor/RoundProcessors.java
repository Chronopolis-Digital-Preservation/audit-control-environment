/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.ims.IMSContext;
import edu.umiacs.ace.ims.store.TokenStore;
import edu.umiacs.ace.ims.store.TokenStoreLocal;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.server.ServiceLocator;
import edu.umiacs.ace.server.exception.ServerBusyException;
import edu.umiacs.ace.server.exception.ServiceUnavailableException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.log4j.Logger;

/**
 *
 * @author mmcgann
 */
public final class RoundProcessors
{
    private static final IMSContext context = IMSContext.getInstance();
    
    private static RoundProcessors instance = null;
    private static final Logger print = Logger.getLogger(RoundProcessors.class);
    private Map<TokenClass, RoundProcessor> processors =
            new HashMap<TokenClass, RoundProcessor>();
    private ReadWriteLock processorsLock = new ReentrantReadWriteLock();
    private TokenStoreLocal tokenStoreBean = 
            ServiceLocator.getInstance().getLocal(TokenStoreLocal.class);
    private TokenPersistenceThread persistenceThread;
    private RequestPermits requestPermits;
    
    private RoundProcessors()
    {
        requestPermits = new RequestPermits(context.getMaximumQueuedRequests());
        persistenceThread = new TokenPersistenceThread(requestPermits);
        persistenceThread.start();
    }

    public static RoundProcessors getInstance()
    {
        synchronized ( RoundProcessors.class )
        {
            if ( instance == null )
            {
                instance = new RoundProcessors();
            }
        }
        return instance;
    }

    public void installProcessor(TokenClass tokenClass)
    {
        processorsLock.writeLock().lock();
        try
        {
            String tokenClassName = tokenClass.getName();
            RoundProcessor oldProcessor = processors.get(tokenClass);
            if ( oldProcessor != null )
            {
                oldProcessor.shutdown();
                print.info("Replacing processor: " + tokenClassName);
            }
            else
            {
                print.info("Installing processor: " + tokenClassName);
            }
            RoundProcessor newProcessor = new RoundProcessor(tokenClass);
            processors.put(tokenClass, newProcessor);
            newProcessor.start();
        }
        finally
        {
            processorsLock.writeLock().unlock();
        }
    }

    public void shutdown() throws InterruptedException
    {
        print.info("Processor shutdown starting");
        
        for ( RoundProcessor processor : processors.values() )
        {
            print.info("Sending shutdown to processor: " +
                    processor.getTokenClassName());
            processor.shutdown();
        }
        for ( RoundProcessor processor : processors.values() )
        {
            print.debug("Waiting on processor: " +
                    processor.getTokenClassName());
            processor.join(context.getThreadShutdownTimeout());
        }
        print.info("Shutting down persistence thread");
        persistenceThread.shutdown();
        persistenceThread.join(context.getThreadShutdownTimeout());

        print.info("Processor shutdown complete");
    }
    
    private RoundProcessor getProcessor(TokenClass tokenClass)
    {
        RoundProcessor processor = processors.get(tokenClass);
        if ( processor == null )
        {
            throw new ServiceUnavailableException(
                    StatusCode.TOKEN_PROCESSOR_NOT_RUNNING);
        }
        return processor;
    }


    /**
     * Client requested linking
     * 
     * @param clientId
     * @param tokenClass
     * @param hashValue
     * @return
     */
    public LinkResponse requestLink(String clientId, TokenClass tokenClass, 
            String hashValue)
    {
        processorsLock.readLock().lock();
        try
        {
            RoundProcessor processor = getProcessor(tokenClass);
            return processor.link(clientId, hashValue);
        }
        finally
        {
            processorsLock.readLock().unlock();
        }
    }
    
    /**
     * Attempt to get permits for requested number of permits. Will wait for
     * configurable amount of time if permits are not available for round to run
     * and free up slots.
     * 
     * All or nothing for number of permits allocated, no partial allocation
     * 
     * @param numPermits
     */
    private void acquirePermits(int numPermits)
    {
        try
        {
            if ( !requestPermits.acquire(numPermits, 
                    context.getRequestPermitTimeout(), TimeUnit.MILLISECONDS) )
            {
                throw new ServerBusyException();
            }
        }
        catch ( InterruptedException ie )
        {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException(
                    StatusCode.TOKEN_SERVICE_UNAVAILABLE);
        }
    }
    
    /**
     * For each request, 
     *  1. create work unit, w/ response destination of persistenceThread
     *  
     * @param clientId
     * @param tokenClass
     * @param requests
     * @return
     */
    public TokenReceipt requestTokensAsync(String clientId,
            TokenClass tokenClass, List<TokenRequest> requests)
    {
        TokenReceipt receipt = null;
        
        processorsLock.readLock().lock();
        acquirePermits(requests.size());
        try
        {
            RoundProcessor processor = getProcessor(tokenClass);              
            TokenStore tokenStore = tokenStoreBean.createTokenStore(
                    requests.size());
            for ( TokenRequest request: requests )
            {
                WorkUnit unit = new WorkUnit();
                unit.setClientId(clientId);
                unit.setTokenRequest(request);
                unit.setResponseDestination(persistenceThread);
                unit.setTokenStore(tokenStore);
                processor.addWorkUnit(unit);

                Date now = new Date();
                Date readyAt = processor.getNextRun();
                if ( readyAt == null )
                {
                    readyAt = now;
                }
                receipt = new TokenReceipt();
                receipt.setRequestNumber(tokenStore.getId());
                receipt.setSessionKey(tokenStore.getSessionKey());
                receipt.setNow(now);
                receipt.setReadyAt(readyAt);
            }
            if ( tokenClass.getRoundLength() == 0 )
            {
                processor.processNow();
            }
        }
        finally
        {
            processorsLock.readLock().unlock();
        }
        return receipt;
    }
    
    /**
     * work unit created w/ local latch response queue as resp destination
     * will block until all tokens have been issued.
     * 
     * @param clientId
     * @param tokenClass
     * @param requests
     * @return
     */
    public List<TokenResponse> requestTokensImmediate(String clientId, 
            TokenClass tokenClass, List<TokenRequest> requests)
    {

        LatchResponseQueue responses = null;
        
        processorsLock.readLock().lock();
        acquirePermits(requests.size());
        try
        {
            try
            {
                RoundProcessor processor = getProcessor(tokenClass);  
                responses = new LatchResponseQueue(requests.size());
                for ( TokenRequest request: requests )
                {
                    WorkUnit workUnit = new WorkUnit();
                    workUnit.setClientId(clientId);
                    workUnit.setTokenRequest(request);
                    workUnit.setResponseDestination(responses);
                    processor.addWorkUnit(workUnit);
                }
                if ( tokenClass.getRoundLength() == 0 )
                {
                    processor.processNow();
                }
            }
            finally
            {
                processorsLock.readLock().unlock();
            }
       
            try
            {
                List<TokenResponse> results = responses.getResponses(
                        tokenClass.getRequestTimeout(), 
                        TimeUnit.MILLISECONDS); 
                if ( results == null )
                {
                    throw new ServiceUnavailableException(
                            StatusCode.TOKEN_REQUEST_TIMEOUT);
                }
                return results;
           }
           catch ( InterruptedException ie )
           {
                Thread.currentThread().interrupt();
                throw new ServiceUnavailableException(
                        StatusCode.TOKEN_SERVICE_UNAVAILABLE);
           }
        }
        finally
        {
            requestPermits.release(requests.size());
        }
    }

}

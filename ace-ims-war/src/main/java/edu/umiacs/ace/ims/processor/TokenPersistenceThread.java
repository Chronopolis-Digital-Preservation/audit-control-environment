/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.ims.IMSContext;
import edu.umiacs.ace.ims.Log;
import edu.umiacs.ace.ims.store.IssuedToken;
import edu.umiacs.ace.ims.store.TokenStore;
import edu.umiacs.ace.ims.store.TokenStoreLocal;
import edu.umiacs.ace.server.ServiceLocator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 *
 * @author mmcgann
 */
public class TokenPersistenceThread extends Thread 
        implements ResponseDestination
{
    private int batchSize;
    
    private TokenStoreLocal tokenStoreBean = 
            ServiceLocator.getInstance().getLocal(TokenStoreLocal.class);
    private LinkedBlockingDeque<WorkUnit> queue = 
            new LinkedBlockingDeque<WorkUnit>();
    private static final Logger print = 
            Logger.getLogger(TokenPersistenceThread.class);
    private RequestPermits requestPermits;
    private volatile boolean shutdownRequested = false;
    private boolean exceptionOnLastPersist = false;
    
    public TokenPersistenceThread(RequestPermits requestPermits)
    {
        super("Token Persistence");
        
        this.requestPermits = requestPermits;
        this.batchSize = IMSContext.getInstance()
                .getTokenPersistenceBatchSize();
    }
    
    public void enqueue(WorkUnit workUnit)
    {
        queue.add(workUnit);
    }
    
    public void shutdown()
    {
        shutdownRequested = true;
        this.interrupt();
    }
    
    @Override
    public void run()
    {
        NDC.push("persist: ");
        Log.system("Thread started, batch size: " + batchSize);
        boolean done = false;
        
        try
        {
            while ( !done )
            {
                try
                {      
                    WorkUnit unit = queue.take();
                    queue.push(unit);
                }
                catch ( InterruptedException ie )
                {
                    print.info("Interrupted");
                }
                processQueue();
                if ( shutdownRequested )
                {
                    done = true;
                }
            }
        }
        catch ( Exception e )
        {
            Log.error("Unexpected exception in token persistence thread: " + 
                    e.getMessage(), e);
        }
                        
        Log.system("Thread stopped");
        NDC.pop();
    }
    
    private void processQueue()
    {
        int numProcessed = 0;
        int numFailures = 0;
        
        while ( !queue.isEmpty() )
        {
            int numInBatch = 0;
            List<IssuedToken> issuedTokens = new LinkedList<IssuedToken>();    
            TokenResponseWrapper trWrapper = new TokenResponseWrapper();
            
            try
            {            
                while ( !queue.isEmpty() && numInBatch < batchSize )
                {
                    WorkUnit unit = queue.pop();
                    TokenStore store = unit.getTokenStore();
                    IssuedToken issued = new IssuedToken();
                    issued.setTokenStore(store);
                    issued.setTokenResponse(trWrapper.encode(unit.getTokenResponse()));
                    issuedTokens.add(issued);
                    numInBatch++;
                }
                tokenStoreBean.addTokenStoreItems(issuedTokens);
                exceptionOnLastPersist = false;
                numProcessed += numInBatch;
                if ( print.isDebugEnabled() )
                {
                    print.debug("Processed batch, queue length: " + 
                            queue.size());
                }
            }
            catch ( Exception e )
            {
                if ( !exceptionOnLastPersist )
                {
                    Log.error("Token persistence error: " + e.getMessage(), 
                            e);
                }
                else
                {
                    print.error("Token persistence error: " + 
                            e.getMessage(), e);
                }
                exceptionOnLastPersist = true;
                numFailures += numInBatch;
            }
            finally
            {
                if ( numInBatch > 0 )
                {
                    requestPermits.release(numInBatch);
                }
            }
        }
        if ( numProcessed > 0 )
        {
            String strFailures = ( numFailures > 0 )
                    ? ", " + numFailures + " failure(s)"
                    : "";
            Log.system("Persisted " + numProcessed + " token(s)" + strFailures);       
        }
    }
    
}

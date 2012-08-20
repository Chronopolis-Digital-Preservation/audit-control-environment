/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.digest.DigestFactory;
import edu.umiacs.ace.digest.DigestService;
import edu.umiacs.ace.digest.InvalidHashException;
import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.hashtree.HashTree;
import edu.umiacs.ace.hashtree.HashTreeBuilder;
import edu.umiacs.ace.hashtree.Proof;
import edu.umiacs.ace.hashtree.ProofHash;
import edu.umiacs.ace.hashtree.ProofNode;
import edu.umiacs.ace.ims.Log;
import edu.umiacs.ace.ims.round.PreviousRound;
import edu.umiacs.ace.ims.round.RoundLocal;
import edu.umiacs.ace.ims.round.RoundSummary;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.ws.ProofElement;
import edu.umiacs.ace.server.ServiceLocator;
import edu.umiacs.ace.server.exception.ServerBusyException;
import edu.umiacs.ace.server.exception.ServiceUnavailableException;
import edu.umiacs.ace.util.HashFormatter;
import edu.umiacs.ace.util.HashValue;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 *
 * @author mmcgann
 */
class RoundProcessor extends Thread 
{
    private static final HashFormatter hashFormatter = new HashFormatter();
    private RoundLocal roundBean = 
            ServiceLocator.getInstance().getLocal(RoundLocal.class);
    
    private TokenClass tokenClass;
    private Queue<WorkUnit> requests = new LinkedList<WorkUnit>();
    private MessageDigest digest;
    
    private State state = State.IDLE;
    private Lock roundLock = new ReentrantLock(true);
    private Condition processorCondition = roundLock.newCondition();
    private Date runAt = null;
    private boolean shutdownRequested = false;
    private boolean processNowRequested = false;
    
    private static final Logger print = Logger.getLogger(RoundProcessor.class);

    enum State
    {
        IDLE, 
        WAITING,
        RUNNING, 
        SHUTDOWN
    };
    
    public RoundProcessor(TokenClass tokenClass)
    {
        super("Round Processor: " + tokenClass.getName());
        
        this.tokenClass = tokenClass;
        DigestService ds = DigestFactory.getInstance().getService(
                tokenClass.getDigestProviderName(), 
                tokenClass.getDigestServiceName());
        digest = ds.createMessageDigest();
    }

    public String getTokenClassName()
    {
        return tokenClass.getName();
    }
        
    public Date getNextRun()
    {
        Date retValue = runAt;
        if ( retValue == null )
        {
            retValue = nextRuntime();
            if ( retValue == null )
            {
                retValue = new Date();
            }
        }
        return retValue;
    }
    
    public void addWorkUnit(WorkUnit workUnit)
    {
        try
        {
            if ( !roundLock.tryLock(tokenClass.getRequestTimeout(), 
                    TimeUnit.MILLISECONDS) )
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
            
        try
        {
            if ( shutdownRequested || state == State.SHUTDOWN )
            {
                throw new ServiceUnavailableException(
                        StatusCode.TOKEN_SERVICE_UNAVAILABLE);
            }
            
            requests.offer(workUnit);
            if ( requests.size() > 0 && state == State.IDLE )
            {
                processorCondition.signal();
            }
            else if ( requests.size() >= tokenClass.getMaximumRequests() 
                    && state != State.RUNNING )
            {
                processorCondition.signal();
            }
        }
        finally
        {
            roundLock.unlock();
        }   
    }
    
    public void processNow()
    {
        roundLock.lock();
        try
        {
            processNowRequested = true;
            processorCondition.signal();
        }
        finally
        {
            roundLock.unlock();
        }
    }
            
    public void shutdown()
    {
        roundLock.lock();
        try
        {
            shutdownRequested = true;
            processorCondition.signal();
        }
        finally
        {
            roundLock.unlock();
        }
    }
    
    @Override
    public void run()
    {
        NDC.push(tokenClass.getName() + ": ");
        print.debug("Processor started");       
        roundLock.lock();  
        
        try
        {
            while ( state != State.SHUTDOWN )
            {
                try
                {
                    switch ( state )
                    {
                        case IDLE:
                            idleState();    break;
                        case WAITING:
                            waitState();    break;
                        case RUNNING:
                            runState();     break;
                    }
                }
                catch ( InterruptedException ie )
                {
                    print.warn("Processor interrupted");                
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        catch ( Exception e )
        {
            Log.error("Unexpected error in processor thread: " + 
                    e.getMessage(), e);
        }
        state = State.SHUTDOWN;
        
        roundLock.unlock();        
        print.debug("Processor shutdown");
        NDC.pop();
    }

    private void idleState() throws InterruptedException
    {
        print.debug("Idle");
        processorCondition.await();
        changeState();
    }
    
    private void waitState() throws InterruptedException
    {
        print.debug("Waiting");
        if ( runAt == null )
        {
            processorCondition.await();
        }
        else
        {
            processorCondition.awaitUntil(runAt);
        }
        changeState();
    }
    
    private void runState()
    {
        print.debug("Running");
        process();     
        processNowRequested = false;
        changeState();
    }
    
    private void changeState()
    {
        int queueLength = requests.size();
        int maxLength = tokenClass.getMaximumRequests();
        
        if ( print.isTraceEnabled() )
        {
            print.trace("Changing state, queue length: " + queueLength + 
                    ", maximum requests: " + maxLength + ", now: " + 
                    new Date() + ", runAt: " + runAt + ", state: " + state);
        }
        if ( queueLength == 0 && shutdownRequested )
        {
            state = State.SHUTDOWN;
        }
        else if ( queueLength == 0 )
        {
            state = State.IDLE;
            runAt = null;
        }
        else if ( queueLength >= maxLength || shutdownRequested || 
                processNowRequested )
        {
            state = State.RUNNING;
        }
        else if ( state == State.WAITING && new Date().after(runAt) )
        {
            state = State.RUNNING;
        }
        else if ( queueLength < maxLength && state != State.WAITING )
        {
            state = State.WAITING;
            runAt = nextRuntime();
        }
    }

    private Date nextRuntime()
    {
        return ( tokenClass.getRoundLength() == 0 ) 
                ? null
                : new Date(System.currentTimeMillis() + 
                  tokenClass.getRoundLength());
    }
    
    private void process()
    {
        try
        {
            process0();
        }
        catch ( Exception e )
        {
            Log.error("Unexpected error while processing round for " +
                    "token class (" + tokenClass.getName() + "): " +
                    e.getMessage(), e);
        }
    }

    private void process0()
    {
        if ( requests.size() == 0 )
        {
            Log.system("No requests to process");
            return;
        }
        
        print.debug("Processing requests, queue length: " + 
                requests.size() + ", maximum nodes: " + 
                tokenClass.getMaximumRequests());

        HashTreeBuilder hashTreeBuilder = buildHashTree();
        RoundSummary summary = new RoundSummary();
        HashTree hashTree = linkHashTree(hashTreeBuilder, summary);
        int numProcessed = enqueueResponses(hashTree, summary);
        
        Log.system("Round " + summary.getId() +
                " complete: " + numProcessed + " unit(s), " + 
                hashTree.getLeafNodes().size() + " node(s), time: " +
                summary.getTimestamp() + ", queue length: " +
                requests.size());
    }

    /**
     * Client requested link. Generated Hash tree for round consists of two pieces,
     * the hashString from the client and the previous round linking hash
     * 
     * @param clientId
     * @param hashString
     * @return
     */
    public LinkResponse link(String clientId, String hashString)
    {
        roundLock.lock();
        try
        {
            byte[] hash = HashValue.asBytes(hashString);
            HashTreeBuilder hashTreeBuilder = new HashTreeBuilder(digest);
            hashTreeBuilder.add(hash);
            
            RoundSummary summary = new RoundSummary();
            HashTree hashTree = linkHashTree(hashTreeBuilder, summary);
            
            LinkResponse response = new LinkResponse();
            response.setPreviousHash(
                    HashValue.asHexString(hashTree.getLinkNode().getHash()));
            response.setRootHash(
                    HashValue.asHexString(hashTree.getRootNode().getHash()));
            response.setTimestamp(summary.getTimestamp());
            response.setRoundId(summary.getId());
            
            Log.system("Round " + summary.getId() + " linked from: " + 
                    clientId);
            return response;
        }
        catch ( NumberFormatException nfe )
        {
            throw new InvalidHashException(hashString);
        }
        finally
        {
            roundLock.unlock();
        }
    }
          
    private HashTreeBuilder buildHashTree()
    {
        HashTreeBuilder htBuilder = new HashTreeBuilder(digest,
                tokenClass.getTreeOrder());        
        
        int numRequests = 0;
        while ( !requests.isEmpty() && 
                numRequests < tokenClass.getMaximumRequests() )
        {
            WorkUnit workUnit = requests.poll();            

            String strHashValue = workUnit.getTokenRequest().getHashValue();            
            try
            {
                byte[] hashValue = HashValue.asBytes(strHashValue);
                htBuilder.add(hashValue, workUnit);
                if ( print.isTraceEnabled() )
                {
                    print.trace("Request: client=" +
                            workUnit.getClientId() +
                            ", hash=" + hashFormatter.format(
                            workUnit.getTokenRequest().getHashValue()) + 
                            ", name=" + workUnit.getTokenRequest().getName());
                }
                numRequests++;            
            }
            catch ( NumberFormatException nfe )
            {
                print.error("Invalid hash value: " + strHashValue);
                enqueueError(StatusCode.INVALID_HASH_VALUE, workUnit);
            }
        }  
        
        if ( numRequests < tokenClass.getMinimumRequests() )
        {
            int diff = tokenClass.getMinimumRequests() - numRequests;
            Random random = new Random();
            byte[] randomValue = new byte[digest.getDigestLength()];
            for ( int i = 0; i < diff; i++ )
            {
                random.nextBytes(randomValue);
                htBuilder.add(randomValue);
                if ( print.isTraceEnabled() )
                {
                    print.trace("Random: hash=" +
                            hashFormatter.format(randomValue));
                }
            }
        }        
        
        return htBuilder;
    }
    
    /**
     * Link this current round to previous round.
     * 
     * @param htBuilder
     * @param summary empty summary to be filled out with information for this round
     * 
     * @return
     */
    private HashTree linkHashTree(HashTreeBuilder htBuilder, 
            RoundSummary summary)
    {        
        PreviousRound previousRound = roundBean.findPreviousRound(tokenClass);
        RoundSummary previousSummary = previousRound.getRoundSummary();
        print.debug("Previous round hash: " +
                hashFormatter.format(previousRound.getRoundSummary().getHashValue()));
        htBuilder.link(HashValue.asBytes(previousSummary.getHashValue()));

        HashTree hashTree = htBuilder.build();
        summary.setHashValue(
                HashValue.asHexString(hashTree.getRootNode().getHash()));
        summary.setTimestamp(new Date());
        summary.setTokenClass(tokenClass);
        roundBean.createRoundSummary(summary);
        
        return hashTree;
    }
    
    /**
     * Convert hashtree response into a number of workunits to be returned
     * to clients. This is where Proof to TokenResponse occurs.
     * 
     * Responses are created from leafs in the supplied hashTree
     * 
     * @param hashTree tree for current round
     * @param summary summary information for current round
     * @return
     */
    private int enqueueResponses(HashTree hashTree, RoundSummary summary)
    {
        int numResponses = 0;
        for ( HashTree.Node node: hashTree.getLeafNodes() )
        {
            WorkUnit workUnit = (WorkUnit)node.getData();
            // If the node doesn't have data, it was a randomly generated
            // hash, no need to enqueue
            if ( node.getData() != null )
            {
                numResponses++;
                TokenRequest request = workUnit.getTokenRequest();
                TokenResponse response = new TokenResponse();
                response.setName(request.getName());
                response.setStatusCode(StatusCode.SUCCESS);
                response.setRoundId(summary.getId());
                response.setTimestamp(summary.getTimestamp());
                response.setTokenClassName(tokenClass.getName());
                response.setDigestService(tokenClass.getDigestServiceName());
                response.setDigestProvider(tokenClass.getDigestProviderName());
  
                Proof proof = hashTree.proof(node);
                response.setProofElements(convertProof(proof));
                workUnit.setTokenResponse(response);
                
                ResponseDestination dest = workUnit.getResponseDestination();
                dest.enqueue(workUnit);
            }
        }  
        return numResponses;
    }
    
    private void enqueueError(int statusCode, WorkUnit workUnit)
    {
        TokenRequest request = workUnit.getTokenRequest();
        TokenResponse response = new TokenResponse();
        ResponseDestination dest = workUnit.getResponseDestination();
        
        response.setName(request.getName());
        response.setStatusCode(statusCode);
        workUnit.setTokenResponse(response);
        dest.enqueue(workUnit);
    }
    
    /**
     * Convert a proof to a list of ProofElements for returning to client.
     * 
     * @param proof
     * @return
     */
    public static List<ProofElement> convertProof(Proof proof)
    {
        int numNodes = proof.getProofNodes().size();
        List<ProofElement> imsProofNodes = 
                new ArrayList<ProofElement>(numNodes);
        for ( ProofNode node: proof )
        {
            ProofElement imsNode = new ProofElement();
            imsNode.setIndex(node.getIndex());
            int numHashes = node.getHashes().size();
            List<String> imsHashes = new ArrayList<String>(numHashes);
            for ( ProofHash proofHash: node )
            {
                String imsHash = HashValue.asHexString(proofHash.getHash());
                imsHashes.add(imsHash);
            }
            imsNode.setHashes(imsHashes);
            imsProofNodes.add(imsNode);
        }
        return imsProofNodes;
    }
   
}

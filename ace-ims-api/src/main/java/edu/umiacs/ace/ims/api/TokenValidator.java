/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
// $Id$

package edu.umiacs.ace.ims.api;

import edu.umiacs.ace.hashtree.ProofValidator;
import edu.umiacs.ace.ims.ws.ProofElement;
import edu.umiacs.ace.ims.ws.RoundSummary;
import edu.umiacs.ace.token.AceToken;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.util.Check;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 *
 * @author toaster
 */
public class TokenValidator extends Thread
{

    private Map<AceToken, String> requests = new HashMap<AceToken, String>();
    private IMSService connection;
    private ValidationCallback callback;
    private static final Logger print =
            Logger.getLogger(TokenValidator.class);
    private boolean shutdownRequested = false;
    private boolean processNow = false;
    private int maxWaitTime;
    private int maxQueueLength;
    private Lock lock = new ReentrantLock();
    private Condition processCondition;
    private MessageDigest digest;

    TokenValidator(IMSService connection, ValidationCallback callback,
            int maxWaitTime, int maxQueueLength, MessageDigest digest)
    {
        Check.notNull("connection", connection);
        Check.notNull("callback", callback);
        Check.notNull("digest", digest);
        this.connection = connection;
        this.callback = callback;
        this.maxQueueLength = maxQueueLength;
        this.maxWaitTime = maxWaitTime;
        this.digest = digest;

        processCondition = lock.newCondition();
        this.start();
    }

//    public void add(String fileHash, AceToken token) throws InterruptedException
//    {
//        Check.notNull("token", token);
//        Check.notNull("fileHash", fileHash);
//
//        lock.lockInterruptibly();
//        try
//        {
//            if ( shutdownRequested )
//            {
//                throw new IllegalStateException("Process shutdown");
//            }
//            print.trace("Adding work: " + fileHash);
//            requests.put(IMSUtil.convertToken(token), fileHash);
//            if ( requests.size() >= maxQueueLength )
//            {
//                processCondition.signal();
//            }
//        }
//        finally
//        {
//            lock.unlock();
//        }
//    }

    public void add(String fileHash, AceToken token) throws InterruptedException
    {
        Check.notNull("token", token);
        Check.notNull("fileHash", fileHash);

        lock.lockInterruptibly();
        try
        {
            if ( shutdownRequested )
            {
                throw new IllegalStateException("Process shutdown");
            }
            print.trace("Adding work: " + fileHash);
            requests.put(token, fileHash);
            if ( requests.size() >= maxQueueLength )
            {
                processCondition.signal();
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void close()
    {
        lock.lock();
        try
        {
            shutdownRequested = true;
            print.info("Shutdown requested");
            processCondition.signal();
        }
        finally
        {
            lock.unlock();
        }

        try
        {
            this.join();
        }
        catch ( InterruptedException ie )
        {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isBatchReady()
    {
        return requests.size() > 0 &&
                (requests.size() >= maxQueueLength ||
                processNow ||
                shutdownRequested);
    }

    /**
     * 1. Determine which round numbers are in this batch
     * 2. Request those rounds from the IMS
     * 3. For each token, check to ensure it validates
     * 4. call appropriate callback for each token
     */
    private void processBatch()
    {
        List<Long> roundNumbers = new ArrayList<Long>();
        ProofValidator pv = new ProofValidator();

        Map<Long, LinkedList<WorkUnit>> proofMap = new HashMap<Long, LinkedList<WorkUnit>>();

        lock.lock();
        try
        {
            print.trace("Processing batch of size: " + requests.size());
            for ( AceToken token : requests.keySet() )
            {
                String hash = requests.get(token);
                WorkUnit unit = new WorkUnit();
                unit.setHash(hash);
                unit.setTokenResponse(token);

                if ( !roundNumbers.contains(token.getRound()) )
                {

                    roundNumbers.add(token.getRound());
                    proofMap.put(token.getRound(), new LinkedList<WorkUnit>());
                }

                proofMap.get(token.getRound()).offer(unit);
            }
            print.trace("batch load finished, calling clear, unlocking.");
            requests.clear();
            print.trace("requests size: " + requests.size());
        }
        finally
        {
            lock.unlock();
        }

        print.trace("Calling IMS for " + roundNumbers.size() + " rounds");
        List<RoundSummary> summaries = connection.getRoundSummaries(roundNumbers);
        print.trace("IMS returned " + summaries.size() + " rounds");

        for ( RoundSummary summary : summaries )
        {

            long round = summary.getId();

            for ( WorkUnit unit : proofMap.get(round) )
            {
                AceToken response = unit.getTokenResponse();
                String localLeafHash = unit.getHash();

                String imsSuppliedHash = summary.getHashValue();
//                String calculatedHash = calculateRoot(digest,
//                        localLeafHash, response.getProofElements());
                String calculatedHash = HashValue.asHexString(pv.rootHash(digest, response.getProof(), HashValue.asBytes(localLeafHash)));

                if ( imsSuppliedHash.equals(calculatedHash) )
                {
                    callback.validToken(response);
                }
                else
                {
                    callback.invalidToken(response, imsSuppliedHash,
                            calculatedHash);
                }

            }
            proofMap.get(round).clear();

        }
        summaries.clear();
        print.trace("processBatch finished");
    }

    @Override
    public void run()
    {

        NDC.push("Validation Thread: ");
        print.info("Started");

        lock.lock();
        try
        {

            while ( true )
            {
                print.info("Checking batch");
                while ( isBatchReady() )
                {
                    print.info("Processing batch, queued items: " + requests.size());
                    processBatch();
                }
                if ( shutdownRequested )
                {
                    print.info("Shutdown acknowledged");
                    break;
                }
                Date deadline = new Date(System.currentTimeMillis() +
                        maxWaitTime);
                try
                {
                    print.info("Waiting until: " + deadline);
                    processNow = !processCondition.awaitUntil(deadline);
                }
                catch ( InterruptedException ie )
                {
                    Thread.currentThread().interrupt();
                    print.info("Interrupted");
                    break;
                }
            }

        }
        catch ( Throwable e )
        {
            callback.unexpectedException(e);
        }
        finally
        {
            lock.unlock();
        }
        print.info("Stopped");
    }

    /**
     * Calculate the root hash given the leaf, local hash and a list of proof elements
     * proof calculation starts at elements[0] . The element.getIndex() position is 
     * where the localHash, or hash from lower tree level is inserted. The
     * hashes for each element are run through the supplied digest. the elements[n]
     * value is what eventually gets returned. Please note digest will be reset prior
     * to computation start
     * 
     * @param digest algorithm to use
     * @param localHash leaf hash to start tree bottom w/
     * @param elements levels proof tree w/ idx 0 the leaf and idx n root -1
     * @return
     */
    public static String calculateRoot(MessageDigest digest, String localHash,
            List<ProofElement> elements)
    {
        // previous from lower level in tree is inserted at element.getIndex() position
        byte[] currentHash = HashValue.asBytes(localHash);
        digest.reset();

        for ( ProofElement element : elements )
        {
            digest.reset();
            int index = element.getIndex();
            int i = 0;
//            System.out.println("index: " + element.getIndex());
            for ( String hash : element.getHashes() )
            {
                byte[] hashBytes = HashValue.asBytes(hash);

                // it element index is the current index, insert then continue on
                if ( i == index )
                {
//                    System.out.println("prev hash: " + i + " " + HashValue.asHexString(currentHash));
                    digest.update(currentHash);
                    i++;
                }

//                System.out.println("elem hash: " + i + " " + hash);
                digest.update(hashBytes);
                i++;


            }
            // end case, if index is last element in list, then need to append
            if ( index == i )
            {
//                System.out.println("prev hash: " + i + " " + HashValue.asHexString(currentHash));
                digest.update(currentHash);
            }
            currentHash = digest.digest();
//            System.out.println("Calc ihv: " + HashValue.asHexString(currentHash));
        }
        return HashValue.asHexString(currentHash);
    }

    private class WorkUnit
    {

        private String hash;
        private AceToken tokenResponse;

        public WorkUnit()
        {
        }

        public void setTokenResponse(AceToken tokenResponse)
        {
            this.tokenResponse = tokenResponse;
        }

        public AceToken getTokenResponse()
        {
            return tokenResponse;
        }

        public void setHash(String hash)
        {
            this.hash = hash;
        }

        public String getHash()
        {
            return hash;
        }
    }
}

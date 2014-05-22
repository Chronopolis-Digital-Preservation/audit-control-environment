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

package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.monitor.settings.SettingsConstants;
import edu.umiacs.ace.token.TokenStoreEntry;
import edu.umiacs.ace.token.TokenStoreReader;
import edu.umiacs.ace.util.TokenUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * TODO - Possibly make a LinkedBlockingQueue that threads can poll from
 *        to get items to ingest
 *      - What happens when maxThreads is changed in the middle of ingestion?
 *          - May need to lock it
 *
 * @author shake
 */
public class IngestThreadPool {
    
    private static final IngestThreadPool instance = new IngestThreadPool();
    private static final Logger LOG = Logger.getLogger(IngestThreadPool.class);
    
    private static Map<Collection, Set<String>> hasSeen;
    private static ThreadPoolExecutor threads;
    private static ThreadPoolExecutor dirThread;
    private static int maxThreads =
            Integer.parseInt(SettingsConstants.maxIngestThreads);
    private static LinkedBlockingQueue ingestQueue = new LinkedBlockingQueue();
    private static LinkedBlockingQueue dirQueue = new LinkedBlockingQueue();
    
    private IngestThreadPool() {
    }
    
    public static IngestThreadPool getInstance() {
        // We instantiate here to ensure 2 things:
        // 1 - We use the correct number for max threads from the DB
        // 2 - Java will throw an exception otherwise
        if ( threads == null ) {
            threads = new ThreadPoolExecutor(maxThreads,
                    maxThreads, 5, TimeUnit.MINUTES, ingestQueue);
        }
        if ( dirThread == null ) {
            dirThread = new ThreadPoolExecutor(1, 1, 5, TimeUnit.MINUTES, dirQueue);
        }
        if ( hasSeen == null ) {
            hasSeen = new HashMap<Collection, Set<String>>();
        }
        return instance;
    }
    
    public static void submitTokenStore(TokenStoreReader tokenReader, Collection coll) {
        if ( tokenReader == null ) {
            throw new RuntimeException("Token file is corrupt");
        }
        IngestThreadPool thePool = IngestThreadPool.getInstance();
        HashMap<String, Token> batchTokens = new HashMap<String, Token>();
        
        while ( tokenReader.hasNext() ) {
            TokenStoreEntry tokenEntry = tokenReader.next();
            Token token = TokenUtil.convertFromAceToken(tokenEntry.getToken());
            
            if ( !token.getProofAlgorithm().equals(coll.getDigestAlgorithm()) ) {
                throw new RuntimeException("Token digest differs from"
                        + " collection digest.");
            }
            batchTokens.put(tokenEntry.getIdentifiers().get(0), token);
        }
        thePool.submitTokens(batchTokens, coll);
    }
    
    public void submitTokens(Map<String, Token> tokens, Collection coll) {
        dirThread.execute(new IngestDirectory(tokens.keySet(), coll));

        // Just to avoid an ugly cast
        double max = maxThreads;

        double numThreads = (tokens.size()/max> maxThreads)
                ? maxThreads
                : Math.ceil(tokens.size()/max);
        LOG.debug("Number of threads for ingestion: " + numThreads);

        // Remove any tokens we've already seen and can possibly be in progress
        // Possibly release tokens after the thread has finished merging them
        Set<String> tokensSeen = hasSeen.get(coll);
        if ( tokensSeen == null ) {
            tokensSeen = new HashSet<String>();
            tokensSeen.addAll(tokens.keySet());
        }else {
            tokens.keySet().removeAll(hasSeen.get(coll));
            tokensSeen.addAll(tokens.keySet());
        }
        hasSeen.put(coll, tokensSeen);
        
        // Split the token store we're given up equally among our threads
        // and submit jobs to the thread pool
        int begin = 0;
        List<String> keyList = new ArrayList<String>(tokens.keySet());
        for ( int idx=0; idx < numThreads; idx++) {
            int end = (int) (tokens.size() * ((idx + 1) / numThreads));
            threads.execute( new IngestThread(tokens, coll, keyList.subList(begin, end)));
            begin = end;
        }
    }
    
    public String getStatus() {
        return String.format("[Thread Pool] Active: %d, Completed: %d, Total: %d",
                threads.getActiveCount(),
                threads.getCompletedTaskCount(),
                threads.getTaskCount());
    }
    
    // Not entirely accurate for the jsp, but it'll show what collections are
    // ingesting what
    public Map<Collection, Set<String>> getIngestedItems() {
        return hasSeen;
    }
    
    public static void setMaxThreads(int maxThreads) {
        IngestThreadPool.maxThreads = maxThreads;
    }
    
    protected static void shutdownPools() {
        LOG.debug("[Ingest]: Shutting down thread pools.");
        threads.shutdown();
        if (!threads.isTerminated() ) {
            threads.shutdownNow();
        }
        
        dirThread.shutdown();
        if ( !dirThread.isShutdown()) {
            dirThread.shutdownNow();
        }
    }
}

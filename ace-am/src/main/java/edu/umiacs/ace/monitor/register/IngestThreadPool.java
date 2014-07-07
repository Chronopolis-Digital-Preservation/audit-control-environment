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

import edu.umiacs.ace.monitor.audit.AuditThreadFactory;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.monitor.settings.SettingsConstants;
import edu.umiacs.ace.token.TokenStoreEntry;
import edu.umiacs.ace.token.TokenStoreReader;
import edu.umiacs.ace.util.TokenUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO - Possibly make a LinkedBlockingQueue that threads can poll from
 * to get items to ingest
 * - What happens when maxThreads is changed in the middle of ingestion?
 * - May need to lock it
 *
 * @author shake
 */
public class IngestThreadPool {

    private static final IngestThreadPool instance = new IngestThreadPool();
    private static final Logger LOG = Logger.getLogger(IngestThreadPool.class);

    private static CopyOnWriteArraySet<Collection> collections;
    private static Map<Collection, Set<String>> hasSeen;
    private static ForkJoinPool forkJoinPool;
    private static int maxThreads =
            Integer.parseInt(SettingsConstants.maxIngestThreads);
    private static ThreadPoolExecutor executor;
    private static LinkedBlockingQueue supervisorQueue =
            new LinkedBlockingQueue();

    private IngestThreadPool() {
    }

    public static IngestThreadPool getInstance() {
        // We instantiate here to ensure 2 things:
        // 1 - We use the correct number for max threads from the DB
        // 2 - Java will throw an exception otherwise
        if (executor == null) {
            executor = new ThreadPoolExecutor(maxThreads, maxThreads, 5, TimeUnit.MINUTES, supervisorQueue);
        }
        if (hasSeen == null) {
            hasSeen = new HashMap<Collection, Set<String>>();
        }
        if (forkJoinPool == null) {
            forkJoinPool = new ForkJoinPool();
        }
        if (collections == null) {
            collections = new CopyOnWriteArraySet<Collection>();
        }

        return instance;
    }

    /**
     * Submit a token store to ingest
     *
     * @param tokenReader The token store
     * @param coll The collection to ingest to
     */
    public static void submitTokenStore(TokenStoreReader tokenReader, Collection coll) {
        if (tokenReader == null) {
            throw new RuntimeException("Token file is corrupt");
        }
        IngestThreadPool thePool = IngestThreadPool.getInstance();
        HashMap<String, Token> batchTokens = new HashMap<String, Token>();

        while (tokenReader.hasNext()) {
            TokenStoreEntry tokenEntry = tokenReader.next();
            Token token = TokenUtil.convertFromAceToken(tokenEntry.getToken());

            if (!token.getProofAlgorithm().equals(coll.getDigestAlgorithm())) {
                throw new RuntimeException("Token digest differs from"
                        + " collection digest.");
            }
            batchTokens.put(tokenEntry.getIdentifiers().get(0), token);
        }
        thePool.submitTokens(batchTokens, coll);
    }

    public static boolean isIngesting(Collection collection) {
        return collections.contains(collection);
    }

    /**
     * Something like this to wait until all ingestion has completed before doing anything
     *
     * @param collection
     */
    public static void awaitIngestionComplete(Collection collection) {
        while (collections.contains(collection)) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                LOG.error("Sleep Interrupted, returning");
                return;
            }
        }

    }

    /**
     * Submit a batch of tokens to be ingested
     *
     * @param tokens The tokens to ingest (mapping path to the token)
     * @param coll The collection to ingest to
     */
    public void submitTokens(Map<String, Token> tokens, Collection coll) {
        if (AuditThreadFactory.isQueued(coll) || AuditThreadFactory.isRunning(coll)) {
            LOG.error("Cannot ingest tokens for a collection which is auditing");
        } else if (executor.getActiveCount() == executor.getMaximumPoolSize()) {
            LOG.error("Executor has reached it's maximum limit");
        } else {
            executor.execute(new IngestSupervisor(tokens, coll));
        }
    }

    public String getStatus() {
        return String.format("[Thread Pool] Active: %d, Completed: %d, Total: %d",
                executor.getActiveCount(),
                executor.getCompletedTaskCount(),
                executor.getTaskCount());
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
        LOG.debug("[Ingest] Shutting down thread pools.");
        executor.shutdown();
        if (!executor.isTerminated()) {
            executor.shutdownNow();
        }
    }

    /**
     * A private class to supervise token ingestion. We use it to keep track of
     * what collections we have seen
     *
     */
    private class IngestSupervisor implements Runnable {
        private Map<String, Token> tokens;
        private Collection coll;

        public IngestSupervisor(final Map<String, Token> tokens, final Collection coll) {
            this.tokens = tokens;
            this.coll = coll;
        }

        public void run() {
            collections.add(coll);
            try {
                ForkJoinTask dirTask = forkJoinPool.submit(new IngestDirectory(tokens.keySet(), coll));

                // Remove any tokens we've already seen and can possibly be in progress
                // Possibly release tokens after the thread has finished merging them
                Set<String> tokensSeen = hasSeen.get(coll);
                if (tokensSeen == null) {
                    tokensSeen = new HashSet<String>();
                    tokensSeen.addAll(tokens.keySet());
                } else {
                    tokens.keySet().removeAll(hasSeen.get(coll));
                    tokensSeen.addAll(tokens.keySet());
                }
                hasSeen.put(coll, tokensSeen);

                // Split the token store we're given up equally among our threads
                // and submit jobs to the thread pool
                List<String> keyList = new ArrayList<String>(tokens.keySet());

                ForkJoinTask fileTask = forkJoinPool.submit(new IngestThread(tokens, coll, keyList));

                dirTask.quietlyJoin();
                fileTask.quietlyJoin();
            } finally {
                collections.remove(coll);
            }
        }
    }
}

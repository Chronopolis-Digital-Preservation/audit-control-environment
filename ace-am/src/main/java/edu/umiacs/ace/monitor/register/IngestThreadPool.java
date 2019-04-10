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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.monitor.support.SubmissionResult;
import edu.umiacs.ace.token.TokenStoreEntry;
import edu.umiacs.ace.token.TokenStoreReader;
import edu.umiacs.ace.util.CollectionThreadPoolExecutor;
import edu.umiacs.ace.util.KSFuture;
import edu.umiacs.ace.util.TokenUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
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

    private static Cache<Collection, KSFuture<IngestSupervisor>> cache;

    private IngestThreadPool() {
    }

    public static IngestThreadPool getInstance() {
        // We instantiate here to ensure 2 things:
        // 1 - We use the correct number for max threads from the DB
        // 2 - Java will throw an exception otherwise
        if (cache == null) {
            // WeakReference instead?
            cache = Caffeine.newBuilder()
                    .expireAfterAccess(10, TimeUnit.MINUTES)
                    .build();
        }

        return instance;
    }

    /**
     * Submit a token store to ingest
     *
     * @param tokenReader The token store
     * @param coll The collection to ingest to
     */
    public static SubmissionResult submitTokenStore(TokenStoreReader tokenReader, Collection coll) {
        if (tokenReader == null) {
            throw new RuntimeException("Token file is corrupt");
        }
        IngestThreadPool thePool = IngestThreadPool.getInstance();
        HashMap<String, Token> batchTokens = new HashMap<>();

        while (tokenReader.hasNext()) {
            TokenStoreEntry tokenEntry = tokenReader.next();
            Token token = TokenUtil.convertFromAceToken(tokenEntry.getToken());

            if (!token.getProofAlgorithm().equals(coll.getDigestAlgorithm())) {
                throw new RuntimeException("Token digest differs from"
                        + " collection digest.");
            }
            batchTokens.put(tokenEntry.getIdentifiers().get(0), token);
        }

        return thePool.submitTokens(batchTokens, coll);
    }

    /**
     * Submit a batch of tokens to be ingested
     * TODO: This can me memory intensive for large amounts of tokens,
     *       maybe find a way to chunk it up?
     *
     * @param tokens The tokens to ingest (mapping path to the token)
     * @param coll The collection to ingest to
     */
    public SubmissionResult submitTokens(Map<String, Token> tokens, Collection coll) {
        SubmissionResult result = SubmissionResult.CONFLICT;
        CollectionThreadPoolExecutor executor = CollectionThreadPoolExecutor.getExecutor();

        // We may want to save the Future so that we can display information
        // about the current ingestion
        KSFuture<IngestSupervisor> future =
                executor.submitIngestThread(coll, new IngestSupervisor(tokens, coll));

        if (future != null) {
            cache.put(coll, future);
            result = SubmissionResult.SUCCESS;
        }

        return result;
    }

    public ConcurrentMap<Collection, KSFuture<IngestSupervisor>> getCache() {
        return cache.asMap();
    }

    public String getStatus() {
        return "[Thread Pool] Active";
    }

    protected static void shutdownPools() {
        LOG.debug("[Ingest] Shutting down thread pools.");
        cache.invalidateAll();
    }

}

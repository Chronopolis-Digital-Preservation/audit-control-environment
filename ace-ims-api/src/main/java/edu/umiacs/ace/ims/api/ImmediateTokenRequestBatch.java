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

import edu.umiacs.ace.ims.ws.TokenRequest;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.util.Check;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread which uses the {@link IMSService} to call
 * {@link IMSService#requestTokensImmediate(String, List)} when processing ACE Collections which
 * need to receive ACE Tokens for files.
 *
 * @author mmcgann
 */
class ImmediateTokenRequestBatch extends Thread implements TokenRequestBatch {

    private static final Logger print = Logger.getLogger(ImmediateTokenRequestBatch.class);

    private RequestBatchCallback callback;
    private boolean processNow = false;
    private boolean shutdownRequested = false;
    private LinkedList<TokenRequest> requests = new LinkedList<>();

    private final IMSService service;
    private final String identifier;
    private final String tokenClassName;
    private final int maxWaitTime;
    private final int maxQueueLength;
    private final Lock lock = new ReentrantLock();
    private final Condition processCondition;

    ImmediateTokenRequestBatch(IMSService imsService,
                               String identifier,
                               String tokenClassName,
                               RequestBatchCallback callback,
                               int maxQueueLength,
                               int maxWaitTime) {
        this.service = imsService;
        this.identifier = identifier;
        this.tokenClassName = tokenClassName;
        this.callback = callback;
        this.maxQueueLength = maxQueueLength;
        this.maxWaitTime = maxWaitTime;
        processCondition = lock.newCondition();
        this.start();
    }

    /**
     * Add a {@link TokenRequest} to the {@code requests} list so that an ACE Token can be
     * requested from the ACE IMS
     * <p>
     * This method will block when trying to acquire a lock from the main thread. This can only
     * happen IFF a batch of requests is not already being processed. If the {@code requests} has a
     * size greater than or equal to the {@code maxQueueLength} after adding the
     * {@link TokenRequest}, this will wake up the main thread in order to process the current batch
     * ({@code requests}).
     *
     * @param request the {@link TokenRequest} to create an ACE Token for
     * @throws InterruptedException if the thread is interrupted
     */
    public void add(TokenRequest request) throws InterruptedException {
        Check.notNull("request", request);

        lock.lockInterruptibly();
        try {
            if (shutdownRequested) {
                throw new IllegalStateException("Process shutdown");
            }

            requests.offer(request);
            if (requests.size() >= maxQueueLength) {
                processCondition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Signal that the {@link ImmediateTokenRequestBatch} should be shut down. Blocks until the
     * lock can be acquired at which point signal the {@link Condition}. Once the final batch has
     * been run, block again on {@link #join()}.
     */
    public void close() {
        lock.lock();
        try {
            shutdownRequested = true;
            print.info("Shutdown requested on Token Request");
            processCondition.signal();
        } finally {
            lock.unlock();
        }

        try {
            this.join();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if a batch is ready
     * * requests is non empty AND
     * * requests.size is greater than equal to maxQueueLength OR
     * * processNow flag is set (the processCondition deadline elapsed) OR
     * * shutdownRequested flag is set (close called)
     *
     * @return true if a batch should be processed, false otherwise
     */
    private boolean isBatchReady() {
        lock.lock();
        try {
            return requests.size() > 0 &&
                    (requests.size() >= maxQueueLength ||
                            processNow ||
                            shutdownRequested);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Process the {@code requests} present and receive {@link TokenResponse}s from the IMS for
     * each request. This will acquire a lock while filling up a local queue of items to process,
     * so any call to {@link #add(TokenRequest)} on {@code requests} will block until
     * {@code requests} has been  drained. Once complete, the lock is released and
     * {@link TokenRequest}s can continue to be added.
     */
    private void processBatch() {
        List<TokenRequest> batch;
        lock.lock();
        try {
            batch = new ArrayList<>(maxQueueLength);
            int numAdded = 0;
            while (numAdded < maxQueueLength && !requests.isEmpty()) {
                batch.add(requests.poll());
            }
        } finally {
            lock.unlock();

        }
        try {
            print.info("Sending batch: " + batch.size() + " requests");
            List<TokenResponse> responses =
                    service.requestTokensImmediate(tokenClassName, batch);
            callback.tokensReceived(batch, responses);
        } catch (Exception e) {
            print.error("Exception on send: " + e.getMessage(), e);
            callback.exceptionThrown(batch, e);
        }
    }

    /**
     * Entry point for the main thread. Runs until {@link #close()} is called or an
     * {@link Exception} is thrown while running {@link #processBatch()} or
     * {@link Condition#awaitUntil(Date)}.
     *
     * This will attempt to acquire the {@code lock} so that it can block for {@code minWaitTime}.
     * While it is calling {@link Condition#awaitUntil(Date)}, the lock can be reacquired by other
     * treads. This allows {@link #add(TokenRequest)} to be used in order to fill the
     * {@code requests} list for processing.
     */
    @Override
    public void run() {
        NDC.push("Request Thread (" + identifier + "): ");
        print.info("Started");

        try {
            while (true) {
                print.info("Checking batch");
                while (isBatchReady()) {
                    print.info("Processing batch");
                    processBatch();
                }
                if (shutdownRequested) {
                    print.info("Shutdown acknowledged");
                    break;
                }

                lock.lock();
                Date deadline = new Date(System.currentTimeMillis() + maxWaitTime);
                try {
                    print.info("Waiting until: " + deadline);
                    processNow = !processCondition.awaitUntil(deadline);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    print.info("Interrupted");
                    break;
                } finally {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            callback.unexpectedException(e);
        } finally {
            print.info("Stopped");
            NDC.pop();
            NDC.remove();
        }

    }
}

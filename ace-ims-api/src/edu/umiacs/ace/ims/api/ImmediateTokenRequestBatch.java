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
// $Id: ImmediateTokenRequestBatch.java 3170 2010-06-15 19:55:00Z toaster $

package edu.umiacs.ace.ims.api;

import edu.umiacs.ace.ims.ws.TokenRequest;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.util.Check;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 *
 * @author mmcgann
 */
class ImmediateTokenRequestBatch extends Thread implements TokenRequestBatch
{

    //private static final int MAXQUEUESIZE = 10
    private IMSService service;
    private LinkedList<TokenRequest> requests = new LinkedList<TokenRequest>();
    private String tokenClassName;
    private int maxWaitTime;
    private int maxQueueLength;
    private RequestBatchCallback callback;
    private boolean shutdownRequested = false;
    private boolean processNow = false;
    private Lock lock = new ReentrantLock();
    private Condition processCondition;
    private static final Logger print =
            Logger.getLogger(ImmediateTokenRequestBatch.class);

    ImmediateTokenRequestBatch(IMSService service, String tokenClassName,
            RequestBatchCallback callback, int maxQueueLength, int maxWaitTime)
    {
        this.service = service;
        this.tokenClassName = tokenClassName;
        this.callback = callback;
        this.maxQueueLength = maxQueueLength;
        this.maxWaitTime = maxWaitTime;
        processCondition = lock.newCondition();
        this.start();
    }

    public void add(TokenRequest request) throws InterruptedException
    {
        Check.notNull("request", request);

        lock.lockInterruptibly();
        try
        {
            if ( shutdownRequested )
            {
                throw new IllegalStateException("Process shutdown");
            }

            requests.offer(request);
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
        lock.lock();
        try
        {
            return requests.size() > 0 &&
                    (requests.size() >= maxQueueLength ||
                    processNow ||
                    shutdownRequested);
        }
        finally
        {
            lock.unlock();
        }
    }

    private void processBatch()
    {
        List<TokenRequest> batch;
        lock.lock();
        try
        {
            batch = new ArrayList<TokenRequest>(maxQueueLength);
            int numAdded = 0;
            while ( numAdded < maxQueueLength && !requests.isEmpty() )
            {
                batch.add(requests.poll());
            }
        }
        finally
        {
            lock.unlock();

        }
        try
        {
            print.info("Sending batch: " + batch.size() + " requests");
            List<TokenResponse> responses =
                    service.requestTokensImmediate(tokenClassName, batch);
            callback.tokensReceived(batch, responses);
        }
        catch ( Exception e )
        {
            print.error("Exception on send: " + e.getMessage(), e);
            callback.exceptionThrown(batch, e);
        }
    }

    @Override
    public void run()
    {
        NDC.push("Request Thread: ");
        print.info("Started");

//        lock.lock();
        try
        {
            while ( true )
            {
                print.info("Checking batch");
                while ( isBatchReady() )
                {
                    print.info("Processing batch");
                    processBatch();
                }
                if ( shutdownRequested )
                {
                    print.info("Shutdown acknowledged");
                    break;
                }
                lock.lock();
                try
                {
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
                finally
                {
                    lock.unlock();
                }
            }
        }
        catch ( Exception e )
        {
            callback.unexpectedException(e);
        }
//        finally
//        {
//            lock.unlock();
//        }

        print.info("Stopped");
    }
}

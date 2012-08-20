/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.processor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * For the immediate token requests
 * 
 * @author mmcgann
 */
class LatchResponseQueue implements ResponseDestination
{
    private LinkedList<WorkUnit> workUnits = new LinkedList<WorkUnit>();
    private CountDownLatch latch;
    
    private LatchResponseQueue()
    {
    }
    
    public LatchResponseQueue(int requiredItemCount)
    {
        latch = new CountDownLatch(requiredItemCount);
    }
    
    public void enqueue(WorkUnit response)
    {
        if ( latch.getCount() == 0 )
        {
            throw new IllegalStateException("Count down latch already at zero");
        }
        workUnits.addLast(response);
        latch.countDown();
    }
    
    /**
     * Client can block here until all responses have finished
     * 
     * @param timeout
     * @param timeoutUnit
     * @return
     * @throws java.lang.InterruptedException
     */
    public List<TokenResponse> getResponses(long timeout, TimeUnit timeoutUnit) 
            throws InterruptedException
    {
        if ( !latch.await(timeout, timeoutUnit) )
        {
            return null;
        }
        
        List<TokenResponse> responses = new LinkedList<TokenResponse>();
        for ( WorkUnit unit: workUnits )
        {
            responses.add(unit.getTokenResponse());
        }
        return responses;
    }
    
    public boolean isReady() throws InterruptedException
    {
        return latch.await(0, TimeUnit.MILLISECONDS);
    }
}


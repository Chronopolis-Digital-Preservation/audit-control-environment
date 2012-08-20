/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.processor;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author mmcgann
 */
public class RequestPermits 
{
    private Semaphore semaphore;
    private Logger print = Logger.getLogger(RequestPermits.class);
    
    public RequestPermits(int numPermits)
    {
        semaphore = new Semaphore(numPermits, true);
    }
    
    public boolean acquire(int numPermits, int timeout, TimeUnit unit)
            throws InterruptedException
    {
        print.debug("Acquiring " + numPermits + " permit(s), available: " + 
                semaphore.availablePermits());
        return semaphore.tryAcquire(numPermits, timeout, unit);
    }
        
    public void release(int numPermits)
    {
        semaphore.release(numPermits);
        print.debug("Released " + numPermits + " permit(s), available: " + 
                semaphore.availablePermits());
    }
}

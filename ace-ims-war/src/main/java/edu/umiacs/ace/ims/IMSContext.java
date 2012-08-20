/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims;

import edu.umiacs.ace.ims.processor.WitnessPublisher;
import java.util.List;

/**
 *
 * @author mmcgann
 */
public final class IMSContext 
{
    private int maximumQueuedRequests;
    private int tokenPersistenceBatchSize;
    private int requestPermitTimeout;
    private int threadShutdownTimeout;
    private int immediateTokenResponseTimeout;
    private List<WitnessPublisher> witnessPublishers;
    private long maxTokenAge;
    
    private static IMSContext instance = new IMSContext();
    
    private IMSContext()
    {}
    
    public static IMSContext getInstance() 
    {
        return instance;
    }
    
    public int getMaximumQueuedRequests()
    {
        return maximumQueuedRequests;
    }

    void setMaximumQueuedRequests(int maximumQueuedRequests)
    {
        this.maximumQueuedRequests = maximumQueuedRequests;
    }

    public int getTokenPersistenceBatchSize()
    {
        return tokenPersistenceBatchSize;
    }

    void setTokenPersistenceBatchSize(int tokenPersistenceBatchSize)
    {
        this.tokenPersistenceBatchSize = tokenPersistenceBatchSize;
    }

    public int getRequestPermitTimeout()
    {
        return requestPermitTimeout;
    }

    void setRequestPermitTimeout(int requestPermitTimeout)
    {
        this.requestPermitTimeout = requestPermitTimeout;
    }

    public int getThreadShutdownTimeout()
    {
        return threadShutdownTimeout;
    }

    void setThreadShutdownTimeout(int threadShutdownTimeout)
    {
        this.threadShutdownTimeout = threadShutdownTimeout;
    }

    public int getImmediateTokenResponseTimeout()
    {
        return immediateTokenResponseTimeout;
    }

    void setImmediateTokenResponseTimeout(int immediateTokenResponseTimeout)
    {
        this.immediateTokenResponseTimeout = immediateTokenResponseTimeout;
    }

    public List<WitnessPublisher> getWitnessPublishers()
    {
        return witnessPublishers;
    }

    void setWitnessPublishers(List<WitnessPublisher> witnessPublishers)
    {
        this.witnessPublishers = witnessPublishers;
    }

    void setMaxTokenAge(long maxTokenAge)
    {
        this.maxTokenAge = maxTokenAge;
    }

    public long getMaxTokenAge()
    {
        return maxTokenAge;
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims;

/**
 *
 * @author mmcgann
 */
public interface IMSParameters 
{
    static final String MAXIMUM_TOKEN_STORE_AGE = 
            "ace.ims.MaxTokenStoreTime";
    
    static final String PROCESSOR_PREFIX = 
            "ace.ims.publisher";
    
    static final String DIGEST_PROVIDER = 
            "ace.ims.DigestProvider";
    
    static final String MAXIMUM_QUEUED_REQUESTS = 
            "ace.ims.MaximumQueuedRequests";
    
    static final String TOKEN_PERSISTENCE_BATCH_SIZE = 
            "ace.ims.TokenPersistenceBatchSize";
    
    static final String REQUEST_PERMIT_TIMEOUT = 
            "ace.ims.RequestPermitTimeout";
    
    static final String THREAD_SHUTDOWN_TIMEOUT = 
            "ace.ims.ThreadShutdownTimeout";
    
    static final String IMMEDIATE_TOKEN_RESPONSE_TIMEOUT = 
            "ace.ims.ImmediateTokenResponseTimeout";
    
    static final long DEFAULT_MAXIMUM_TOKEN_STORE_AGE = 1000*60*60;// one hour
    static final int DEFAULT_MAXIMUM_QUEUED_REQUESTS = 10000;
    static final int DEFAULT_TOKEN_PERSISTENCE_BATCH_SIZE = 500;
    static final int DEFAULT_REQUEST_PERMIT_TIMEOUT = 500;
    static final int DEFAULT_THREAD_SHUTDOWN_TIMEOUT = 5000;
    static final int DEFAULT_IMMEDIATE_TOKEN_RESPONSE_TIMEOUT = 1000;
    
}

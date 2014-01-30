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

import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.ims.ws.IMSFault;
import edu.umiacs.ace.ims.ws.IMSFault_Exception;
import edu.umiacs.ace.ims.ws.IMSWebService;
import edu.umiacs.ace.ims.ws.IMSWebService_Service;
import edu.umiacs.ace.ims.ws.RoundResponse;
import edu.umiacs.ace.ims.ws.RoundSummary;
import edu.umiacs.ace.ims.ws.TokenReceipt;
import edu.umiacs.ace.ims.ws.TokenRequest;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.util.Check;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author mmcgann
 */
public final class IMSService
{
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_PATH = "/ace-ims/IMSWebService?wsdl";
    private static final boolean DEFAULT_SSL = false;
    private static final boolean DEFAULT_BLOCKING = false;
    private boolean blocking;
    private long maxBlockTime;

    private IMSWebService port;
    
    private IMSService(URL url, boolean blocking, int maxBlockTimeMinutes) {
        this.blocking = blocking;
        this.maxBlockTime = TimeUnit.MILLISECONDS.convert(maxBlockTimeMinutes,
                                                          TimeUnit.MINUTES);

        try {
            IMSWebService_Service service = new IMSWebService_Service(url,
                  new QName("http://ws.ims.ace.umiacs.edu/", "IMSWebService"));
            port = service.getIMSWebServicePort();
            ((BindingProvider)port).getRequestContext()
                    .put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        } catch ( Exception e ) {
            throw handleException(e);
        }
    }

    /**
     * Open a connection to the IMS
     * 
     * @param hostName hostname of the ims, usually ims.umiacs.umd.edu
     * @param portNumber port number for the ims, usually 8080
     * @param path url path to the ims web service wsdl, usually /ace-ims/IMSWebService?wsdl
     * @param ssl flag for connecting with ssl
     * @return ims connection
     */
    public static IMSService connect(String hostName, 
                                     int portNumber, 
                                     String path, 
                                     boolean ssl,
                                     boolean blocking,
                                     int maxBlockTimeMinutes) {
        Check.notEmpty("hostName", hostName);
        Check.isPositive("portNumber", portNumber);
        Check.notNegative("maxBlockTime", maxBlockTimeMinutes);
        Check.notEmpty("path", path);
        
        if ( !path.startsWith("/") ) {
            path = "/" + path;
        }
        StringBuilder url = new StringBuilder();
        if ( ssl ) {
            url.append("https://").append(hostName).append(":")
               .append(portNumber).append(path);

        }else {
            url.append("http://").append(hostName).append(":")
               .append(portNumber).append(path);
        }
        try {
            return new IMSService(new URL(url.toString()), blocking, maxBlockTimeMinutes);
        } catch ( MalformedURLException mue ) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }
    
    /**
     * Open a connection to the IMS on the specified host using port 8080
     * and the default wsdl path
     * 
     * @param hostName IMS server
     * @return ims connection
     */
    public static IMSService connect(String hostName)
    {
        return IMSService.connect(hostName, 
                                  DEFAULT_PORT, 
                                  DEFAULT_PATH, 
                                  DEFAULT_SSL,
                                  DEFAULT_BLOCKING,
                                  0);
    }
    
    public static IMSService connect(String hostName, int port) {
        return IMSService.connect(hostName, port, DEFAULT_PATH, DEFAULT_SSL, DEFAULT_BLOCKING, 0);
    }

    public static IMSService connect(String hostName, int port, boolean ssl) {
        return IMSService.connect(hostName, port, DEFAULT_PATH, ssl, DEFAULT_BLOCKING, 0);
    }

    public static IMSService connect(String hostName, int port, boolean ssl, boolean blocking, int maxBlockTimeMinutes) {
        return IMSService.connect(hostName, port, DEFAULT_PATH, ssl, blocking, maxBlockTimeMinutes);
    }
    
//    public TokenClass createTokenClass(TokenClass tokenClass)
//    {
//        try
//        {
//            return port.createTokenClass(tokenClass);
//        }
//        catch ( Exception e )
//        {
//            throw handleException(e);
//        }
//    }
    
    public List<RoundResponse> requestWitnessProofForRounds(List<Long> rounds)
    {
        try
        {
            if ( blocking) {
                Class c = port.getClass();
                Method reqWitnessProof = c.getMethod("requestWitnessProofForRounds", List.class);
                return blockUntil(reqWitnessProof, rounds);
            } else {
                return port.createWitnessProofForRound(rounds);
            }
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
    }
    
    /**
     * Blocking call requesting the IMS close current round and return with 
     * responses to the requested tokens.
     * 
     * @param tokenClassName name of token service to use, usually 'SHA-256-0'
     * @param requests list of token requests
     * 
     * @return list of token responses.
     */
    public List<TokenResponse> requestTokensImmediate(String tokenClassName, 
            List<TokenRequest> requests)
    {
        try
        {
            if ( blocking) {
                Class c = port.getClass();
                Method reqTokensImmediate = c.getMethod("requestTokensImmediate",
                                                        String.class,
                                                        List.class);
                return blockUntil(reqTokensImmediate, tokenClassName, requests);
            } else {
                return port.requestTokensImmediate(tokenClassName, requests);
            }
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
    }
    
    public List<RoundSummary> getRoundSummaries(List<Long> rounds)
    {
        try
        {
            if ( blocking) {
                Class c = port.getClass();
                Method roundSummaries = c.getMethod("getRoundSummaries", List.class);
                return blockUntil(roundSummaries, rounds);
            } else {
                return port.getRoundSummaries(rounds);
            }
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
    }

    private <E> E blockUntil(Method m, Object... args) throws Exception {
        boolean done = false;
        E fin = null;

        long start = System.currentTimeMillis();
        long elapsed = 0;
        while ( !done && elapsed <= maxBlockTime ) {
            try {
                // avoid sleeping on the first pass
                if ( elapsed != 0 ) {
                    TimeUnit.MINUTES.sleep(15);
                }

                fin = (E) m.invoke(port, args);

                done = true;
            } catch (Exception e) {
                long now = System.currentTimeMillis();
                elapsed = now - start;

                // We want to catch when we simply could not connect to the IMS
                // server then continue to block, or when java.lang.reflect catches
                // an exception on the invocation of the method
                if ( e instanceof WebServiceException ) {
                    Throwable cause = e.getCause();
                    if ( cause instanceof java.net.ConnectException ) {
                        continue;
                    }
                } else if ( e instanceof  InvocationTargetException) {
                    // TODO: Properly catch java.net.ConnectException
                    // it's kind of tricky as the invocation exception makes
                    // the stack trace murky
                    continue;
                }
                throw handleException(e);
            }

        }

        // TODO: Throw timeout exception

        return fin;
    }

    public TokenReceipt requestTokensAsync(String tokenClassName,
            List<TokenRequest> requests)
    {
        try
        {
            if ( blocking) {
                Class c = port.getClass();
                Method m = c.getMethod("requestTokensAsync", String.class, List.class);
                return blockUntil(m, tokenClassName, requests);
            } else {
                return port.requestTokensAsync(tokenClassName, requests);
            }
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
    }

    public List<TokenResponse> retrieveAsyncTokens(List<TokenReceipt> receiptList)
    {
        if (receiptList == null || receiptList.isEmpty())
        {
            return Collections.emptyList();
        }

        List<TokenResponse> responses = new ArrayList<TokenResponse>();

        try
        {
            for (TokenReceipt receipt : receiptList)
            {
                long sessionKey = receipt.getSessionKey();
                long requestNumber = receipt.getRequestNumber();
                List<TokenResponse> rResp = port.retrieveTokens(requestNumber, sessionKey);
                responses.addAll(rResp);
            }
            return responses;
            
        } catch (Exception e)
        {
            throw handleException(e);
        }
    }
    
    /**
     * Create a request batch based on the requestTokensImmediate call. 
     * 
     * @param tokenClassName token class name to use 'SHA-256-0'
     * @param callback calback to handle token responses and errors
     * @param maxQueueLength maximum queue size before requests will be forced
     * @param maxWaitTime time to wait between sending requests
     * 
     * @return batched request
     */
    public TokenRequestBatch createImmediateTokenRequestBatch(
            String tokenClassName, RequestBatchCallback callback, 
            int maxQueueLength, int maxWaitTime)
    {
        Check.notEmpty("tokenClassName", tokenClassName);
        Check.notNull("callback", callback);
        Check.isPositive("maxQueueLength", maxQueueLength);
        Check.isPositive("maxWaitTime", maxWaitTime);
        
        return new ImmediateTokenRequestBatch(this, tokenClassName, callback, 
                maxQueueLength, maxWaitTime);
    }
    
    public TokenValidator createTokenValidator(ValidationCallback callback, 
            int maxQueueLength, int maxWaitTime, MessageDigest digest)
    {
        Check.notNull("callback", callback);
        Check.isPositive("maxQueueLength", maxQueueLength);
        Check.isPositive("maxWaitTime", maxWaitTime);
        
        return new TokenValidator(this, callback,  
                maxWaitTime, maxQueueLength, digest);
    }
    
    private IMSException handleException(Exception e)
    {
        if ( e instanceof WebServiceException )
        {
            Throwable cause = e.getCause();
            if ( cause instanceof java.net.ConnectException ||
                 cause instanceof java.io.FileNotFoundException )
            {
                return new IMSConnectionException(cause.getMessage(), cause);
            }
            
        }
        if ( e instanceof IMSFault_Exception )
        {
            IMSFault fault = ((IMSFault_Exception)e).getFaultInfo();
            return new IMSException(fault.getStatusCode(), fault.getMessage());
        }
        throw new IMSException(StatusCode.CLIENT_ERROR, "Client error: " + 
                e.getMessage(), e);
    }
}

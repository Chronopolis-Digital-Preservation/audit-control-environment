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
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

/**
 * @author mmcgann
 */
public final class IMSService {

    private static final Logger print = Logger.getLogger(IMSService.class);

    private static final int DEFAULT_RETRY = 3;
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_TIMEOUT = 5000;
    private static final String DEFAULT_PATH = "/ace-ims/IMSWebService?wsdl";
    private static final boolean DEFAULT_SSL = false;
    private static final boolean DEFAULT_BLOCKING = false;

    private boolean blocking;
    private final int imsRetryAttempts;
    private final int imsResetTimeout;

    private IMSWebService port;

    private IMSService(URL url, boolean blocking, int imsRetryAttempts, int imsResetTimeout) {
        this.blocking = blocking;
        this.imsRetryAttempts = imsRetryAttempts;
        this.imsResetTimeout = imsResetTimeout;

        try {
            IMSWebService_Service service = new IMSWebService_Service(url,
                    new QName("http://ws.ims.ace.umiacs.edu/", "IMSWebService"));
            port = service.getIMSWebServicePort();
            ((BindingProvider) port).getRequestContext()
                    .put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Open a connection to the IMS
     *
     * @param hostName   hostname of the ims, usually ims.umiacs.umd.edu
     * @param portNumber port number for the ims, usually 8080
     * @param path       url path to the ims web service wsdl, usually /ace-ims/IMSWebService?wsdl
     * @param ssl        flag for connecting with ssl
     * @return ims connection
     */
    public static IMSService connect(String hostName,
                                     int portNumber,
                                     String path,
                                     boolean ssl,
                                     boolean blocking,
                                     int imsRetryAttempts,
                                     int imsResetTimeout) {
        Check.notEmpty("hostName", hostName);
        Check.isPositive("portNumber", portNumber);
        Check.notNegative("imsRetryAttempts", imsRetryAttempts);
        Check.notNegative("imsResetTimeout", imsResetTimeout);
        Check.notEmpty("path", path);

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        StringBuilder url = new StringBuilder();
        if (ssl) {
            url.append("https://").append(hostName).append(":")
                    .append(portNumber).append(path);

        } else {
            url.append("http://").append(hostName).append(":")
                    .append(portNumber).append(path);
        }

        try {
            return new IMSService(new URL(url.toString()), blocking, imsRetryAttempts, imsResetTimeout);
        } catch (MalformedURLException mue) {
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
    public static IMSService connect(String hostName) {
        return IMSService.connect(hostName,
                DEFAULT_PORT,
                DEFAULT_PATH,
                DEFAULT_SSL,
                DEFAULT_BLOCKING,
                DEFAULT_RETRY,
                DEFAULT_TIMEOUT);
    }

    public static IMSService connect(String hostName, int port) {
        return IMSService.connect(hostName, port, DEFAULT_PATH, DEFAULT_SSL, DEFAULT_BLOCKING, DEFAULT_RETRY, DEFAULT_TIMEOUT);
    }

    public static IMSService connect(String hostName, int port, boolean ssl) {
        return IMSService.connect(hostName, port, DEFAULT_PATH, ssl, DEFAULT_BLOCKING, DEFAULT_RETRY, DEFAULT_TIMEOUT);
    }

    public static IMSService connect(String hostName,
                                     int port,
                                     boolean ssl,
                                     boolean blocking,
                                     int imsRetryAttempts,
                                     int imsResetTimeout) {
        return IMSService.connect(hostName, port, DEFAULT_PATH, ssl, blocking, imsRetryAttempts, imsResetTimeout);
    }

    public List<RoundResponse> requestWitnessProofForRounds(List<Long> rounds) {
        Supplier<IMSResult<List<RoundResponse>>> supplier = () -> {
            try {
                List<RoundResponse> witness = port.createWitnessProofForRound(rounds);
                return new IMSResult<>(witness);
            } catch (Exception e) {
                return new IMSResult<>(e);
            }
        };

        return invoke(supplier);
    }

    /**
     * Blocking call requesting the IMS close current round and return with
     * responses to the requested tokens.
     *
     * @param tokenClassName name of token service to use, usually 'SHA-256-0'
     * @param requests       list of token requests
     * @return list of token responses.
     */
    public List<TokenResponse> requestTokensImmediate(String tokenClassName,
                                                      List<TokenRequest> requests) {
        Supplier<IMSResult<List<TokenResponse>>> supplier = () -> {
            try {
                List<TokenResponse> responses = port.requestTokensImmediate(tokenClassName, requests);
                return new IMSResult<>(responses);
            } catch (Exception e) {
                return new IMSResult<>(e);
            }
        };

        return invoke(supplier);
    }

    public List<RoundSummary> getRoundSummaries(List<Long> rounds) {
        Supplier<IMSResult<List<RoundSummary>>> supplier = () -> {
            try {
                List<RoundSummary> summaries = port.getRoundSummaries(rounds);
                return new IMSResult<>(summaries);
            } catch (Exception e) {
                return new IMSResult<>(e);
            }
        } ;

        return invoke(supplier);
    }

    public TokenReceipt requestTokensAsync(String tokenClassName,
                                           List<TokenRequest> requests) {
        Supplier<IMSResult<TokenReceipt>> supplier = () -> {
            try {
                TokenReceipt receipt = port.requestTokensAsync(tokenClassName, requests);
                return new IMSResult<>(receipt);
            } catch (Exception e) {
                return new IMSResult<>(e);
            }
        };

        return invoke(supplier);
    }

    public List<TokenResponse> retrieveAsyncTokens(List<TokenReceipt> receiptList) {
        if (receiptList == null || receiptList.isEmpty()) {
            return emptyList();
        }

        List<TokenResponse> responses = new ArrayList<>();

        try {
            for (TokenReceipt receipt : receiptList) {
                long sessionKey = receipt.getSessionKey();
                long requestNumber = receipt.getRequestNumber();
                List<TokenResponse> rResp = port.retrieveTokens(requestNumber, sessionKey);
                responses.addAll(rResp);
            }
            return responses;

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Create a request batch based on the requestTokensImmediate call.
     *
     * @param identifier     a unique identifier for tracking what the request batch is working on
     * @param tokenClassName token class name to use 'SHA-256-0'
     * @param callback       callback to handle token responses and errors
     * @param maxQueueLength maximum queue size before requests will be forced
     * @param maxWaitTime    time to wait between sending requests
     * @return batched request
     */
    public TokenRequestBatch createImmediateTokenRequestBatch(String identifier,
                                                              String tokenClassName,
                                                              RequestBatchCallback callback,
                                                              int maxQueueLength,
                                                              int maxWaitTime) {
        Check.notEmpty("tokenClassName", tokenClassName);
        Check.notNull("callback", callback);
        Check.isPositive("maxQueueLength", maxQueueLength);
        Check.isPositive("maxWaitTime", maxWaitTime);

        return new ImmediateTokenRequestBatch(this, identifier, tokenClassName, callback, maxQueueLength, maxWaitTime);
    }

    /**
     * Create a {@link TokenValidator}
     *
     * @param identifier     a unique identifier for tracking what the request batch is working on
     * @param callback       callback to handle token validation and errors
     * @param maxQueueLength maximum queue size before requests will be forced
     * @param maxWaitTime    time to wait between sending requests
     * @param digest         type of {@link MessageDigest} used
     * @return the {@link TokenValidator}
     */
    public TokenValidator createTokenValidator(String identifier,
                                               ValidationCallback callback,
                                               int maxQueueLength,
                                               int maxWaitTime,
                                               MessageDigest digest) {
        Check.notNull("callback", callback);
        Check.isPositive("maxQueueLength", maxQueueLength);
        Check.isPositive("maxWaitTime", maxWaitTime);

        return new TokenValidator(this, identifier, callback, maxWaitTime, maxQueueLength, digest);
    }

    private <E> E invoke(Supplier<IMSResult<E>> supplier) {
        boolean done = false;
        IMSResult<E> imsResult = new IMSResult<>(new RuntimeException("IMS not contacted"));

        long attempt = 0;

        // first attempt always goes through (attempt == 0)
        // when blocking, make sure that attempt is less than maxAttempts
        while (!done && (attempt == 0 || (attempt <= imsRetryAttempts && blocking))) {
            if (attempt > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(imsResetTimeout);
                } catch (InterruptedException exception) {
                    // we were probably cancelled, get out of here now
                    IMSConnectionException imsException = new IMSConnectionException(
                            exception.getMessage(),
                            exception.getCause());
                    imsResult = new IMSResult<>(imsException);
                    break;
                }
            }

            print.trace("Supplier: attempt = " + attempt
                    + ", maxBlock = " + imsRetryAttempts
                    + ", blocking = " + blocking);
            imsResult = supplier.get();
            done = imsResult.getResult().isPresent();
            attempt++;
        }

        return imsResult.getOrThrow();
    }

    private IMSException handleException(Exception e) {
        if (e instanceof WebServiceException) {
            Throwable cause = e.getCause();
            if (cause instanceof java.net.ConnectException ||
                    cause instanceof java.io.FileNotFoundException) {
                return new IMSConnectionException(cause.getMessage(), cause);
            }

        }
        if (e instanceof IMSFault_Exception) {
            IMSFault fault = ((IMSFault_Exception) e).getFaultInfo();
            return new IMSException(fault.getStatusCode(), fault.getMessage());
        }
        throw new IMSException(StatusCode.CLIENT_ERROR, "Client error: " +
                e.getMessage(), e);
    }
}

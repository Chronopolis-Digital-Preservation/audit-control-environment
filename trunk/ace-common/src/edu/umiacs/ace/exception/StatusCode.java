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
// $Id: StatusCode.java 3192 2010-06-22 16:54:09Z toaster $

package edu.umiacs.ace.exception;

/**
 *
 * @author mmcgann
 */
public interface StatusCode {

    static final int SUCCESS = 100;
    static final int CLIENT_ERROR = 200;
    static final int CONNECTION_FAILURE = 201;
    static final int INVALID_PARAMETER = 300;
    static final int INVALID_DIGEST_PROVIDER = 301;
    static final int INVALID_DIGEST_SERVICE = 302;
    static final int PROVIDER_REGISTRATION_FAILED = 303;
    static final int DUPLICATE_TOKEN_CLASS = 304;
    static final int INVALID_TOKEN_CLASS = 305;
    static final int INVALID_HASH_VALUE = 306;
    static final int INVALID_CSI_ROUND = 307;
    static final int WITNESS_NOT_YET_GENERATED = 308;
    static final int TOKEN_SERVICE_UNAVAILABLE = 400;
    static final int TOKEN_REQUEST_TIMEOUT = 401;
    static final int SERVER_BUSY = 402;
    static final int TOKEN_PROCESSOR_NOT_RUNNING = 403;
    static final int TOKEN_STORE_EXPIRED = 404;
    static final int TOKEN_STORE_NOT_READY = 405;
    static final int TOKEN_STORE_DELETE_ERROR = 406;
    static final int INTERNAL_ERROR = 500;
    static final int NO_PREVIOUS_ROUND = 501;
    static final int ROUND_SEED_EXISTS = 502;
    static final int WITNESS_SEED_EXISTS = 503;
    static final int NO_PREVIOUS_WITNESS = 504;
    static final int SERVER_FAULT_START_NUMBER = 500;
}

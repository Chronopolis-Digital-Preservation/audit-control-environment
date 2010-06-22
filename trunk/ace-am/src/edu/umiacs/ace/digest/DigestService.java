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
// $Id: DigestService.java 3192 2010-06-22 16:54:09Z toaster $

package edu.umiacs.ace.digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 *
 * @author mmcgann
 */
public class DigestService implements Comparable<DigestService> {

    private Provider.Service service;
    private DigestProvider digestProvider;

    DigestService( DigestProvider digestProvider, Provider.Service service ) {
        this.digestProvider = digestProvider;
        this.service = service;
    }

    public String getName() {
        return service.getAlgorithm();
    }

    public DigestProvider getDigestProvider() {
        return digestProvider;
    }

    public MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance(service.getAlgorithm(),
                    digestProvider.getProvider());
        } catch ( NoSuchAlgorithmException nsae ) {
            throw new InvalidDigestServiceException(digestProvider.getName(),
                    this.getName(), nsae);
        }
    }

    public int compareTo( DigestService ds ) {
        return service.getAlgorithm().compareTo(ds.service.getAlgorithm());
    }

    @Override
    public boolean equals( Object o ) {
        if ( !(o instanceof DigestService) ) {
            return false;
        }
        return o.toString().equals(getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return service.getAlgorithm();
    }
}

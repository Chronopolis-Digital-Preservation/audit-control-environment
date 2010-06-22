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
// $Id: DigestProvider.java 3192 2010-06-22 16:54:09Z toaster $

package edu.umiacs.ace.digest;

import java.security.Provider;
import java.security.Provider.Service;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mmcgann
 */
public class DigestProvider {

    private static final String DIGEST_SERVICE_TYPE = "MessageDigest";
    private Provider provider;
    private Map<String, DigestService> digestServices =
            new HashMap<String, DigestService>();

    DigestProvider( Provider provider ) {
        this.provider = provider;

        Set<Service> services = provider.getServices();
        for ( Service service : services ) {
            if ( service.getType().equals(DIGEST_SERVICE_TYPE) ) {
                digestServices.put(service.getAlgorithm(),
                        new DigestService(this, service));
            }
        }
    }

    public String getName() {
        return provider.getName();
    }

    public String getClassName() {
        return provider.getClass().getName();
    }

    public double getVersion() {
        return provider.getVersion();
    }

    public Set<DigestService> getServices() {
        return Collections.unmodifiableSet(
                new HashSet<DigestService>(digestServices.values()));
    }

    public DigestService getService( String name ) {
        DigestService digestService = digestServices.get(name);
        if ( digestService == null ) {
            throw new InvalidDigestServiceException(getName(), name);
        }
        return digestService;
    }

    public boolean hasService( String name ) {
        return digestServices.get(name) != null;
    }

    @Override
    public String toString() {
        return provider.getName();
    }

    Provider getProvider() {
        return provider;
    }
}

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
package edu.umiacs.ace.digest;

import edu.umiacs.util.Check;
import edu.umiacs.util.Strings;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author @@Author
 */
public class DigestFactory {

    private Map<String, DigestProvider> digestProviders =
            new LinkedHashMap<String, DigestProvider>();
    private static DigestFactory instance = null;

    private DigestFactory() {
    }

    public static DigestFactory getInstance() {
        synchronized ( DigestFactory.class ) {
            if ( instance == null ) {
                instance = new DigestFactory();
            }
        }
        return instance;
    }

    public void registerProvider( String className ) {
        Check.notEmpty("className", className);
        try {
            Provider provider =
                    (Provider) Class.forName(className).newInstance();
            DigestProvider dp = new DigestProvider(provider);
            if ( dp.getServices().size() == 0 ) {
                throw new ProviderRegistrationException(
                        "No digest services provided by class", className);
            }
            if ( digestProviders.containsKey(dp.getName()) ) {
                throw new ProviderRegistrationException(
                        "Provider already registered", className);
            }
            addProvider(dp);
        } catch ( ClassCastException cce ) {
            throw new ProviderRegistrationException(
                    "Class is not a subclass of java.security.Provider",
                    className);
        } catch ( ClassNotFoundException cnfe ) {
            throw new ProviderRegistrationException(
                    "Digest provider class not found", className, cnfe);
        } catch ( InstantiationException ie ) {
            throw new ProviderRegistrationException(
                    "Digest provider failed to instantiate", className, ie);
        } catch ( IllegalAccessException iae ) {
            throw new ProviderRegistrationException(
                    "Digest provider failed to instantiate", className, iae);
        }
    }

    public List<DigestProvider> getProviders() {
        return Collections.unmodifiableList(
                new ArrayList<DigestProvider>(digestProviders.values()));
    }

    public DigestProvider getProvider( String name ) {
        Check.notEmpty("name", name);
        DigestProvider dp = digestProviders.get(name);
        if ( dp == null ) {
            throw new InvalidDigestProviderException(name);
        }
        return dp;
    }

    public DigestService getService( String providerName, String serviceName ) {
        if ( Strings.isEmpty(providerName) ) {
            return getService(serviceName);
        }

        DigestProvider dp = getProvider(providerName);
        return dp.getService(serviceName);
    }

    public DigestService getService( String serviceName ) {
        for ( DigestProvider provider : digestProviders.values() ) {
            if ( provider.hasService(serviceName) ) {
                return provider.getService(serviceName);
            }
        }
        throw new InvalidDigestServiceException(serviceName);
    }

    private void addProvider( DigestProvider digestProvider ) {
        if ( digestProvider.getServices().size() != 0 ) {
            digestProviders.put(digestProvider.getName(), digestProvider);
        }
    }
}

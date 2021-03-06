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
package edu.umiacs.ace.driver;

import edu.umiacs.ace.driver.benchmark.BenchmarkAccess;
import edu.umiacs.ace.driver.irods.IrodsAccess;
import edu.umiacs.ace.driver.localfile.LocalFileAccess;
import edu.umiacs.ace.driver.srb.SrbAccess;
import edu.umiacs.ace.driver.swap.SwapFileAccess;
import edu.umiacs.ace.monitor.core.Collection;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory to register all storage resources supported by this monitor.
 *  New storage types should be added in the storage block listed below.
 * 
 * @author toaster
 */
public final class StorageDriverFactory {

    /**
     * Map to track descriptive storage type name to implementing class
     */
    private static final Map<String, Class<? extends StorageDriver>> implementationMap =
            new HashMap<>();

    /**
     * All available storage gateways must be registered here
     */
    static {
        implementationMap.put("irods", IrodsAccess.class);
        implementationMap.put("local", LocalFileAccess.class);
        implementationMap.put("srb", SrbAccess.class);
        implementationMap.put("swap", SwapFileAccess.class);
        implementationMap.put("benchmark", BenchmarkAccess.class);
//        implementationMap.put("smb", SmbAccess.class);
    }

    public static final List<String> listResources() {
        List<String> lt = new ArrayList<>(implementationMap.keySet());
        Collections.sort(lt,comp);
        return lt;
    }

    public static final StorageDriver createStorageAccess( Collection c,
            EntityManager em ) {
        try {
            if ( !implementationMap.containsKey(c.getStorage()) ) {
                throw new IllegalArgumentException("No such connection: " + c.getStorage());
            }
            Class<? extends StorageDriver> clazz = implementationMap.get(c.getStorage());

            return clazz.getConstructor(Collection.class, EntityManager.class).newInstance(c,
                    em);

        } catch ( Exception ex ) {
            throw new RuntimeException(ex);
        }
    }

    private static final Comparator<String> comp = new Comparator<String>() {

        public int compare(String o1, String o2) {
            if ("local".equals(o1))
                return -1;
            if ("local".equals(o2))
                return 1;
            return o1.compareTo(o2);
        }
    };
}

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

import edu.umiacs.ace.monitor.audit.AuditIterable;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 *
 * @author toaster
 */
public abstract class StorageDriver {

    private Collection collection;

    public StorageDriver( Collection c ) {
        this.collection = c;
    }

    public Collection getCollection() {
        return collection;
    }

    /**
     * This should set the parameters for a given instance. The Map is the 
     * map returned from HttpRequest.getParameters();
     * Map<String,String[]>
     * 
     * @param m
     */
    public abstract void setParameters( Map m );

    public abstract String checkParameters( Map m, String path );

    public abstract void remove( EntityManager em );

    /**
     * Return the url of the page that will support the form this collection
     * 
     * @return relative url of page to handle settings or null if none
     */
    public abstract String getPage();

    public abstract AuditIterable<FileBean> getWorkList( String digestAlgorithm, PathFilter filter,
            MonitoredItem... startPathList );

    public abstract InputStream getItemInputStream( String itemPath ) throws IOException;
    //public abstract boolean itemExists(String relativePath);
}

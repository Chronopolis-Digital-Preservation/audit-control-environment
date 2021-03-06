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

package edu.umiacs.ace.driver.cifs;

import edu.umiacs.ace.driver.AuditIterable;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.driver.StorageDriver;
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
public class SmbAccess extends StorageDriver {

    private EntityManager em;

    public SmbAccess( Collection c, EntityManager em ) {
        super(c);
        this.em = em;
    }

    @Override
    public void setParameters( Map m ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String checkParameters( Map m, String path ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove( EntityManager em ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AuditIterable<FileBean> getWorkList( String digestAlgorithm,
            PathFilter filter, MonitoredItem... startPathList ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getItemInputStream( String itemPath ) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

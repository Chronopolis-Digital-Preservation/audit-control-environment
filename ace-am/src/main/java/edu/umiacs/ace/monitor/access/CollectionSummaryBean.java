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
package edu.umiacs.ace.monitor.access;

import edu.umiacs.ace.monitor.audit.AuditThread;
import edu.umiacs.ace.monitor.audit.AuditThreadFactory;
import edu.umiacs.ace.monitor.audit.AuditTokens;
import edu.umiacs.ace.monitor.core.Collection;

/**
 *
 * @author toaster
 */
public class CollectionSummaryBean {

    private Collection collection;
    private long totalFiles;
    private long missingTokens;
    private long missingFiles;
    private long activeFiles;
    private long corruptFiles;
    private long invalidDigests;
    private long totalErrors;
    private long totalSize;
    private long remoteMissing;
    private long remoteCorrupt;

    public void setCollection( Collection collection ) {
        this.collection = collection;
    }

    public Collection getCollection() {
        return collection;
    }

    /**
     * return current running thread if any. 
     * 
     * @return current thread, null if none
     */
    public AuditThread getFileAuditThread() {
        return AuditThreadFactory.getThread(collection);
    }

    public AuditTokens getTokenAuditThread() {
        return AuditTokens.getThread(collection);
    }

    public boolean isQueued() {
        return AuditThreadFactory.isQueued(collection);
    }

    public boolean isFileAuditRunning() {
        return AuditThreadFactory.isRunning(collection);
    }

    public boolean isTokenAuditRunning() {
        return AuditTokens.isRunning(collection);
    }

    public void setTotalFiles( long totalFiles ) {
        this.totalFiles = totalFiles;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public void setTotalErrors( long totalErrors ) {
        this.totalErrors = totalErrors;
    }

    public long getTotalErrors() {
        return totalErrors;
    }

    public long getMissingTokens() {
        return missingTokens;
    }

    public void setMissingTokens( long missingTokens ) {
        this.missingTokens = missingTokens;
    }

    public long getMissingFiles() {
        return missingFiles;
    }

    public void setMissingFiles( long missingFiles ) {
        this.missingFiles = missingFiles;
    }

    public long getActiveFiles() {
        return activeFiles;
    }

    public void setActiveFiles( long activeFiles ) {
        this.activeFiles = activeFiles;
    }

    public long getCorruptFiles() {
        return corruptFiles;
    }

    public void setCorruptFiles( long corruptFiles ) {
        this.corruptFiles = corruptFiles;
    }

    public long getInvalidDigests() {
        return invalidDigests;
    }

    public void setInvalidDigests( long invalidDigests ) {
        this.invalidDigests = invalidDigests;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize( long totalSize ) {
        this.totalSize = totalSize;
    }

    public void setRemoteCorrupt( long remoteCorrupt ) {
        this.remoteCorrupt = remoteCorrupt;
    }

    public void setRemoteMissing( long remoteMissing ) {
        this.remoteMissing = remoteMissing;
    }

    public long getRemoteCorrupt() {
        return remoteCorrupt;
    }

    public long getRemoteMissing() {
        return remoteMissing;
    }
}

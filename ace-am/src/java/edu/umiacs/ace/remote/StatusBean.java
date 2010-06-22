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

package edu.umiacs.ace.remote;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.codehaus.jackson.annotate.JsonUseDeserializer;

/**
 *
 * @author toaster
 */
public class StatusBean implements Serializable {

    private List<CollectionBean> collections;
    private boolean paused;

    /**
     * @return the collections
     */
    public List<CollectionBean> getCollections() {
        return collections;
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * @param collections the collections to set
     */
    public void setCollections( List<CollectionBean> collections ) {
        this.collections = collections;
    }

    @JsonUseDeserializer(CustomBooleanDeserializer.class)
    public void setPaused( boolean paused ) {
        this.paused = paused;
    }

    public static class CollectionBean implements Serializable {

        private long id;
        private String name;
        private String group;
        private String directory;
        private Date lastSync;
        private String storage;
        private int checkPeriod;
        private boolean proxyData;
        private boolean auditTokens;
        private char state;
        private long totalFiles;
        private boolean fileAuditRunning;
        private boolean tokenAuditRunning;
        private FileAudit fileAudit;
        private TokenAudit tokenAudit;
        private long totalSize;
        private long totalErrors;
        private long missingTokens;
        private long missingFiles;
        private long activeFiles;
        private long corruptFiles;
        private long invalidDigests;
        private long remoteMissing;
        private long remoteCorrupt;

        /**
         * @return the id
         */
        public long getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId( long id ) {
            this.id = id;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize( long totalSize ) {
            this.totalSize = totalSize;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName( String name ) {
            this.name = name;
        }

        /**
         * @return the group
         */
        public String getGroup() {
            return group;
        }

        /**
         * @param group the group to set
         */
        public void setGroup( String group ) {
            this.group = group;
        }

        /**
         * @return the directory
         */
        public String getDirectory() {
            return directory;
        }

        /**
         * @param directory the directory to set
         */
        public void setDirectory( String directory ) {
            this.directory = directory;
        }

        /**
         * @return the lastSync
         */
        public Date getLastSync() {
            return lastSync;
        }

        /**
         * @param lastSync the lastSync to set
         */
        @JsonUseDeserializer(CustomDateDeserializer.class)
        public void setLastSync( Date lastSync ) {
            this.lastSync = lastSync;
        }

        /**
         * @return the storage
         */
        public String getStorage() {
            return storage;
        }

        /**
         * @param storage the storage to set
         */
        public void setStorage( String storage ) {
            this.storage = storage;
        }

        /**
         * @return the checkPeriod
         */
        public int getCheckPeriod() {
            return checkPeriod;
        }

        /**
         * @param checkPeriod the checkPeriod to set
         */
        public void setCheckPeriod( int checkPeriod ) {
            this.checkPeriod = checkPeriod;
        }

        /**
         * @return the proxyData
         */
        public boolean isProxyData() {
            return proxyData;
        }

        /**
         * @param proxyData the proxyData to set
         */
        public void setProxyData( boolean proxyData ) {
            this.proxyData = proxyData;
        }

        /**
         * @return the auditTokens
         */
        public boolean isAuditTokens() {
            return auditTokens;
        }

        /**
         * @param auditTokens the auditTokens to set
         */
        public void setAuditTokens( boolean auditTokens ) {
            this.auditTokens = auditTokens;
        }

        /**
         * @return the state
         */
        public char getState() {
            return state;
        }

        /**
         * @param state the state to set
         */
        public void setState( char state ) {
            this.state = state;
        }

        /**
         * @return the totalFiles
         */
        public long getTotalFiles() {
            return totalFiles;
        }

        /**
         * @param totalFiles the totalFiles to set
         */
        public void setTotalFiles( long totalFiles ) {
            this.totalFiles = totalFiles;
        }

        /**
         * @return the fileAuditRunning
         */
        public boolean isFileAuditRunning() {
            return fileAuditRunning;
        }

        /**
         * @param fileAuditRunning the fileAuditRunning to set
         */
        public void setFileAuditRunning( boolean fileAuditRunning ) {
            this.fileAuditRunning = fileAuditRunning;
        }

        /**
         * @return the tokenAuditRunning
         */
        public boolean isTokenAuditRunning() {
            return tokenAuditRunning;
        }

        /**
         * @param tokenAuditRunning the tokenAuditRunning to set
         */
        public void setTokenAuditRunning( boolean tokenAuditRunning ) {
            this.tokenAuditRunning = tokenAuditRunning;
        }

        public FileAudit getFileAudit() {
            return fileAudit;
        }

        public void setFileAudit( FileAudit fileAudit ) {
            this.fileAudit = fileAudit;
        }

        public TokenAudit getTokenAudit() {
            return tokenAudit;
        }

        public void setTokenAudit( TokenAudit tokenAudit ) {
            this.tokenAudit = tokenAudit;
        }

        /**
         * @return the totalErrors
         */
        public long getTotalErrors() {
            return totalErrors;
        }

        /**
         * @param totalErrors the totalErrors to set
         */
        public void setTotalErrors( long totalErrors ) {
            this.totalErrors = totalErrors;
        }

        /**
         * @return the missingTokens
         */
        public long getMissingTokens() {
            return missingTokens;
        }

        /**
         * @param missingTokens the missingTokens to set
         */
        public void setMissingTokens( long missingTokens ) {
            this.missingTokens = missingTokens;
        }

        /**
         * @return the missingFiles
         */
        public long getMissingFiles() {
            return missingFiles;
        }

        /**
         * @param missingFiles the missingFiles to set
         */
        public void setMissingFiles( long missingFiles ) {
            this.missingFiles = missingFiles;
        }

        /**
         * @return the activeFiles
         */
        public long getActiveFiles() {
            return activeFiles;
        }

        /**
         * @param activeFiles the activeFiles to set
         */
        public void setActiveFiles( long activeFiles ) {
            this.activeFiles = activeFiles;
        }

        /**
         * @return the corruptFiles
         */
        public long getCorruptFiles() {
            return corruptFiles;
        }

        /**
         * @param corruptFiles the corruptFiles to set
         */
        public void setCorruptFiles( long corruptFiles ) {
            this.corruptFiles = corruptFiles;
        }

        /**
         * @return the invalidDigests
         */
        public long getInvalidDigests() {
            return invalidDigests;
        }

        /**
         * @param invalidDigests the invalidDigests to set
         */
        public void setInvalidDigests( long invalidDigests ) {
            this.invalidDigests = invalidDigests;
        }

        /**
         * @return the remoteMissing
         */
        public long getRemoteMissing() {
            return remoteMissing;
        }

        /**
         * @param remoteMissing the remoteMissing to set
         */
        public void setRemoteMissing( long remoteMissing ) {
            this.remoteMissing = remoteMissing;
        }

        /**
         * @return the remoteCorrupt
         */
        public long getRemoteCorrupt() {
            return remoteCorrupt;
        }

        /**
         * @param remoteCorrupt the remoteCorrupt to set
         */
        public void setRemoteCorrupt( long remoteCorrupt ) {
            this.remoteCorrupt = remoteCorrupt;
        }

        public static class FileAudit implements Serializable {
            // if fileauditrunning

            private long totalErrors;
            private long newFilesFound;
            private long filesSeen;
            private long tokensAdded;
            private String lastFileSeen;

            /**
             * @return the totalErrors
             */
            public long getTotalErrors() {
                return totalErrors;
            }

            /**
             * @param totalErrors the totalErrors to set
             */
            public void setTotalErrors( long totalErrors ) {
                this.totalErrors = totalErrors;
            }

            /**
             * @return the newFilesFounr
             */
            public long getNewFilesFound() {
                return newFilesFound;
            }

            /**
             * @param newFilesFounr the newFilesFounr to set
             */
            public void setNewFilesFound( long newFilesFounr ) {
                this.newFilesFound = newFilesFounr;
            }

            /**
             * @return the filesSeen
             */
            public long getFilesSeen() {
                return filesSeen;
            }

            /**
             * @param filesSeen the filesSeen to set
             */
            public void setFilesSeen( long filesSeen ) {
                this.filesSeen = filesSeen;
            }

            /**
             * @return the tokensAdded
             */
            public long getTokensAdded() {
                return tokensAdded;
            }

            /**
             * @param tokensAdded the tokensAdded to set
             */
            public void setTokensAdded( long tokensAdded ) {
                this.tokensAdded = tokensAdded;
            }

            public void setLastFileSeen( String lastFileSeen ) {
                this.lastFileSeen = lastFileSeen;
            }

            public String getLastFileSeen() {
                return lastFileSeen;
            }
        }

        public static class TokenAudit implements Serializable {

            private long totalErrors;
            // if token auditrunning; (totalErrors from above)
            private long tokensSeen;
            private long validTokens;

            /**
             * @return the totalErrors
             */
            public long getTotalErrors() {
                return totalErrors;
            }

            /**
             * @param totalErrors the totalErrors to set
             */
            public void setTotalErrors( long totalErrors ) {
                this.totalErrors = totalErrors;
            }

            /**
             * @return the tokensSeen
             */
            public long getTokensSeen() {
                return tokensSeen;
            }

            /**
             * @param tokensSeen the tokensSeen to set
             */
            public void setTokensSeen( long tokensSeen ) {
                this.tokensSeen = tokensSeen;
            }

            /**
             * @return the validTokens
             */
            public long getValidTokens() {
                return validTokens;
            }

            /**
             * @param validTokens the validTokens to set
             */
            public void setValidTokens( long validTokens ) {
                this.validTokens = validTokens;
            }
        }
    }
}

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

package edu.umiacs.ace.monitor.log;

/**
 * Enumeration of all possible log types used in LogEvent
 * 
 * @author toaster
 */
public enum LogEnum {

    LOG_TYPE_UNKNOWN(0, "Unknown", "Unknown Log Entry Type"),
    /**
     * New file registered into the system
     */
    FILE_NEW(1, "New File", "New file added for auditing"),
    /**
     * Previously registered  file no longer exists
     */
    FILE_MISSING(2, "File Missing", "File could not be accessed for auditing"),
    /**
     * file checksums do not match
     */
    FILE_CORRUPT(3, "Corrupt File", "File's checksum does not match stored checksum"),
    /**
     * Found a previously missing file and marked it intact
     */
    FILE_ONLINE(4, "File Online", "Previously offline file is available"),
    ADD_TOKEN(5, "Add Token", "New token from IMS added for this file"),
    CREATE_TOKEN_ERROR(6, "Create Token Error", "Error creating token for this file"),
    MISSING_TOKEN(7, "Missing Token", "File is registered, but has no token"),
    REMOVE_ITEM(8, "Item Removed", "File or directory and tokens removed from monitoring"),
    TOKEN_INVALID(9, "Digest Token Mismatch", "Possibly corrupt checksum or proof, local hash with proof does not match IMS CSI"),
    SITE_UNACCESSABLE(10, "Collection Unaccessable", "Unable to connect to collection for auditing"), //TODO
    /**
     * Error reading file from local storage
     */
    ERROR_READING(11, "Read Error", "Error reading the file locally, check local file"),
    /**
     * unknown error returned from ims
     */
    UNKNOWN_IMS_COMMUNICATION_ERROR(12, "IMS Error", "Unkown error trying to communicate with the IMS, check IMS logs"),
    /**
     * token state changed from invalid token to valid, this should be VERY VERY rare
     */
    TOKEN_VALID(13, "Token Digest Revalidated", "hash and proof match CSI"),
    REMOTE_FILE_CORRUPT(14, "Remote File Corrupt", "remote file digest differs from local file"),
    REMOTE_FILE_MISSING(15, "Remote File Missing", "not corresponding file on remote site"),
    REMOTE_FILE_ONLINE(16, "Remote File Online", "Remote file marked as active"),
    REMOVE_STORAGE_DRIVER(17, "Remove Storage Driver", "Configured storage driver was removed"),
    //STORAGE_DRIVER_ADDED(18, "Add Storage Driver", "New Storage Driver configured"),
    COLLECTION_REGISTERED(19,"Collection Registered", "Collection registered for monitoring"),
    /////////// audit start/stops
    /**
     * Start a synchronization run on a master site
     */
    FILE_AUDIT_START(20, "File Audit Start", "Auditing of this collection's files started"),
    /**
     * Finish a sync run on a master site
     */
    FILE_AUDIT_FINISH(21, "File Audit Finish", "Auditing of this collection's files finished"),
    /**
     * Start a synchronization run on a master site
     */
    TOKEN_AUDIT_START(22, "Token Audit Start", "Auditing of this collection's tokens started"),
    /**
     * Finish a sync run on a master site
     */
    TOKEN_AUDIT_FINISH(23, "Token Audit Finish", "Auditing of this collection's tokens finished"),
    TOKEN_INGEST_UPDATE(24, "Token Ingest Update", "Token was out of date and has been updated"),
    FILE_REGISTER(25, "File Registered", "New file registered but is not ready for auditing"),
    FILE_AUDIT_FALLBACK(26, "File Audit Fallback", "File Audit could not connect to the IMS, falling back to audit-only mode"),
    SMTP_ERROR(27, "SMTP Communication Error", "Could not connect to designated SMTP host"),
    // audit stop errors
    FILE_AUDIT_CANCEL(30, "File Audit Cancel", "Auditing of this collection's files was cancelled"),
    FILE_AUDIT_ABORT(31, "File Audit Aborted", "Auditing of this collection's files was aborted"),

    SYSTEM_ERROR(99, "System Error", "Unknown system error occurred, check server logs");
    private int type;
    private String shortName;
    private String details;

    LogEnum( int i, String shortName, String details ) {
        this.type = i;
        this.shortName = shortName;
        this.details = details;
    }

    public int getType() {
        return type;
    }

    public String getDetails() {
        return details;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * Convert log type to category
     * categories: errors (System Errors), missing (Monitored File Errors), newmaster (New Master Items), sync (Sync Start/Stop Event)
     * @param logType
     * @return
     */
	public String getCategory(int logType) {
		String category = "";
        if (logType == LogEnum.SYSTEM_ERROR.getType()
			    || logType == LogEnum.SITE_UNACCESSABLE.getType()
		        || logType == LogEnum.LOG_TYPE_UNKNOWN.getType()
		        || logType == LogEnum.CREATE_TOKEN_ERROR.getType()
		        || logType == LogEnum.ERROR_READING.getType()
	            || logType == LogEnum.UNKNOWN_IMS_COMMUNICATION_ERROR.getType()) {
		    category = LogEvent.CHOICE_ERRORS;
	    } else if (logType == LogEnum.FILE_MISSING.getType()
			    || logType == LogEnum.FILE_CORRUPT.getType()
		        || logType == LogEnum.MISSING_TOKEN.getType()) {
            category = LogEvent.CHOICE_MISSING;
        } else if (logType == LogEnum.FILE_NEW.getType()
			    || logType == LogEnum.ADD_TOKEN.getType()
		        || logType == LogEnum.FILE_ONLINE.getType()
		        || logType == LogEnum.FILE_REGISTER.getType()) {
            category = LogEvent.CHOICE_NEWMASTER;
        } else if (logType == LogEnum.FILE_AUDIT_FINISH.getType()
	            || logType == LogEnum.FILE_AUDIT_START.getType()
	            || logType == LogEnum.FILE_AUDIT_CANCEL.getType()
                || logType == LogEnum.FILE_AUDIT_ABORT.getType()) {
            category = LogEvent.CHOICE_SYNC;
        }

        return category;
    }

    public static LogEnum valueOf( int i ) {
        switch ( i ) {
            case 0:
                return LOG_TYPE_UNKNOWN;
            case 1:
                return FILE_NEW;
            case 2:
                return FILE_MISSING;
            case 3:
                return FILE_CORRUPT;
            case 4:
                return FILE_ONLINE;
            case 5:
                return ADD_TOKEN;
            case 6:
                return CREATE_TOKEN_ERROR;
            case 7:
                return MISSING_TOKEN;
            case 8:
                return REMOVE_ITEM;
            case 9:
                return TOKEN_INVALID;
            case 10:
                return SITE_UNACCESSABLE;
            case 11:
                return ERROR_READING;
            case 12:
                return UNKNOWN_IMS_COMMUNICATION_ERROR;
            case 13:
                return TOKEN_VALID;
            case 14:
                return REMOTE_FILE_CORRUPT;
            case 15:
                return REMOTE_FILE_MISSING;
            case 16:
                return REMOTE_FILE_ONLINE;
            case 17:
                return REMOVE_STORAGE_DRIVER;
//            case 18:
//                return STORAGE_DRIVER_ADDED;
            case 20:
                return FILE_AUDIT_START;
            case 21:
                return FILE_AUDIT_FINISH;
            case 22:
                return TOKEN_AUDIT_START;
            case 23:
                return TOKEN_AUDIT_FINISH;
            case 24:
                return TOKEN_INGEST_UPDATE;
            case 25:
                return FILE_REGISTER;
            case 26:
                return FILE_AUDIT_FALLBACK;

            case 30:
                return FILE_AUDIT_CANCEL;
            case 31:
                return FILE_AUDIT_ABORT;

            case 99:
                return SYSTEM_ERROR;
        }

        throw new IllegalArgumentException("Int " + i + " does not match any known type");
    }
}

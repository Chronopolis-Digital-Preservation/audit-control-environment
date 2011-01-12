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

import edu.umiacs.util.Argument;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonUseDeserializer;

public class AceItem {

    private long id;
    private char state;
    private boolean directory;
    private String path;
    private String parentPath;
    private Date lastSeen;
    private Date stateChange;
    private Date lastVisited;
    private String fileDigest;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @JsonUseDeserializer(value = CustomLongDeserializer.class)
    public void setId( long id ) {
        this.id = id;
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
    @JsonUseDeserializer(value = CustomCharDeserializer.class)
    public void setState( char state ) {
        this.state = state;
    }

    /**
     * @return the directory
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * @param directory the directory to set
     */
    @JsonUseDeserializer(value = CustomBooleanDeserializer.class)
    public void setDirectory( boolean directory ) {
        this.directory = directory;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath( String path ) {
        this.path = path;
    }

    /**
     * @return the parentPath
     */
    public String getParentPath() {
        return parentPath;
    }

    /**
     * @param parentPath the parentPath to set
     */
    public void setParentPath( String parentPath ) {
        this.parentPath = parentPath;
    }

    /**
     * @return the lastSeen
     */
    public Date getLastSeen() {
        return Argument.dateClone(lastSeen);
    }

    /**
     * @param lastSeen the lastSeen to set
     */
    @JsonUseDeserializer(value = CustomDateDeserializer.class)
    public void setLastSeen( Date lastSeen ) {
        this.lastSeen = Argument.dateClone(lastSeen);
    }

    /**
     * @return the stateChange
     */
    public Date getStateChange() {
        return Argument.dateClone(stateChange);
    }

    /**
     * @param stateChange the stateChange to set
     */
    @JsonUseDeserializer(value = CustomDateDeserializer.class)
    public void setStateChange( Date stateChange ) {
        this.stateChange = Argument.dateClone(stateChange);
    }

    /**
     * @return the lastVisited
     */
    public Date getLastVisited() {
        return Argument.dateClone(lastVisited);
    }

    /**
     * @param lastVisited the lastVisited to set
     */
    @JsonUseDeserializer(value = CustomDateDeserializer.class)
    public void setLastVisited( Date lastVisited ) {
        this.lastVisited = Argument.dateClone(lastVisited);
    }

    /**
     * @return the fileDigest
     */
    public String getFileDigest() {
        return fileDigest;
    }

    /**
     * @param fileDigest the fileDigest to set
     */
    public void setFileDigest( String fileDigest ) {
        this.fileDigest = fileDigest;
    }
}

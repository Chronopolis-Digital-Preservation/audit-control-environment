/*
 * Copyright (c) 2007-@year@, University of Maryland
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
package edu.umiacs.ace.driver;

/**
 *
 * @author toaster
 */
public class DriverStateBean {

    private Thread runningThread;
    private State state;
    private String file;
    private long read;
    private long totalSize;
    private long lastChange;

    public DriverStateBean() {
        state = State.UNKNOWN;
    }

    public void clear() {
        //state = State.UNKNOWN;
        file = null;
        read = -1;
        totalSize = -1;
        lastChange = System.currentTimeMillis();

    }

    public void setRunningThread( Thread runningThread ) {
        this.runningThread = runningThread;
    }

    public Thread getRunningThread() {
        return runningThread;
    }

    public long getIdle() {
        return System.currentTimeMillis() - lastChange;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state the state to set, this will also clear all other values
     */
    public void setStateAndReset( State state ) {
        this.state = state;
        clear();
    }

    public void setState( State state ) {
        this.state = state;
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile( String file ) {
        this.file = file;
    }

    /**
     * @return the read
     */
    public long getRead() {
        return read;
    }

    /**
     * @param read the read to set
     */
    public void setRead( long read ) {
        this.read = read;
    }

    /**
     * @return the totalSize
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * @param totalSize the totalSize to set
     */
    public void setTotalSize( long totalSize ) {
        this.totalSize = totalSize;
    }

    /**
     * @return the lastChange
     */
    public long getLastChange() {
        return lastChange;
    }

    public void updateLastChange() {
        lastChange = System.currentTimeMillis();
    }

    /**
     * @param lastChange the lastChange to set
     */
    public void setLastChange( long lastChange ) {
        this.lastChange = lastChange;
    }

    public enum State {

        IDLE,
        READING,
        LISTING,
        THROTTLE_WAIT,
        UNKNOWN,
        WAITING_ON_FILE,
        OPENING_FILE
    }
}

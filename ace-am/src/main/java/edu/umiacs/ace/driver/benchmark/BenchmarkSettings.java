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

package edu.umiacs.ace.driver.benchmark;

import edu.umiacs.ace.monitor.core.Collection;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author toaster
 */
@Entity
@NamedQuery(name = "BenchmarkSettings.getByCollection", query =
"SELECT i FROM BenchmarkSettings i WHERE i.collection = :coll")
@Table(name = "benchmarksettings")
public class BenchmarkSettings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(nullable = false)
    private Collection collection;
    @Column(nullable = false)
    private int files;
    @Column(nullable = false)
    private int depth;
    @Column(nullable = false)
    private int dirs;
    private boolean readFiles;
    private long fileLength;
    private int blockSize;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals( Object object ) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if ( !(object instanceof BenchmarkSettings) ) {
            return false;
        }
        BenchmarkSettings other = (BenchmarkSettings) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(
                other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.ace.monitor.benchmark.BenchmarkSettings[id=" + id + "]";
    }

    public int getFiles() {
        return files;
    }

    public void setFiles( int files ) {
        this.files = files;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth( int depth ) {
        this.depth = depth;
    }

    public int getDirs() {
        return dirs;
    }

    public void setDirs( int dirs ) {
        this.dirs = dirs;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength( long fileLength ) {
        this.fileLength = fileLength;
    }

    public void setBlockSize( int blockSize ) {
        this.blockSize = blockSize;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setCollection( Collection collection ) {
        this.collection = collection;
    }

    public Collection getCollection() {
        return collection;
    }

    public boolean isReadFiles() {
        return readFiles;
    }

    public void setReadFiles( boolean readFiles ) {
        this.readFiles = readFiles;
    }
}

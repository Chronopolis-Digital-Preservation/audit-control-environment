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

package edu.umiacs.ace.driver.filter;

import edu.umiacs.ace.monitor.core.Collection;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Each collection has a list of patterns that specify what files are to be
 * excluded from a collection.
 * 
 * @author toaster
 */
@Entity
@Table(name = "filter_entry")
@NamedQueries({
    @NamedQuery(name = "FilterEntry.dropByCollection", query =
    "DELETE FROM FilterEntry fe WHERE fe.collection = :coll"),
    @NamedQuery(name = "FilterEntry.listByCollection", query =
    "SELECT fe FROM FilterEntry fe WHERE fe.collection = :coll")
})
public class FilterEntry implements Serializable {

    public static final int ITEM_FILE = 1;
    public static final int ITEM_DIRECTORY = 2;
    public static final int ITEM_ALL = 3;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String regex;
    private int affectedItem = 2;
    @ManyToOne()
    private Collection collection;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public int getAffectedItem() {
        return affectedItem;
    }

    public void setAffectedItem( int affectedItem ) {
        if ( affectedItem < 1 || affectedItem > 3 ) {
            throw new IllegalArgumentException(
                    "Not valid affected item type " + affectedItem);
        }
        this.affectedItem = affectedItem;
    }

    public void setRegex( String regex ) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    public void setCollection( Collection collection ) {
        this.collection = collection;
    }

    public Collection getCollection() {
        return collection;
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
        if ( !(object instanceof FilterEntry) ) {
            return false;
        }
        FilterEntry other = (FilterEntry) object;
        if ( (this.id == null && other.id != null) || (this.id != null && !this.id.equals(
                other.id)) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.umiacs.ace.monitor.filter.FilterEntry[id=" + id + ",regex=" + regex + ",type="
                + affectedItem + "]";
    }
}

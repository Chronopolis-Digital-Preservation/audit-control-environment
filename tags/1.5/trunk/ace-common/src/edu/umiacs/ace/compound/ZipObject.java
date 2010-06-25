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

package edu.umiacs.ace.compound;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.activation.DataSource;

/**
 *
 * @author toaster
 */
public class ZipObject implements CompoundObject {

    private DataSource ds;

    public ZipObject( DataSource ds ) {
        this.ds = ds;
    }

    public Iterator<ObjectEntry> iterator() {
        return new ZipIterator();
    }

    class ZipIterator implements Iterator<ObjectEntry> {

        byte data[] = new byte[2048];
        ZipInputStream zin;
        ObjectEntry oe;
        int index = 0;
        private MessageDigest digest;

        public ZipIterator() {
            try {
                digest = MessageDigest.getInstance("SHA-256");
                zin = new ZipInputStream(ds.getInputStream());
                readNext();
            } catch ( NoSuchAlgorithmException ex ) {
                throw new RuntimeException(
                        "Error creating digest " + ds.getName(), ex);
            } catch ( IOException ex ) {
                throw new RuntimeException(
                        "Error opening zip file " + ds.getName(), ex);
            }
        }

        public boolean hasNext() {
            return oe != null;
        }

        public ObjectEntry next() {
            ObjectEntry previous = oe;

            try {
                readNext();
            } catch ( IOException ioe ) {
                throw new RuntimeException(ioe);
            }

            return previous;
        }

        private void readNext() throws IOException {
            ZipEntry entry = zin.getNextEntry();
            int count;

            if ( entry == null ) {
                if ( zin != null ) {
                    zin.close();
                    zin = null;
                }
                oe = null;
                return;
            }

            oe = new ObjectEntry();
            oe.setOffset(-1);
            oe.setSize(entry.getSize());
            oe.setName(entry.getName());
            oe.setIndex(index);

            digest.reset();
            while ( (count = zin.read(data)) != -1 ) {
                digest.update(data, 0, count);

            }
            oe.setDigest(digest.digest());
            index++;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported");
        }
    }
}

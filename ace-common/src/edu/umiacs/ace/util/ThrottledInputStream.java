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

package edu.umiacs.ace.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author toaster
 */
public class ThrottledInputStream extends FilterInputStream {

    private long maxBps;
    private long bytes;
    private long start;
    private byte[] oneByte = new byte[1];
    private double sleepTime;

    public ThrottledInputStream( InputStream in, long maxBps, double startPause ) {
        super(in);
        this.maxBps = maxBps;
        bytes = 0;
        start = System.currentTimeMillis();
        this.sleepTime = startPause;
    }

    @Override
    public int read() throws IOException {
        read(oneByte, 0, 1);
        return oneByte[0];
    }

    public double getSleepTime() {
        return sleepTime;
    }

    @Override
    public int read( byte[] b, int off, int len ) throws IOException {
        int read = in.read(b, off, len);
        bytes += read;

        long elapsed = System.currentTimeMillis() - start;
        elapsed = (elapsed == 0 ? 1 : elapsed);
        double bps = bytes * 1000L / elapsed;


        if ( maxBps > 0 && bps > maxBps ) {
            sleepTime = 1 + sleepTime * 1.05;

        } else if ( maxBps > 0 ) {
            sleepTime = sleepTime * .95;
        }

        if ( sleepTime > 0 && maxBps > 0 ) {
            try {
//                System.out.println("Sleeping for " + sleepTime
//                        + " bps " + bps + " elapsed " + elapsed
//                        + " bytes " + bytes + " length " + len + " maxBPS " + maxBps);
                Thread.sleep((long) sleepTime);
            } catch ( InterruptedException ignore ) {
                Thread.currentThread().interrupt();
                throw new IOException(ignore);
            }
        }

        return read;
    }
}

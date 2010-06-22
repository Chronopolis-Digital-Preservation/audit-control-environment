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
// $Id: AdditionMessageDigest.java 3192 2010-06-22 16:54:09Z toaster $
package edu.umiacs.ace.digest.debug;

import java.math.BigInteger;
import java.security.MessageDigestSpi;
import java.util.Arrays;

/*******************************************************************************
 *
 *
 * @version {@code $Revision: 3192 $ $Date$}
 *
 ******************************************************************************/
public final class AdditionMessageDigest extends MessageDigestSpi
        implements Cloneable {

    private BigInteger sum = BigInteger.ZERO;
    private int DIGEST_LENGTH = 64;

    protected void engineReset() {
        this.sum = BigInteger.ZERO;
    }

    protected byte[] engineDigest() {
        byte[] digest = new byte[DIGEST_LENGTH];
        byte[] byteSum = sum.toByteArray();
        if ( sum.signum() < 0 ) {
            Arrays.fill(digest, (byte) 255);
        }
        int copyLength = Math.min(DIGEST_LENGTH, byteSum.length);
        System.arraycopy(byteSum, 0, digest, DIGEST_LENGTH - copyLength, copyLength);
        engineReset();
        return digest;
    }

    protected void engineUpdate( byte[] input, int offset, int length ) {
        byte[] byteInput = new byte[length];
        System.arraycopy(input, offset, byteInput, 0, length);
        BigInteger biInput = new BigInteger(byteInput);
        this.sum = this.sum.add(biInput);
    }

    protected void engineUpdate( byte b ) {
        engineUpdate(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        AdditionMessageDigest amd = new AdditionMessageDigest();
        amd.sum = this.sum;
        return this;
    }

    @Override
    protected int engineGetDigestLength() {
        return this.DIGEST_LENGTH;
    }

    @Override
    public String toString() {
        return sum.toString();
    }
}

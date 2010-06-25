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

import edu.umiacs.util.Strings;
import java.math.BigInteger;

/**
 *
 * @author mmcgann
 */
public final class HashValue {

    private HashValue() {
    }

    public static byte[] asBytes( long value ) {
        return BigInteger.valueOf(value).toByteArray();
    }

    public static long asLong( byte[] value ) {
        return new BigInteger(value).longValue();
    }

    public static String asHexString( byte[] value ) {
        String str = new BigInteger(1, value).toString(16);
        if ( str.length() < value.length * 2 ) {
            str = Strings.repeat('0', value.length * 2 - str.length()) + str;
        }
        return str;
    }

    public static byte[] asBytes( String hexValue ) {
//        byte[] a = new BigInteger(hexValue, 16).toByteArray();
//        byte[] b;
//        if ( a.length * 2 == hexValue.length() + 2 )
//        {
//            b = new byte[a.length - 1];
//            System.arraycopy(a, 1, b, 0, b.length);
//        }
//        else
//        {
//            b = a;
//        }
//        return b;
        byte[] bts = new byte[hexValue.length() / 2];
        for ( int i = 0; i < bts.length; i++ ) {
            bts[i] = (byte) Integer.parseInt(hexValue.substring(2 * i, 2 * i + 2), 16);
        }
        return bts;
    }
}

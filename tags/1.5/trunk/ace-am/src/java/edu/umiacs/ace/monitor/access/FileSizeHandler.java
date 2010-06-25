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
package edu.umiacs.ace.monitor.access;

import java.text.DecimalFormat;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

/**
 *
 * @author toaster
 */
public class FileSizeHandler extends SimpleTagSupport {

    private static final String[] units = {" B", " KB", " MB", " GB", " TB", " PB"};
    private long value;
    private char unit;

    /**
     * Called by the container to invoke this tag. 
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();
        long kb = 1 << 10;
        long mb = kb << 10;
        long gb = mb << 10;
        long tb = gb << 10;
        long pb = tb << 10;

        try {
            DecimalFormat df = new DecimalFormat("###,###.#");

            if ( unit > 0 ) {
                switch ( unit ) {
                    case 'b':
                        out.print(value + units[0]);
                        break;
                    case 'k':
                        out.println(df.format((double) value / kb) + units[1]);
                        break;
                    case 'm':
                        out.println(df.format((double) value / mb) + units[2]);
                        break;
                    case 'g':
                        out.println(df.format((double) value / gb) + units[3]);
                        break;
                    case 't':
                        out.println(df.format((double) value / tb) + units[4]);
                        break;
                    case 'p':
                        out.println(df.format((double) value / pb) + units[5]);
                        break;
                }
            } else {
                long testval = value;
                long div = 1;
                for ( int i = 0; i < 6; i++ ) {
                    if ( (testval >>>= 10) == 0 ) {
                        out.println(df.format((double) value / div) + units[i]);
                        i = 6;
                    }
                    div <<= 10;
                }
            }

        } catch ( java.io.IOException ex ) {
            throw new JspException(ex.getMessage());
        }

    }

    public void setValue( long value ) {
        this.value = value;
    }

    public void setUnit( char unit ) {
        this.unit = unit;
    }
}

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
// $Id: LogTypeHandler.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.monitor.log;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

/**
 * Tag to print out the string name of a log given its numeric value.
 * If verbose is set, then the the details of the enum value are printed.
 * 
 * @author toaster
 */
public class LogTypeHandler extends SimpleTagSupport {

    private int type;
    private boolean verbose = false;

    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();

        try {

            out.println(LogEnum.valueOf(type));
            if ( verbose ) {
                out.println(" - " + LogEnum.valueOf(type).getDetails());
            }
            JspFragment f = getJspBody();
            if ( f != null ) {
                f.invoke(out);
            }

        } catch ( java.io.IOException ex ) {
            throw new JspException(ex.getMessage());
        }

    }

    public void setVerbose( boolean verbose ) {
        this.verbose = verbose;
    }

    public void setType( int type ) {
        this.type = type;
    }
}

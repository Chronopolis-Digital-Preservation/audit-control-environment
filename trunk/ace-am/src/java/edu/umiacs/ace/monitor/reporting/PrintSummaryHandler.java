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
// $Id: PrintSummaryHandler.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.monitor.reporting;

import edu.umiacs.util.Strings;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Tag to output the value of a report item given the report and
 * the item
 * 
 * @author toaster
 */
public class PrintSummaryHandler extends SimpleTagSupport {

    private ReportSummary summary;
    private String attribute;

  
    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();
        boolean found = false;

        try {

            if ( Strings.isEmpty(attribute) || summary == null ) {
                out.print(0);
            } else {
                for ( ReportItem ri : summary.getSummaryItems() ) {
                    if ( attribute.equals(ri.getAttribute()) ) {
                        found = true;
                        out.print(ri.getValue());
                    }
                }
                if ( !found ) {
                    out.print(0);
                }
            }

            JspFragment f = getJspBody();
            if ( f != null ) {
                f.invoke(out);
            }
        } catch ( java.io.IOException ex ) {
            throw new JspException(ex.getMessage());
        }

    }

    public void setSummary( ReportSummary summary ) {
        this.summary = summary;
    }

    public void setAttribute( String attribute ) {
        this.attribute = attribute;
    }
}

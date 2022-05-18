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

package edu.umiacs.ace.monitor.log;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * Tag to print out the category of a log given its numeric value.
 * 
 * @author lsitu
 */
public class LogCategoryHandler extends SimpleTagSupport {

    private int type;

    @Override
    public void doTag() throws JspException, IOException {

        JspWriter out = getJspContext().getOut();

        try {
        	LogEnum logType = LogEnum.valueOf(type);
            out.println(getCategoryLabel(logType.getCategory(logType.getType())));

        } catch ( Exception ex ) {
        	out.println("Error converting log type " + type + " to category: " + ex.getMessage());
        } finally {
            JspFragment f = getJspBody();
            if ( f != null ) {
                f.invoke(out);
            }
        }

    }

    public void setType( int type ) {
        this.type = type;
    }

    private String getCategoryLabel(String category) {
        String label = "";

        switch (category) {
            case LogEvent.CHOICE_ERRORS:
                label = "System Errors";
                break;
            case LogEvent.CHOICE_MISSING:
                label = "Monitored File Errors";
                break;       
            case LogEvent.CHOICE_NEWMASTER:
                label = "New Master Items";
                break;
            case LogEvent.CHOICE_SYNC:
                label = "Sync Start/Stop Events";
                break;
            case LogEvent.CHOICE_REMOVALS:
                label = "Removals";
                break;
            default:
                label = category;
                break;
        }
 
        return label;
    }
}

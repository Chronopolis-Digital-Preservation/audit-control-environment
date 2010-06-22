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

package edu.umiacs.ace.monitor.access.browse;

import edu.umiacs.ace.monitor.access.browse.DirectoryTree.DirectoryNode;
import java.io.IOException;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;
import org.apache.log4j.Logger;

/**
 * Tag to support expanding/folding a tree
 * @author toaster
 */
public class DirectoryTreeHandler extends SimpleTagSupport {

    private static final Logger LOG = Logger.getLogger(DirectoryTreeHandler.class);
    private Object node;
    private String var;
    private JspFragment beginFragment;
    private JspFragment endFragment;

    /**
     * Called by the container to invoke this tag. 
     * The implementation of this method is provided by the tag library developer,
     * and handles all tag processing, body iteration, etc.
     */
    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();

        if ( !(node instanceof DirectoryNode) ) {
            throw new JspException("node is not DirectoryNode");
        }

        try {

// process first to let child tags set begin/end fragments
            JspFragment f = getJspBody();
            if ( f != null ) {
                f.invoke(out);
            }

//                try
//                {
            printNode((DirectoryNode) node);
//                }
//                catch ( Exception e )
//                {
//                    LOG.error("Exception printing node: ", e);
//                    throw new JspException(e);
//                }


        } catch ( IOException ex ) {
            throw new JspException(ex.getMessage());
        }

    }

    public void setNode( Object node ) {
        this.node = node;
    }

    public void setVar( String var ) {
        this.var = var;
    }

    void setEndFragment( JspFragment endFragment ) {
        this.endFragment = endFragment;
    }

    void setBeginFragment( JspFragment beginFragment ) {
        this.beginFragment = beginFragment;
    }

    private void printNode( DirectoryNode dn ) throws JspException, IOException {

        if ( beginFragment != null ) {
            getJspContext().setAttribute(var, dn);
            beginFragment.invoke(getJspContext().getOut());
        }

        if ( dn.isExpanded() ) {
            for ( DirectoryNode child : dn.getChildren() ) {

                printNode(child);

            }

        }

        if ( endFragment != null ) {
            getJspContext().setAttribute(var, dn);
            endFragment.invoke(getJspContext().getOut());
        }


    }
}

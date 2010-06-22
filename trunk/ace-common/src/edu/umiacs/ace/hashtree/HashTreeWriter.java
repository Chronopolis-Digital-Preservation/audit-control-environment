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
// $Id: HashTreeWriter.java 3192 2010-06-22 16:54:09Z toaster $

package edu.umiacs.ace.hashtree;

import edu.umiacs.ace.hashtree.HashTree.Node;
import edu.umiacs.ace.util.HashValue;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author toaster
 */
public class HashTreeWriter {

    /**
     * Write an xml representation of the supplied hash tree to an output stream
     * 
     * @param tree
     * @param os
     */
    public static void writeXmlTree( HashTree tree, OutputStream os, boolean ignoreLeafs ) throws IOException {
        os.write("<?xml version=\"1.0\"?>\n".getBytes());
        Node node = tree.getRootNode();
        writeXmlNode(node, os, ignoreLeafs);
        os.close();
    }

    private static void writeXmlNode( Node node, OutputStream os, boolean ignoreLeafs ) throws IOException {
        os.write("<node><digest>".getBytes());
        os.write(HashValue.asHexString(node.getHash()).getBytes());
        os.write("</digest>".getBytes());
        if ( node.getChildren() != null && node.getChildren().size() > 0 ) {

            os.write("<children>".getBytes());
            for ( Node child : node.getChildren() ) {
                writeXmlNode(child, os, ignoreLeafs);
            }
            os.write("</children>".getBytes());
        }

        os.write("</node>".getBytes());
    }
}

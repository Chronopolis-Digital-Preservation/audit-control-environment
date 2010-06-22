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
// $Id: HashTreeFormatter.java 3192 2010-06-22 16:54:09Z toaster $
package edu.umiacs.ace.hashtree.util;

import edu.umiacs.ace.hashtree.HashTree;
import edu.umiacs.util.Check;
import edu.umiacs.util.Strings;
import java.math.BigInteger;

/*******************************************************************************
 *
 * Utility class for creating a readable string representation of a 
 * {@code HashTree}.
 *
 * @see HashTree
 * @author University of Maryland, Institute for Advanced Computer Studies
 * @version {@code $Revision: 3192 $ $Date$}
 *
 ******************************************************************************/
public class HashTreeFormatter {

    /**
     * Default constructor.
     */
    public HashTreeFormatter() {
    }

    /**
     * Returns a readable string representation of a hash tree. 
     *
     * @param tree hash tree to create the representation of
     * @return string representation
     * @throws IllegalArgumentException if {@code tree} is null.
     */
    public String format( HashTree tree ) {
        Check.notNull("tree", tree);

        StringBuilder sb = new StringBuilder();
        if ( !Strings.isEmpty(tree.getProvider()) ) {
            sb.append(tree.getProvider() + ": ");
        }
        sb.append(tree.getAlgorithm() + "\n");
        formatNode(sb, 0, tree.getRootNode());
        return sb.toString();
    }

    private void formatNode( StringBuilder sb, int depth, HashTree.Node node ) {
        sb.append(Strings.repeat(' ', depth * 3));
        sb.append(new BigInteger(node.getHash()) + "[" + node.getIndex() + "]");
        sb.append("\n");
        for ( HashTree.Node child : node.getChildren() ) {
            formatNode(sb, depth + 1, child);
        }
    }
}

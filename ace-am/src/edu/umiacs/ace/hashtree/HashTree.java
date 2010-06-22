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
// $Id: HashTree.java 3192 2010-06-22 16:54:09Z toaster $
package edu.umiacs.ace.hashtree;

import edu.umiacs.ace.util.HashValue;
import edu.umiacs.util.Check;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/*******************************************************************************
 *
 * A hash tree (or Merkle tree).
 *
 * <p>
 * Use the {@code HashTreeBuilder} to create an instance of a {@code HashTree}.
 * A suitable description of a hash tree can found at the Wikipedia article
 * found in in the link below. 
 * </p>
 *
 * <p>
 * The {@code getRootNode} method can be used to traverse the entire tree and
 * the {@code getLeafNodes} method can be used to traverse a path from a leaf
 * node to the tree root. Use the {@code proof} method to generate a proof 
 * which can be used to validate a hash against the entire tree. See the 
 * {@code Proof} class description for more information. 
 * </p>
 *
 * <p>
 * An instance of a hash tree is immutable. 
 * </p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Hash_tree"><code>Hash Tree 
 * (Wikipedia)</code></a>
 * @see HashTreeBuilder
 * @see HashTree.Node
 * @see Proof
 * @author University of Maryland, Institute for Advanced Computer Studies
 * @version {@code $Revision: 3192 $ $Date$}
 *
 ******************************************************************************/
public final class HashTree {

    /**
     * Root node of the tree
     */
    private Node root;
    /**
     * Leaf nodes of the tree
     */
    private List<Node> leafNodes;
    private Node linkNode;
    /**
     * Name of the hash algorithm used to create the hash tree
     */
    private String algorithm;
    /**
     * Name of the provider that supplied the hash algorithm implementation.
     */
    private String provider;

    private HashTree() {
    }

    /**
     * Used by the {@code HashTreeBuilder} to create a hash tree.
     *
     * @param algorithm name of the hash algorithm used to create the tree.
     * @param provider name of the provider that supplied the hash algorithm
     * implementation. If a registered provider did not supply the 
     * implementation, this value must be an empty string. 
     * @param root root node of the tree
     * @param leafNodes of the tree
     */
    HashTree( String algorithm, String provider, Node root,
            List<Node> leafNodes, Node linkNode ) {
        this.algorithm = algorithm;
        this.provider = provider;
        this.root = root;
        this.leafNodes = Collections.unmodifiableList(leafNodes);
        this.linkNode = linkNode;
    }

    /**
     * Root node of the tree.
     *
     * @return root node which can be used to traverse the entire tree. 
     */
    public Node getRootNode() {
        return this.root;
    }

    /**
     * Leaf nodes of the tree.
     *
     * @return an immutable list which contains all of the leaf nodes of the
     * tree in order from left to right. This list can be used to traverse
     * a path from a leaf node to the root.
     */
    public List<Node> getLeafNodes() {
        return this.leafNodes;
    }

    public Node getLinkNode() {
        return linkNode;
    }

    /**
     * Hash algorithm name.
     *
     * @return the name of the hash algorithm used to generate the tree. 
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Provider name of the hash algorithm.
     *
     * @return the name of the security provider that supplied the hash 
     * algorithm implementation used to generate the tree. If the implementation
     * was not registered with a security provider, this method will return 
     * the empty string {@code ""}.
     */
    public String getProvider() {
        return provider;
    }

    public Proof proof( int leafIndex ) {
        return proof(leafNodes.get(leafIndex));
    }

    public Proof proof( byte[] leafHash ) {
        Check.notNull("leafHash", leafHash);

        for ( Node node : leafNodes ) {
            if ( Arrays.equals(leafHash, node.getHash()) ) {
                return proof(node);
            }
        }
        throw new IllegalArgumentException("hash is not a leaf of this tree "
                + HashValue.asHexString(leafHash));
    }

    /**
     * Generate a proof for a given leafNode node in the hash tree. 
     *
     * List of proof nodes starts with p[0] the leaf level and p[n] the hashes 
     * required to regenerate the root node (ie, hashes from depth 1 of the tree)
     * 
     * On a side note, this means that in a tree by the HashTreeBuilder with a
     * linking node, the linking Hash will be the supplied hash for p[n] and the 
     * generated hash the root hash of items supplied for that round
     * 
     * @param leafNode a leaf node of this tree used to create the proof. 
     * @return proof object used to validate a leaf hash against this tree. 
     * @throws IllegalArgumentException if {@code leafNode} is null or is not
     * a leaf node of this tree. 
     * @see Proof
     */
    public Proof proof( HashTree.Node leafNode ) {
        Check.notNull("leafNode", leafNode);
        if ( !leafNodes.contains(leafNode) ) {
            throw new IllegalArgumentException("Node is not a leaf of this " + "tree: "
                    + leafNode.toString());
        }

        // To build the proof, start at the parent node of the leaf node.
        // This will be the 'current' node being worked on. Also keep track 
        // of what the child index of the leaf node is. 
        List<ProofNode> proofNodes = new LinkedList<ProofNode>();
        HashTree.Node current = leafNode.getParent();
        int lastIndex = leafNode.getIndex();

        // Iterate through all of the current node's children to create a 
        // ProofNode object. Do not add the child we just traversed from 
        while ( current != null ) {
            List<ProofHash> proofHashes =
                    new ArrayList<ProofHash>(current.getChildren().size());
            for ( HashTree.Node child : current.getChildren() ) {
                if ( child.getIndex() != lastIndex ) {
                    ProofHash ph = new ProofHash(child.getHash(),
                            child.getIndex());
                    proofHashes.add(ph);
                }
            }
            ProofNode proofNode = new ProofNode(proofHashes, lastIndex);
            proofNodes.add(proofNode);

            // Keep track of the index of this node so it isn't added in the
            // proof list on the level above, and then move up to the next 
            // level. 
            lastIndex = current.getIndex();
            current = current.getParent();
        }
        Proof proof = new Proof(algorithm, provider, leafNode.getHash(),
                proofNodes);
        return proof;
    }

    /**
     * Compact string representation useful for debugging. Use
     * {@code HashTreeFormatter} for a string representation that is more
     * readable. 
     *
     * @return string representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringNode(sb, getRootNode());
        return sb.toString();
    }

    private void toStringNode( StringBuilder sb, Node node ) {
        BigInteger hash = new BigInteger(node.getHash());
        sb.append(hash.toString() + "[" + node.getIndex() + "]");
        if ( node.getChildren().size() > 0 ) {
            sb.append("(");
            for ( int i = 0; i < node.getChildren().size(); i++ ) {
                if ( i != 0 ) {
                    sb.append(",");
                }
                toStringNode(sb, node.getChildren().get(i));
            }
            sb.append(")");
        }
    }

    /**
     * Node of a hash tree. 
     *
     * <p>
     * The node contains the hash value, a reference to its parent node, 
     * a list of references to its children, and the index of this node in the
     * children list of the parent node. 
     * <p>
     *
     * <p>
     * This class is immutable.
     * </p>
     * 
     * @see HashTree
     * @author University of Maryland, Institute for Advanced Computer Studies
     * @version {@code $Revision: 3192 $ $Date$}
     */
    public final static class Node {

        private byte[] hash;
        private Node parent;
        private List<Node> children;
        private int index;
        private Object data = null;

        Node() {
        }

        Node( byte[] hash ) {
            this.hash = hash;
        }

        /**
         * Hash value for this node.
         *
         * @return a copy of the supplied hash value if this is a leaf node, 
         * otherwise a copy of the computed hash value for this node.
         */
        public byte[] getHash() {
            return Arrays.copyOf(hash, hash.length);
        }

        void setHash( byte[] hash ) {
            this.hash = hash;
        }

        /**
         * Parent of this node.
         *
         * @return the reference to this node's parent, or null if this is 
         * the root node.
         */
        public Node getParent() {
            return parent;
        }

        void setParent( Node parent ) {
            this.parent = parent;
        }

        /**
         * Children of this node.
         *
         * @return an immutable list of nodes that are the children of this 
         * node in order from left to right. All children nodes are guaranteed 
         * to have their {@code getIndex()} method return the same value as the
         * ordinal index in this list. If this is a leaf node, this returns an 
         * empty list.
         */
        public List<Node> getChildren() {
            return children;
        }

        void setChildren( List<Node> children ) {
            this.children = Collections.unmodifiableList(children);
        }

        /**
         * Child index of this node.
         *
         * @return the index of this node in the children list of the parent
         * node. If this node is the root, it returns zero.
         */
        public int getIndex() {
            return this.index;
        }

        void setIndex( int index ) {
            this.index = index;
        }

        public Object getData() {
            return this.data;
        }

        void setData( Object data ) {
            this.data = data;
        }
    }
}

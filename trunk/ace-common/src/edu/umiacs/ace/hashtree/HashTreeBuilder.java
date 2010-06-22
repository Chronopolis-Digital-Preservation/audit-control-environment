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
package edu.umiacs.ace.hashtree;

import edu.umiacs.util.Check;
import java.security.MessageDigest;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*******************************************************************************
 *
 * Builds a hash tree given the leaf nodes. 
 *
 * <p>
 * Use this builder to create a {@code HashTree}. The constructor requires a
 * {@code MessageDigest} instance that will be used to build the hashes in 
 * the tree and the order specifies the minimum number of children per node.
 * The builder assumes ownership of the {@code MessageDigest} object and once
 * constructed, the {@code MessageDigest} cannot be used externally.  
 * Add leaves to the tree by calling {@code add} and then create the tree
 * with {@code build}. 
 * </p>
 * 
 * <p>
 * For any tree level, if the number of nodes {@code (n)} with a tree order 
 * {@code (d)} is {@code n % d = 0}, then the number of children per node in 
 * the parent level will be {@code d}. If {@code n % d != 0}, the extra nodes
 * will be distributed across the level starting with the first parent node. 
 * For each parent node, an extra {@code ceiling((n % d) / (n / d))} nodes 
 * are added to each parent node until there are no extra nodes left. Any 
 * leftover nodes still remaining when constructing the final parent node are 
 * added to the final parent node. 
 * </p>
 *
 * <p>
 * This class is not thread-safe and external locking must be used if the tree 
 * is being built by multiple threads. The build method may be called multiple 
 * times to create intermediate trees or to create variations by changing the 
 * tree order. 
 * </p>
 *
 * @see HashTree
 * @author University of Maryland, Institute for Advanced Computer Studies
 * @version {@code $Revision$ $Date$}
 *
 ******************************************************************************/
public final class HashTreeBuilder {

    /**
     * Minimum number of children per node
     */
    private int order;
    /**
     * Digest algorithm to use in constructing the tree
     */
    private MessageDigest digest;
    /**
     * Leaf nodes provided by hashes in the add method
     */
    private List<HashTree.Node> leafNodes = new ArrayList<HashTree.Node>();
    private HashTree.Node linkNode = null;
    /**
     * Immutable empty list used for the children list for the leaf nodes
     */
    private static final List<HashTree.Node> emptyNodeList =
            Collections.emptyList();

    private HashTreeBuilder() {
    }

    /**
     * Creates a builder with a tree order of 2.
     *
     * @param digest digest algorithm to use when creating hashes in the 
     * tree. This object assumes ownership of the digest object and the 
     * digest object cannot be used externally after this class is constructed.
     * @throws IllegalArgumentException if {@code digest} is null
     */
    public HashTreeBuilder( MessageDigest digest ) {
        this(digest, 2);
    }

    /**
     * Creates a builder with a given tree order. 
     *
     * @param digest digest algorithm to use when creating hashes in the 
     * tree. This object assumes ownership of the digest object and the digest
     * object cannot be used externally after this class is constructed. 
     * @param order tree order
     * @throws IllegalArgumentException if {@code digest} is null or 
     * {@code order <= 1}.
     */
    public HashTreeBuilder( MessageDigest digest, int order ) {
        Check.notNull("digest", digest);
        this.setOrder(order);
        this.digest = digest;
    }

    /**
     * Adds a leaf hash to the tree. 
     * 
     * @param hash leaf hash value. 
     * @throws IllegalArgumentException if {@code hash} is null.
     */
    public void add( byte[] hash ) {
        add(hash, null);
    }

    public void add( byte[] hash, Object data ) {
        Check.notNull("hash", hash);

        HashTree.Node node = new HashTree.Node();
        node.setHash(hash);
        node.setData(data);
        node.setChildren(emptyNodeList);
        leafNodes.add(node);
    }

    /**
     * Link this hash tree to a previous hash tree. Essentially seeding the 
     * root val
     * @param hash
     */
    public void link( byte[] hash ) {
        if ( hash == null || hash.length == 0 ) {
            linkNode = null;
        } else {
            linkNode = new HashTree.Node();
            linkNode.setHash(hash);
            linkNode.setChildren(emptyNodeList);
        }
    }

    public List<HashTree.Node> getLeafNodes() {
        return Collections.unmodifiableList(leafNodes);
    }

    /**
     * Changes the tree order used when constructing the tree.
     *
     * @param order tree order to be used when constructing the tree.
     * @throws IllegalArgumentException if {@code order <= 1}
     */
    public void setOrder( int order ) {
        if ( order <= 1 ) {
            throw new IllegalArgumentException("Invalid order: " + order);
        }
        this.order = order;
    }

    /**
     * The tree order used when constructing the tree.
     *
     * @return tree order, the minumum number of children per node.
     */
    public int getOrder() {
        return order;
    }

    /**
     * Number of leaf nodes added to the builder
     *
     * @return the number of leaf nodes added with the {@code add} method.
     */
    public int size() {
        return leafNodes.size();
    }

    /**
     * Builds a hash tree. root of tree is either 
     * 1. tree formed by leaf nodes only if linkNode is null
     * 2. root from leaf nodes combined with linkNode if linkNode is not null
     *
     * @return hash tree built with the leaf hashes added with the {@code add}
     * method.
     * @throws IllegalStateException if no leaves have been added to the
     * builder.
     */
    public HashTree build() {
        if ( leafNodes.isEmpty() ) {
            throw new IllegalStateException(
                    "HashTreeBuilder has no leaf nodes");
        }

        HashTree.Node baseNode = buildLevel(leafNodes);
        HashTree.Node rootNode = null;
        if ( linkNode != null ) {
            linkNode.setIndex(0);
            baseNode.setIndex(1);
            digest.reset();
            digest.update(linkNode.getHash());
            byte[] rootHash = digest.digest(baseNode.getHash());
            rootNode = new HashTree.Node();
            rootNode.setIndex(0);
            rootNode.setHash(rootHash);
            rootNode.setChildren(Arrays.asList(linkNode, baseNode));
            linkNode.setParent(rootNode);
            baseNode.setParent(rootNode);
        } else {
            rootNode = baseNode;
        }
        Provider p = digest.getProvider();
        String providerName = (p == null) ? "" : p.getName();
        return new HashTree(digest.getAlgorithm(), providerName, rootNode,
                leafNodes, linkNode);
    }

    public MessageDigest getMessageDigest() {
        return digest;
    }

    private HashTree.Node buildLevel( List<HashTree.Node> level ) {
        // Tree is built one level at a time starting with the leaf nodes and
        // this method is recursively called to build each level. Passed in is 
        // the last completed level, first time this is the leaf nodes. If
        // this level only has one node, the root has been created and return 
        // the completed hash tree
        int size = level.size();
        if ( size == 1 ) {
            return level.get(0);
        }

        // Find out how many nodes are going to be in the parent level
        int parentNodeCount = size / order;
        List<HashTree.Node> parentLevel =
                new ArrayList<HashTree.Node>(parentNodeCount);

        // Index for iterating through the nodes in this level
        int iterIndex = 0;

        // If the number of nodes in this level is not a multiple of the 
        // tree order, the last nodes in the level won't fit the minimum
        // required number of children from a node. Find out how many extra
        // nodes there are.
        int remainderNodes = size % order;

        // These extra nodes will be redistributed as evenly as possible. Find
        // out how many additional children per node that should be added
        int additionalChildrenPerNode = (int) Math.ceil(remainderNodes / (double) parentNodeCount);

        // Each iteration builds a new parent node
        while ( iterIndex < size ) {
            int childrenNodeCount = 0;
            // Check to see if this will be the final parent node
            if ( size - iterIndex >= order * 2 ) {
                // If not, this parent node will have the tree order number
                // of nodes, plus any remaining additional nodes that need
                // to be distributed
                childrenNodeCount = order;
                if ( remainderNodes > 0 ) {
                    childrenNodeCount += additionalChildrenPerNode;
                    remainderNodes -= additionalChildrenPerNode;
                }
            } else {
                // If the final parent node, it gets all of the remaining
                // nodes on this level
                childrenNodeCount = size - iterIndex;
            }

            // Construct the parent node
            List<HashTree.Node> childrenNodes =
                    new ArrayList<HashTree.Node>(childrenNodeCount);
            HashTree.Node parentNode = new HashTree.Node();

            for ( int i = iterIndex, childIndex = 0;
                    i < iterIndex + childrenNodeCount; i++, childIndex++ ) {
                HashTree.Node thisNode = level.get(i);
                thisNode.setParent(parentNode);
                thisNode.setIndex(childIndex);
                digest.update(thisNode.getHash());
                childrenNodes.add(thisNode);
            }

            parentNode.setHash(digest.digest());
            parentNode.setChildren(childrenNodes);
            parentLevel.add(parentNode);
            iterIndex += childrenNodeCount;
        }
        return buildLevel(parentLevel);
    }
}

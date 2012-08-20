/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.ws;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author mmcgann
 */
public class ProofElement implements Serializable
{
    private int index;
    private List<String> hashes;

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public List<String> getHashes()
    {
        return hashes;
    }

    public void setHashes(List<String> hashes)
    {
        this.hashes = hashes;
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.util;

import org.junit.Test;

/**
 *
 * @author mmcgann
 */
public class HashValueTest {

    @Test
    public void test()
    {
        String s = "deadbeef";
        byte[] b = HashValue.asBytes(s);
        System.out.println("B len: " + b.length);
        s = HashValue.asHexString(b);
        System.out.println(s);
//        assertEquals("deadbeef", s);
    }
    

}
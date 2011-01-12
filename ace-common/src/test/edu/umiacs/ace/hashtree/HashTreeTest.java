/*
 * HashTreeBuilderTest.java
 * JUnit based test
 *
 * Created on September 11, 2007, 11:43 AM
 */

package edu.umiacs.ace.hashtree;

import edu.umiacs.ace.digest.DigestFactory;
import edu.umiacs.ace.hashtree.util.HashTreeFormatter;
import edu.umiacs.ace.hashtree.util.ProofFormatter;
import edu.umiacs.util.Strings;
import java.math.BigInteger;
import java.security.MessageDigest;
import junit.framework.*;

/**
 *
 * @author mmcgann
 */
public class HashTreeTest extends TestCase
{
    
    public HashTreeTest(String testName)
    {
        super(testName);
    }

    
    public void testAdd()
    {
    }
    
    public void testBuild() throws Exception
    {
        DigestFactory df = DigestFactory.getInstance();
        df.registerProvider("edu.umiacs.ace.digest.debug.DebugProvider");
        MessageDigest md = df.getService("Addition").createMessageDigest();
        
        HashTreeBuilder htb = new HashTreeBuilder(md, 2);
        HashTreeFormatter htf = new HashTreeFormatter();
        ProofFormatter pf = new ProofFormatter();
        ProofValidator pv = new ProofValidator();
        
        HashTree ht;
        Proof proof;
        byte[] sum;
        
        try
        {
            htb.build();
            fail();
        }
        catch ( IllegalStateException ise )
        {}
                
        htb.add(BigInteger.valueOf(1).toByteArray());
        htb.add(BigInteger.valueOf(2).toByteArray());
        htb.add(BigInteger.valueOf(3).toByteArray());
        htb.add(BigInteger.valueOf(4).toByteArray());
        ht = htb.build();
        System.out.println(Strings.repeat('=', 79));
        System.out.println(ht.toString());
        System.out.println(htf.format(ht));
        assertEquals("10[0](3[0](1[0],2[1]),7[1](3[0],4[1]))",
                ht.toString());
        
        proof = ht.proof(ht.getLeafNodes().get(0));
        System.out.println(proof.toString());
        System.out.println(pf.format(proof));
        assertEquals("1:0(2[1]),0(7[1])", proof.toString());
        sum = pv.rootHash(md, proof);
        assertEquals(10, new BigInteger(sum).intValue());
        
        proof = ht.proof(ht.getLeafNodes().get(2));
        System.out.println(proof.toString());
        System.out.println(pf.format(proof));
        assertEquals("3:0(4[1]),1(3[0])", proof.toString());
        sum = pv.rootHash(md, proof);
        assertEquals(10, new BigInteger(sum).intValue());
        

        htb.link(BigInteger.valueOf(20).toByteArray());
        ht = htb.build();
        System.out.println(Strings.repeat('=', 79));
        System.out.println(ht.toString());
        System.out.println(htf.format(ht));
        assertEquals("30[0](20[0],10[1](3[0](1[0],2[1]),7[1](3[0],4[1])))", 
                ht.toString());
        
        proof = ht.proof(ht.getLeafNodes().get(0));
        System.out.println(proof.toString());
        System.out.println(pf.format(proof));
        assertEquals("1:0(2[1]),0(7[1]),1(20[0])", proof.toString());
        
        htb.link(null);
        htb.add(BigInteger.valueOf(5).toByteArray());
        ht = htb.build();
        System.out.println(Strings.repeat('=', 79));
        System.out.println(ht.toString());
        System.out.println(htf.format(ht));
        assertEquals("15[0](6[0](1[0],2[1],3[2]),9[1](4[0],5[1]))",
                ht.toString());
        
        proof = ht.proof(ht.getLeafNodes().get(1));
        System.out.println(proof.toString());
        System.out.println(pf.format(proof));
        assertEquals("2:1(1[0],3[2]),0(9[1])", proof.toString());
        sum = pv.rootHash(md, proof);
        assertEquals(15, new BigInteger(sum).intValue());
        
        htb.add(BigInteger.valueOf(6).toByteArray());
        ht = htb.build();
        System.out.println(Strings.repeat('=', 79));
        System.out.println(ht.toString());
        System.out.println(htf.format(ht));
        assertEquals("21[0](3[0](1[0],2[1]),7[1](3[0],4[1]),11[2](5[0],6[1]))",
                ht.toString());
        proof = ht.proof(ht.getLeafNodes().get(5));
        System.out.println(proof.toString());
        System.out.println(pf.format(proof));
        assertEquals("6:1(5[0]),2(3[0],7[1])", proof.toString());
        sum = pv.rootHash(md, proof);
        assertEquals(21, new BigInteger(sum).intValue());
        
        htb.setOrder(3);
        ht = htb.build();
        System.out.println(Strings.repeat('=', 79));
        System.out.println(ht.toString());
        System.out.println(htf.format(ht));
        assertEquals("21[0](6[0](1[0],2[1],3[2]),15[1](4[0],5[1],6[2]))",
                ht.toString());
        proof = ht.proof(ht.getLeafNodes().get(4));
        System.out.println(proof.toString());
        System.out.println(pf.format(proof));
        assertEquals("5:1(4[0],6[2]),1(6[0])", proof.toString());
        
        htb.add(BigInteger.valueOf(7).toByteArray());
        htb.add(BigInteger.valueOf(8).toByteArray());
        ht = htb.build();
        System.out.println(Strings.repeat('=', 79));
        System.out.println(ht.toString());
        System.out.println(htf.format(ht));
        assertEquals("36[0](10[0](1[0],2[1],3[2],4[3]),26[1](5[0],6[1],7[2],8[3]))",
                ht.toString());
        
        proof = ht.proof(ht.getLeafNodes().get(0));
        System.out.println(proof.toString());
        System.out.println(pf.format(proof));
        assertEquals("1:0(2[1],3[2],4[3]),0(26[1])", proof.toString());
        
        htb.setOrder(2);
        ht = htb.build();
        System.out.println(Strings.repeat('=', 79));
        System.out.println(ht.toString());
        System.out.println(htf.format(ht));        
        assertEquals("36[0](10[0](3[0](1[0],2[1]),7[1](3[0],4[1])),26[1](11[0](5[0],6[1]),15[1](7[0],8[1])))", 
                ht.toString());
        
        proof = ht.proof(ht.getLeafNodes().get(4));
        System.out.println(proof.toString());
        System.out.println(pf.format(proof));
        assertEquals("5:0(6[1]),0(15[1]),1(10[0])", proof.toString());
        
    }
    
}

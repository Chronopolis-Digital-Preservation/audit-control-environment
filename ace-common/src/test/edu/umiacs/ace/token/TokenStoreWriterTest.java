package edu.umiacs.ace.token;

import edu.umiacs.ace.digest.DigestFactory;
import edu.umiacs.ace.hashtree.HashTree;
import edu.umiacs.ace.hashtree.HashTreeBuilder;
import edu.umiacs.ace.hashtree.Proof;
import edu.umiacs.ace.hashtree.ProofBuilder;
import edu.umiacs.ace.hashtree.ProofValidator;
import edu.umiacs.ace.hashtree.util.HashTreeFormatter;
import edu.umiacs.ace.hashtree.util.ProofFormatter;
import edu.umiacs.util.Strings;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author toaster
 */
public class TokenStoreWriterTest {

//    private AceToken sampleToken;
    private TokenStoreWriter instance;

    public TokenStoreWriterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {


        instance = new AceTokenWriter(System.out);
    }

    public static final AceToken createSampleToken() {
        AceToken sampleToken;
        sampleToken = new AceToken();
        sampleToken.setDate(new Date());
        sampleToken.setIms("ims.umiacs.umd.edu");
        sampleToken.setImsService("SHA-256-0");
        sampleToken.setRound(54678);

        sampleToken.setProof(createTestProof());
        sampleToken.setDigestType(sampleToken.getProof().getAlgorithm());
        return sampleToken;
    }

    public static final Proof createTestProof() {
        DigestFactory df = DigestFactory.getInstance();
        if ( !df.isRegistered("edu.umiacs.ace.digest.debug.DebugProvider") ) {
            df.registerProvider("edu.umiacs.ace.digest.debug.DebugProvider");
        }
        MessageDigest md = df.getService("Addition").createMessageDigest();

        HashTreeBuilder htb = new HashTreeBuilder(md, 2);
        HashTreeFormatter htf = new HashTreeFormatter();


        HashTree ht;
        Proof proof;

        htb.add(BigInteger.valueOf(1).toByteArray());
        htb.add(BigInteger.valueOf(2).toByteArray());
        htb.add(BigInteger.valueOf(3).toByteArray());
        htb.add(BigInteger.valueOf(4).toByteArray());
        htb.add(BigInteger.valueOf(5).toByteArray());
        htb.add(BigInteger.valueOf(6).toByteArray());
        htb.add(BigInteger.valueOf(7).toByteArray());
        htb.add(BigInteger.valueOf(8).toByteArray());
        ht = htb.build();


        proof = ht.proof(ht.getLeafNodes().get(0));
        System.out.println("Generated Sample Proof: " + proof);

        ProofValidator pv = new ProofValidator();
//        System.out.println("Asserting proof validates");
        assertTrue(pv.validate(md, proof, ht.getRootNode().getHash()));
        return proof;
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of writeTokenEntry method, of class TokenStoreWriter.
     */
    @Test
    public void testWriteTokenEntry() throws Exception {
        System.out.println("writeTokenEntry");
        try {
            instance.writeTokenEntry();
            fail("null token exception not thrown");
        } catch ( IllegalArgumentException e ) {
            System.out.println(e.getMessage());
        }

        instance.startToken(createSampleToken());
        try {
            instance.writeTokenEntry();
            fail("null null identifier exception not thrown");
        } catch ( IllegalArgumentException e ) {
            System.out.println(e.getMessage());

        }

        instance.addIdentifier("testid");

        instance.writeTokenEntry();
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of close method, of class TokenStoreWriter.
     */
    @Test
    public void testClose() throws Exception {
        System.out.println("close");
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
}

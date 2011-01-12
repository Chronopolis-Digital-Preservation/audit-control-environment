package edu.umiacs.ace.token;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author toaster
 */
public class TokenStoreReaderTest {

//    private TokenStoreReader reader;
    AceToken token;
    byte[] input;
    private static final File inFile = new File("/tmp/store1.txt");

    public TokenStoreReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws IOException {
    }

    @After
    public void tearDown() {
    }

    private void writeCopies( int copies, int ids ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);


        TokenStoreWriter writer = new AceTokenWriter(bos);
        token = TokenStoreWriterTest.createSampleToken();

        for ( int i = 0; i < copies; i++ ) {
            writer.startToken(token);
            for ( int j = 0; j < ids; j++ ) {
                writer.addIdentifier("int " + j);
            }
            writer.writeTokenEntry();
        }

        writer.close();

        input = bos.toByteArray();
        System.out.println("Store size: " + input.length);

    }

    @Test
    public void testEmptyIdentifier() throws IOException {
        System.out.println("----Testing one-entry no ids");
        writeCopies(1, 0);
        long time = System.currentTimeMillis();
        TokenStoreReader reader = new TokenStoreReader(new ByteArrayInputStream(input));
        long span = System.currentTimeMillis() - time;

        assertTrue(reader.hasNext());
        TokenStoreEntry tse = reader.next();

        assertNotNull(tse);
        validateEntry(tse);
        assertFalse(reader.hasNext());

        System.out.println("----Finished testing one-entry no ids file " + span + "ms");
    }

    @Test
    public void testOneRead() throws IOException {
        System.out.println("----Testing one-entry file");
        writeCopies(1, 1);
        long time = System.currentTimeMillis();
        TokenStoreReader reader = new TokenStoreReader(new ByteArrayInputStream(input));
        long span = System.currentTimeMillis() - time;

        assertTrue(reader.hasNext());
        TokenStoreEntry tse = reader.next();

        assertNotNull(tse);
        validateEntry(tse);
        assertFalse(reader.hasNext());

        System.out.println("----Finished testing one-entry file " + span + "ms");
    }

    @Test
    public void testFileRead() throws IOException {
        System.out.println("----Testing real file");
        assertTrue(inFile.canRead());
        long time = System.currentTimeMillis();
        TokenStoreReader reader = new TokenStoreReader(new FileInputStream(inFile));
        long span = System.currentTimeMillis() - time;

        int i = 0;
        time = System.currentTimeMillis();
        for ( TokenStoreEntry tse : reader ) {
            span = span + System.currentTimeMillis() - time;
            i++;
            time = System.currentTimeMillis();
        }
        System.out.println("----Finished reading file " + span + "ms entries:" + i);
    }

    @Test
    public void testFiveRead() throws IOException {
        System.out.println("----Testing five-entry file");

        writeCopies(1000, 1);
        long time = System.currentTimeMillis();
        TokenStoreReader reader = new TokenStoreReader(new ByteArrayInputStream(input));
        long span = System.currentTimeMillis() - time;

        int i = 0;
        time = System.currentTimeMillis();
        for ( TokenStoreEntry tse : reader ) {
            span = span + System.currentTimeMillis() - time;
            validateEntry(tse);
            i++;
            time = System.currentTimeMillis();
        }
        assertEquals(1000, i);
        System.out.println("----Finished testing five-entry file " + span + "ms");
    }

    private void validateEntry( TokenStoreEntry tse ) {

        assertEquals(token.getDate(), tse.getToken().getDate());
        assertEquals(token.getDigestType(), tse.getToken().getDigestType());
        assertEquals(token.getIms(), tse.getToken().getIms());
        assertEquals(token.getImsService(), tse.getToken().getImsService());
        assertEquals(token.getRound(), tse.getToken().getRound());
        assertEquals(token, tse.getToken());
        assertEquals(token.getProof().getProofNodes().size(), tse.getToken().getProof().getProofNodes().size());
        for ( int i = 0; i < token.getProof().getProofNodes().size(); i++ ) {
            assertEquals(token.getProof().getProofNodes().get(i),
                    tse.getToken().getProof().getProofNodes().get(i));
        }
    }
}

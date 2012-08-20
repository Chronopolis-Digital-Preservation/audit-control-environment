/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.ims.ws.ProofElement;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 *    String name;
 *    String tokenClassName;
 *    String digestService;
 *    String digestProvider;
 *    int statusCode;
 *    long roundId;
 *    Date timestamp;
 *    List<ProofElement> proofElements;
 *       int index;
 *       List<String> hashes;
 *
 * This is NOT threadsafe
 *
 * @author toaster
 */
public class TokenResponseWrapper
{

    ByteBuffer bb = ByteBuffer.allocate(1024 * 64);
    private static Charset charset = Charset.forName("UTF-8");
    private CharsetEncoder encoder = charset.newEncoder();
    private CharsetDecoder decoder = charset.newDecoder();

    public byte[] encode(TokenResponse response)
    {
        
        bb.clear();
        writeString(response.getName());
        writeString(response.getTokenClassName());
        writeString(response.getDigestService());
        writeString(response.getDigestProvider());
        bb.putInt(response.getStatusCode());
        bb.putLong(response.getRoundId());
        bb.putLong(response.getTimestamp().getTime());

        if (response.getProofElements() != null)
        {
            List<ProofElement> proofs = response.getProofElements();
            bb.putInt(proofs.size());

            for (ProofElement pe : proofs)
            {
                bb.putInt(pe.getIndex());
                if (pe.getHashes() != null)
                {
                    bb.putInt(pe.getHashes().size());
                    for (String hash : pe.getHashes())
                    {
                        writeString(hash);
                    }
                } else
                {
                    bb.putInt(0);
                }
            }

        } else
        {
            bb.putInt(0);
        }

        bb.flip();
        byte[] newBuf = new byte[bb.remaining()];
        bb.get(newBuf);
        return newBuf;
    }

    public TokenResponse decode(byte[] encoded) throws CharacterCodingException
    {
        ByteBuffer inputBytes = ByteBuffer.wrap(encoded);

        TokenResponse tr = new TokenResponse();
        tr.setName(readString(inputBytes));
        tr.setTokenClassName(readString(inputBytes));
        tr.setDigestService(readString(inputBytes));
        tr.setDigestProvider(readString(inputBytes));
        tr.setStatusCode(inputBytes.getInt());
        tr.setRoundId(inputBytes.getLong());
        tr.setTimestamp(new Date(inputBytes.getLong()));

        int pListSize = inputBytes.getInt();
        List<ProofElement> pList = new ArrayList<ProofElement>(pListSize);
        tr.setProofElements(pList);
        for (int i = 0; i < pListSize; i++)
        {
            ProofElement pe = new ProofElement();
            pe.setIndex(inputBytes.getInt());

            int hListSize = inputBytes.getInt();
            List<String> hList = new ArrayList<String>(hListSize);
            for (int j = 0; j < hListSize; j++)
            {
                hList.add(readString(inputBytes));
            }

            pe.setHashes(hList);
            pList.add(pe);
        }

        return tr;
    }

    private void writeString(String s)
    {
        ByteBuffer startBuffer = bb.duplicate();

        bb.putInt(0);
        if (s == null || s.equals("")) 
        {
            return;
        }

        encoder.reset();
        encoder.encode(CharBuffer.wrap(s), bb, true);
        encoder.flush(bb);

        int strLen = bb.position() - startBuffer.position() - Integer.SIZE / 8;
        startBuffer.putInt(strLen);

    }

    private String readString(ByteBuffer inBytes) throws
            CharacterCodingException
    {
        int strLen = inBytes.getInt();
        if (strLen == 0)
        {
            return "";
        }
        ByteBuffer strBuf = inBytes.duplicate();
        strBuf.limit(strBuf.position() + strLen);
        inBytes.position(inBytes.position() + strLen);
        decoder.reset();
        return decoder.decode(strBuf).toString();

    }
}

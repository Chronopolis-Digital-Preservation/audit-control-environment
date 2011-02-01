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
package edu.umiacs.ace.token;

import edu.umiacs.ace.token.TokenLoadException.ErrorType;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.util.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author toaster
 */
public class TokenStoreReader implements Iterable<TokenStoreEntry>, Iterator<TokenStoreEntry> {

    private Pattern headerPattern =
            Pattern.compile("^([\\w-]+)\\s+([\\w\\.]+)\\s+([\\w-]+)\\s+(\\d+)\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?[-\\+]{1}\\d{2}:\\d{2})\\s+(\\d+)$");
    private BufferedReader input;
    private TokenStoreEntry next;
    private AceTokenBuilder builder = new AceTokenBuilder();

    public TokenStoreReader( InputStream input ) throws IOException {
        init(input, "UTF-8");
    }

    public TokenStoreReader( InputStream input, String charset ) throws IOException {
        init(input, charset);
    }

    private void init( InputStream input, String charset ) throws IOException {
        this.input = new BufferedReader(new InputStreamReader(input, charset));
        next = loadNext();
    }

    public Iterator<TokenStoreEntry> iterator() {
        return this;
    }

    public boolean hasNext() {
        return next != null;
    }

    public TokenStoreEntry next() {
        TokenStoreEntry tok = this.next;
        next = loadNext();
        return tok;
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove Not Supported");
    }

    private TokenStoreEntry loadNext() {

        TokenStoreEntry entry = new TokenStoreEntry();

        String header;
        try {

            header = input.readLine();
        } catch ( IOException e ) {
            throw new TokenLoadException(e);
        }

        if ( header == null ) {
            return null;
        }

        if ( Strings.isEmpty(header) ) {
            throw new TokenLoadException(ErrorType.IOEXCEPTION, "Header line empty");
        }

        Matcher m = headerPattern.matcher(header);
        if ( !m.matches() ) {
            throw new TokenLoadException(ErrorType.HEADERFORMATERROR, header);
        }

        String digest = m.group(1);
        String ims = m.group(2);
        String imsService = m.group(3);
        long round = Long.parseLong(m.group(4));
        String date = m.group(5);
        int length = Integer.parseInt(m.group(7));


        builder.setIms(ims);
        builder.setDigestAlgorithm(digest);
        builder.setDate(parseDate(date)); 
        builder.setImsService(imsService);
        builder.setRound(round);

        // Read identifiers and proof
        List<String> idList = new ArrayList<String>(5);
        String identifier;
        try {
            while ( !Strings.isEmpty(identifier = input.readLine()) ) {
                idList.add(identifier);
            }
        } catch ( IOException e ) {

            //TODO: should we forward to next extry
            throw new TokenLoadException(e);
        }

        String proofLine;
        TokenLoadException delayedError = null;
        try {
            while ( !Strings.isEmpty(proofLine = input.readLine()) ) {
                String[] proofList = proofLine.split(":");
                if ( delayedError == null && (proofList.length <= 1 || proofList.length > 3) ) {
                    delayedError = new TokenLoadException(ErrorType.PROOFFORMATERROR, proofLine);
                }

//                System.out.println("Parsing level " + proofLine + " "+proofList.length);
                builder.startProofLevel(proofList.length);
                for ( int i = 0; i < proofList.length; i++ ) {
//                                        System.out.println("testing " + proofList[i] + " " + i);

                    
                    if ( Strings.isEmpty(proofList[i]) ) {
                        //TOOD: error:
                    }

                    if ( proofList[i].equals("X") ) {
//                        System.out.println("setting idx " + i);
                        builder.setLevelInheritIndex(i);
                    } else {
                        byte[] hashBlock = HashValue.asBytes(proofList[i]);
//                        System.out.println("adding hash " + proofList[i]);
                        builder.addLevelHash(hashBlock);
                    }
                }

            }
        } catch ( IOException e ) {
            //TODO: should we forward to next extry
            throw new TokenLoadException(e);
        }

        if ( delayedError != null ) {
            throw delayedError;
        }

        entry.setToken(builder.createToken());
        entry.setIdentifiers(idList);

        return entry;
    }

    private Date parseDate( String dateStr ) {
        try {
            XMLGregorianCalendar xc = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateStr);
            return xc.toGregorianCalendar().getTime();
        } catch ( DatatypeConfigurationException e ) {
            throw new RuntimeException(e);
        }

    }
}

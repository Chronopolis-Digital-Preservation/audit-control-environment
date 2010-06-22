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

package edu.umiacs.ace.ims.api;

import edu.umiacs.ace.ims.ws.ProofElement;
import edu.umiacs.ace.ims.ws.TokenResponse;
import edu.umiacs.ace.util.DateConvert;
import edu.umiacs.ace.util.XMLDateTimeFormat;
import edu.umiacs.util.Check;
import edu.umiacs.util.Strings;
import java.io.OutputStream;
import java.text.DateFormat;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author mmcgann
 */
public class XMLTokenWriter 
{
    private static final String NAMESPACE = 
            "http://umiacs.umd.edu/adapt/ace/token/1.0";
    
    private DateFormat xmlDate = new XMLDateTimeFormat();
    private static XMLOutputFactory xmlOutputFactory = null;
    
    public XMLTokenWriter()
    {
    }
    
    public void write(OutputStream stream, TokenResponse response)
    {
        Check.notNull("stream", stream);
        Check.notNull("response", response);
        
        synchronized ( XMLTokenWriter.class )
        {
            if ( xmlOutputFactory == null )
            {
                xmlOutputFactory = XMLOutputFactory.newInstance();
            }
        }
        try
        {
            write(xmlOutputFactory.createXMLStreamWriter(stream), response);
        }
        catch ( XMLStreamException xse )
        {
            throw new TokenWriteException("Unable to create writer: " + 
                    xse.getMessage(), xse);
        }
    }
        
    public void write(XMLStreamWriter writer, TokenResponse response)
    {
        XMLStreamWriter xw = Check.notNull("writer", writer);
        Check.notNull("response", response);
        
        try
        {
            xw.writeStartDocument("utf-8", "1.0");
            xw.setDefaultNamespace(NAMESPACE);
            xw.writeStartElement("token");
            xw.writeDefaultNamespace(NAMESPACE);
            
            xw.writeStartElement("token-class");
            xw.writeCharacters(response.getTokenClassName());
            xw.writeEndElement();
           
            xw.writeStartElement("digest-service");
            xw.writeCharacters(response.getDigestService());
            xw.writeEndElement();

            if ( !Strings.isEmpty(response.getDigestProvider()) )
            {
                xw.writeStartElement("digest-provider");
                xw.writeCharacters(response.getDigestProvider());
                xw.writeEndElement();
            }
            
           xw.writeStartElement("name");
           xw.writeCharacters(response.getName());
           xw.writeEndElement();
           
           xw.writeStartElement("round-id");
           xw.writeCharacters(String.valueOf(response.getRoundId()));
           xw.writeEndElement();
           
           xw.writeStartElement("time-stamp");
           xw.writeCharacters(xmlDate.format(
                   DateConvert.toDate(response.getTimestamp())));
           xw.writeEndElement();
           
           xw.writeStartElement("proof");
           for ( ProofElement proofElement: response.getProofElements() )
           {
               xw.writeStartElement("element");
               xw.writeAttribute("index", String.valueOf(
                       proofElement.getIndex()));
               for ( String hash: proofElement.getHashes() )
               {
                   xw.writeStartElement("hash");
                   xw.writeCharacters(hash);
                   xw.writeEndElement();
               }
               xw.writeEndElement(); // element
           }
           xw.writeEndElement(); // proof
           xw.writeEndElement(); // token
           xw.writeEndDocument(); 
           xw.flush(); 
        }
        catch ( XMLStreamException xse )
        {
            throw new TokenWriteException("Unable to write token: " + 
                    xse.getMessage(), xse);
        }
    }
        
}

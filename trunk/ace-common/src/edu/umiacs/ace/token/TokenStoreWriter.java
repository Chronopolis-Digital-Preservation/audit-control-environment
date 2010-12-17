package edu.umiacs.ace.token;

import edu.umiacs.ace.util.HashValue;
import edu.umiacs.util.Check;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * https://wiki.umiacs.umd.edu/adapt/index.php/Ace:TokenStore
 *
 * @author toaster
 */
public abstract class TokenStoreWriter<T> {

    // currentheader info
    private String ims;
    private String imsService;
    private String digestAlgorithm;
    private String formattedDate;
    private long round;
    private LinkedList<String> identifierList = new LinkedList<String>();
    private LinkedList<String> levelList = new LinkedList<String>();
    private OutputStream tokenStore;
    
    public abstract void startToken(T token);


    public TokenStoreWriter( OutputStream tokenStore ) {
        this.tokenStore = tokenStore;
    }

    /**
     * reset all values in prep for next entry
     */
    private void clear()
    {
        digestAlgorithm = null;
        ims = null;
        imsService = null;
        formattedDate = null;
        round = -1;
        identifierList.clear();
        levelList.clear();
    }

    protected final void setHeaderInformation( String digestAlg, String ims, String imsService,
            long round, Object date ) {
        
        Check.notEmpty("Digest Algorithm", digestAlg);
        Check.notEmpty("IMS", ims);
        Check.notEmpty("IMS Service", imsService);
        Check.notNegative("Round", round);
        Check.notNull("Date", date);

        this.ims = ims;
        this.digestAlgorithm = digestAlg;
        this.imsService = imsService;
        this.round = round;
        if ( date instanceof XMLGregorianCalendar ) {
            this.formattedDate = formatDate((XMLGregorianCalendar) date);
        } else if ( date instanceof Date ) {
            this.formattedDate = formatDate((Date) date);
        } else {
            throw new IllegalArgumentException("Unsupported Date type: " + date.getClass());
        }
    }

    protected final void addHashLevel(String s)
    {
        levelList.add(s);
    }

    protected final void addHashLevel(int inheritIdx, byte[] ... hashes) {
        if (hashes == null || hashes.length == 0)
            throw new IllegalArgumentException("Empty hash level");

        Check.notNegative("Inherit Index", inheritIdx);

        StringBuilder level = new StringBuilder();
        int i = 0;

        for (; i < hashes.length; i++)
        {
            if (i == inheritIdx)
            {
                level.append("X:");
            }
            level.append(HashValue.asHexString(hashes[i]));
            if (i != (hashes.length -1))
                level.append(':');
        }
        
        if (inheritIdx == i)
            level.append(":X");

        levelList.add(level.toString());
    }

    protected final void addHashLevel(int inheritIdx, String ... hashes) {
        Check.notNegative("Inherit Index", inheritIdx);

        StringBuilder level = new StringBuilder();
        int i = 0;

        for (; i < hashes.length; i++)
        {
            if (i == inheritIdx)
            {
                level.append("X:");
            }
            level.append(hashes[i]);
            if (i != (hashes.length -1))
                level.append(':');
        }

        if (inheritIdx == i)
            level.append(":X");

        levelList.add(level.toString());
    }

    protected final void addHashLevel(int inheritIdx, List<String> hashes) {
        Check.notNegative("Inherit Index", inheritIdx);

        StringBuilder level = new StringBuilder();
        int i = 0;

        for (; i < hashes.size(); i++)
        {
            if (i == inheritIdx)
            {
                level.append("X:");
            }
            level.append(hashes.get(i));
            if (i != (hashes.size() -1))
                level.append(':');
        }

        if (inheritIdx == i)
            level.append(":X");

        levelList.add(level.toString());
    }

    public void addIdentifier( String identifier ) {
        Check.notEmpty("Identifier", identifier);
        identifierList.add(identifier);
    }

    /**
     * Write a token entry to the output stream according to the tokenstore spec.
     * header\n
     * identifiers\n
     * proof\n
     * 
     * @throws IOException
     */
    public void writeTokenEntry() throws IOException {
        Check.notNegative("IdentifierList Size", identifierList.size());
        Check.notNegative("IdentifierList Size", levelList.size());
        Check.notNegative("Round", round);

        String header = digestAlgorithm + " " + ims + " " + imsService + " "
                + round + " " + formattedDate + " ";

        StringBuilder entryBody = new StringBuilder();
        for ( String identifier : identifierList ) {
            entryBody.append(identifier);
            entryBody.append("\n");
        }

        entryBody.append("\n");

        for ( String level : levelList ) {
            entryBody.append(level);
            entryBody.append("\n");
        }

        entryBody.append("\n");

        byte[] block = entryBody.toString().getBytes("UTF-8");
        header += block.length + "\n";
        tokenStore.write(header.getBytes("UTF-8"));
        tokenStore.write(block);
        clear();        

    }

    public void close() throws IOException {
        tokenStore.close();
    }

    protected static final String formatDate( XMLGregorianCalendar cal ) {
        return cal.toXMLFormat();
    }

    /**
     * handle creating 8601 time
     * 
     * @param d
     * @return formatted date string
     */
    protected static final String formatDate( Date d ) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(d.getTime());
        try {
            DatatypeFactory df = DatatypeFactory.newInstance();
            XMLGregorianCalendar xc = df.newXMLGregorianCalendar(gc);
            return xc.toXMLFormat();
        } catch ( DatatypeConfigurationException e ) {
            throw new RuntimeException(e);
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.ims.IMSParameters;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.witness.Witness;
import edu.umiacs.util.Parameters;
import java.util.Properties;

/**
 *
 * @author toaster
 */
public abstract class WitnessPublisher
{

    private String name;
    private Properties prop;

    public final String getName()
    {
        return name;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    public final Properties getProperties()
    {
        return prop;
    }

    public final void loadParam(Parameters param)
    {
        this.prop = new Properties();
        String prefix = IMSParameters.PROCESSOR_PREFIX + "." + name + ".";

        for ( String key : param.keySet() )
        {
            if ( key.startsWith(prefix) )
            {
                String newKey = key.substring(prefix.length());
                prop.setProperty(newKey, param.getString(key));
            }
        }
    }

    /**
     * Publish the supplied witness value
     * 
     * @param newWitness
     */
    public abstract void publishWitness(Witness newWitness);

    /**
     * Test to see if this publisher is valid for a given token class
     * @param tokenClass
     * @return
     */
    public abstract boolean validForClass(TokenClass tokenClass);
}

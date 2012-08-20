/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.server;

import edu.umiacs.io.IO;
import edu.umiacs.io.ResourceInputStream;
import java.util.Properties;

/**
 *
 * @author mmcgann
 */
public class StartupBanner
{
    static final String VERSION_PROPERTIES = "/META-INF/version.properties";
    
    static final String PROPERTY_VERSION_MAJOR = "version.major";
    static final String PROPERTY_VERSION_MINOR = "version.minor";
    static final String PROPERTY_VERSION_REVISION = "version.revision";
    static final String PROPERTY_VERSION_REPOSITORY = "version.repository";
    static final String PROPERTY_VERSION_DATE = "version.date";
    
    private String banner = "";

    private StartupBanner()
    {
    }

    public StartupBanner(Object classLoaderToUse)
    {
        Properties prop = new Properties();
        ResourceInputStream is = null;
        
        try
        {
            is = new ResourceInputStream(classLoaderToUse, VERSION_PROPERTIES);
            prop.load(is);
            is.close();
            banner = String.format("Version %s.%s.%s (%s %s)", 
                    prop.getProperty(PROPERTY_VERSION_MAJOR, "?"), 
                    prop.getProperty(PROPERTY_VERSION_MINOR, "?"),
                    prop.getProperty(PROPERTY_VERSION_REVISION, "?"),
                    prop.getProperty(PROPERTY_VERSION_DATE, "?"),
                    prop.getProperty(PROPERTY_VERSION_REPOSITORY, "?"));
        }
        // No big deal if the banner can't be loaded
        catch ( Exception e )
        {
            banner = "Banner information failed to load: " + e.getMessage();
        }
        finally
        {
            IO.release(is);
        }
    }

    @Override
    public String toString()
    {
        return banner;
    }
}

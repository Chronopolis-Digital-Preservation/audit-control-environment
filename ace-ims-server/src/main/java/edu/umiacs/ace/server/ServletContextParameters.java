// $Id: ServletContextParameters.java 2250 2008-06-12 17:15:33Z mmcgann $

package edu.umiacs.ace.server;

import edu.umiacs.util.Parameters;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;

/**
 * 
 * @version {@code $Revision: 2250 $ $Date$}
 */
public class ServletContextParameters extends Parameters
{
    public ServletContextParameters(ServletContext ctx)
    {
        @SuppressWarnings("unchecked")
        List<String> names = Collections.list(ctx.getInitParameterNames());
        for ( String name: names )
        {
            this.put(name, ctx.getInitParameter(name));
        }
    }
}

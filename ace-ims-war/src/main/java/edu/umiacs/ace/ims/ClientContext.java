/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims;

import edu.umiacs.ace.util.SessionFormatter;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 *
 * @author mmcgann
 */
public class ClientContext 
{
    private String remoteAddress;
    private String sessionId;
    private String shortSessionId;
    
    private static final SessionFormatter sessionFormatter = 
            new SessionFormatter();
    
    private ClientContext()
    {}
    
    public ClientContext(WebServiceContext wsContext)
    {
        MessageContext mc = wsContext.getMessageContext();
        HttpServletRequest request = (HttpServletRequest)
                mc.get(MessageContext.SERVLET_REQUEST);        
        
        remoteAddress = request.getRemoteAddr();
        sessionId = request.getSession().getId();
        shortSessionId = sessionFormatter.format(sessionId);
    }
    
    public String getRemoteAddress()
    {
        return remoteAddress;
    }
    
    public String getSessionId()
    {
        return sessionId;
    }
    
    public String getShortSessionId()
    {
        return shortSessionId;
    }
    
}

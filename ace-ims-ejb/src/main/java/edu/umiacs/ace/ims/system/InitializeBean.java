/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.system;

import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.tokenclass.TokenClassLocal;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author mmcgann
 */
@Stateless
public class InitializeBean implements InitializeLocal 
{
    @EJB
    private TokenClassLocal tokenClassBean;
    
    public void development()
    {
        if ( tokenClassBean.list() == null || tokenClassBean.list().size() == 0 )
        {
            TokenClass tc = new TokenClass();
            tc.setName("SHA-256");
            tc.setDigestServiceName("SHA-256");
            tc.setRequestTimeout(5000);
            tc.setLinkingAllowed(true);
            tc.setRequestAsyncAllowed(true);
            tc.setRequestImmediateAllowed(true);
            tc.setRoundLength(1000);
            tc.setTreeOrder(2);
            tc.setMinimumRequests(8);
            tc.setMaximumRequests(1024);
            tokenClassBean.create(tc);

            tc = new TokenClass();
            tc.setName("SHA-256-0");
            tc.setDigestServiceName("SHA-256");
            tc.setRequestTimeout(5000);
            tc.setLinkingAllowed(true);
            tc.setRequestAsyncAllowed(true);
            tc.setRequestImmediateAllowed(true);
            tc.setRoundLength(0);
            tc.setTreeOrder(2);
            tc.setMinimumRequests(8);
            tc.setMaximumRequests(1024);
            tokenClassBean.create(tc);
        }
    }
   
}

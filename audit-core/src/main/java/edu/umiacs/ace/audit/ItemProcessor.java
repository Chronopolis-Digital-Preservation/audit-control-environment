/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.audit;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author toaster
 */
public class ItemProcessor implements Runnable{

    private AuditItem ai;
    private AuditQueue parent;

    public ItemProcessor(AuditItem ai,AuditQueue parent) {
        this.ai = ai;
        this.parent = parent;
    }

    public void run() {
        try
        {
        InputStream is = ai.openStream();
        

        }
        catch (IOException e)
        {
            
        }
        catch (Throwable t)
        {
            
        }
        finally
        {
            parent.notifyItemEnd(ai);
        }
    }


}

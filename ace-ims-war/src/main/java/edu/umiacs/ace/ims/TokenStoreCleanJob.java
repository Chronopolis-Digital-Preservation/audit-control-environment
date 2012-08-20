/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims;

import edu.umiacs.ace.ims.store.TokenStoreLocal;
import edu.umiacs.ace.server.ServiceLocator;
import java.util.Date;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job to periodically clean out old tokens
 * @author toaster
 */
public class TokenStoreCleanJob implements Job
{

    private TokenStoreLocal tokenStoreBean =
            ServiceLocator.getInstance().getLocal(TokenStoreLocal.class);

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        Log.system("Cleaning old tokens");
        IMSContext imsCtx = IMSContext.getInstance();
        Date oldDate = new Date(System.currentTimeMillis() - imsCtx.getMaxTokenAge());

        try
        {
            tokenStoreBean.deleteOldTokens(oldDate);
        }
        catch ( Exception t )
        {
            Log.error("Error cleaning old tokens", t);
            throw new JobExecutionException(t);
        }
    }
}

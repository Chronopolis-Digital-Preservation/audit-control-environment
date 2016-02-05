/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims;

import edu.umiacs.ace.ims.processor.WitnessPublication;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.tokenclass.TokenClassLocal;
import edu.umiacs.ace.server.ServiceLocator;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.EJB;

/**
 * Publish job for the quartz scheduler
 * @author toaster
 */
public class PublishJob implements Job
{

    @EJB(name = "TokenClassBean")
    private TokenClassLocal tokenClassBean =
            ServiceLocator.getInstance().getLocal(TokenClassLocal.class);

    public PublishJob()
    {
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException
    {
        Log.system("Running PublishJob");
        for ( TokenClass token : tokenClassBean.list() )
        {
            try
            {
                new WitnessPublication(token).publishWitness();
            }
            catch ( Exception t )
            {
                Log.error("Uncaught error publishing witness " + t.getMessage(),
                        t);
                throw new JobExecutionException(t);
            }
        }
        Log.system("Finish PublishJob");
    }
}

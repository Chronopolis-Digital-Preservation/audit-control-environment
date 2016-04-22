/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims;

import edu.umiacs.ace.digest.DigestException;
import edu.umiacs.ace.digest.DigestFactory;
import edu.umiacs.ace.digest.DigestProvider;
import edu.umiacs.ace.ims.processor.RoundProcessors;
import edu.umiacs.ace.ims.processor.WitnessPublisher;
import edu.umiacs.ace.ims.system.InitializeLocal;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.tokenclass.TokenClassLocal;
import edu.umiacs.ace.server.ServletContextParameters;
import edu.umiacs.ace.server.StartupBanner;
import edu.umiacs.ace.server.exception.StartupException;
import edu.umiacs.util.Parameters;
import edu.umiacs.util.StringListBuilder;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static edu.umiacs.ace.ims.IMSParameters.DEFAULT_IMMEDIATE_TOKEN_RESPONSE_TIMEOUT;
import static edu.umiacs.ace.ims.IMSParameters.DEFAULT_MAXIMUM_QUEUED_REQUESTS;
import static edu.umiacs.ace.ims.IMSParameters.DEFAULT_MAXIMUM_TOKEN_STORE_AGE;
import static edu.umiacs.ace.ims.IMSParameters.DEFAULT_REQUEST_PERMIT_TIMEOUT;
import static edu.umiacs.ace.ims.IMSParameters.DEFAULT_THREAD_SHUTDOWN_TIMEOUT;
import static edu.umiacs.ace.ims.IMSParameters.DEFAULT_TOKEN_PERSISTENCE_BATCH_SIZE;
import static edu.umiacs.ace.ims.IMSParameters.IMMEDIATE_TOKEN_RESPONSE_TIMEOUT;
import static edu.umiacs.ace.ims.IMSParameters.MAXIMUM_QUEUED_REQUESTS;
import static edu.umiacs.ace.ims.IMSParameters.MAXIMUM_TOKEN_STORE_AGE;
import static edu.umiacs.ace.ims.IMSParameters.PROCESSOR_PREFIX;
import static edu.umiacs.ace.ims.IMSParameters.REQUEST_PERMIT_TIMEOUT;
import static edu.umiacs.ace.ims.IMSParameters.THREAD_SHUTDOWN_TIMEOUT;
import static edu.umiacs.ace.ims.IMSParameters.TOKEN_PERSISTENCE_BATCH_SIZE;

/**
 * Web application lifecycle listener.
 * @author mmcgann
 */
public class ApplicationListener implements ServletContextListener
{
    private static final Logger print =
            Logger.getLogger(ApplicationListener.class);
    private static Scheduler scheduler;

    @EJB
    private InitializeLocal initializeBean;
    
    @EJB
    private TokenClassLocal tokenClassBean;
    
    public void contextInitialized(ServletContextEvent evt)
    {
        print.info("Starting context");
        Log.system(new StartupBanner(this));
        try
        {
            initializeContext(evt.getServletContext());
        }
        catch ( StartupException se )
        {
            Log.error(se.getMessage(), se);
            throw se;
        }
        Log.system("Online");
    }

    private void initializeContext(ServletContext context)
    {
        Parameters params = new ServletContextParameters(context);
        try
        {
            registerDigestProviders(params);
            configureContext(params);
            initializeDatabase();
            startProcessors();
            startWitnessPublication(params);
            startTokenCleaning();
        }
        catch ( StartupException se )
        {
            throw se;
        }
        catch ( Exception e )
        {
            throw new StartupException("Unexpected exception: " +
                    e.getMessage(), e);
        }
    }

    private void startTokenCleaning()
    {
        
        JobDetail jobDetail = new JobDetail("cleanJob", Scheduler.DEFAULT_GROUP,
                TokenStoreCleanJob.class);


        Trigger trigger = TriggerUtils.makeMinutelyTrigger(5);

        trigger.setName("cleanTrigger");
        trigger.setStartTime(new Date());

        try
        {
            scheduler.scheduleJob(jobDetail, trigger);
        }
        catch ( SchedulerException ex )
        {
            throw new StartupException("Unexpected exception registering token clean job: " +
                    ex.getMessage(), ex);
        }
    }

    private void startWitnessPublication(Parameters params)
    {

        // load publishers
        String publisherPattern = "^ace\\.ims\\.publisher\\.\\w*$";
        List<WitnessPublisher> publishers = new ArrayList<WitnessPublisher>();
        
        for ( String key : params.keySet() )
        {
            if ( key.matches(publisherPattern) )
            {
                String className = params.getString(key);
                String descriptiveName = key.substring(PROCESSOR_PREFIX.length() + 1);
                publishers.add(createPublisher(descriptiveName, className,
                        params));
            }
        }
        IMSContext.getInstance().setWitnessPublishers(publishers);

        try
        {
            // start scheduler
            SchedulerFactory sf = new StdSchedulerFactory(params.cloneAsProperties());
            scheduler = sf.getScheduler();

            JobDetail jobDetail = new JobDetail("witnessJob", Scheduler.DEFAULT_GROUP,
                    PublishJob.class);

            Trigger trigger = TriggerUtils.makeDailyTrigger(0, 0);
            trigger.setName("midnightTrigger");
            trigger.setStartTime(new Date());


            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);
        }
        catch ( SchedulerException ex )
        {
            throw new StartupException("Unexpected exception starting PublishJob: " +
                    ex.getMessage(), ex);
        }
    }

    private WitnessPublisher createPublisher(String descriptiveName, String className,
            Parameters params)
    {
        WitnessPublisher publisher; 
        try
        {
            
            Class<?> clazz = Class.forName(className);
            Class<? extends WitnessPublisher> interfaceClass 
                    = clazz.asSubclass(WitnessPublisher.class);
            publisher = interfaceClass.newInstance();
            publisher.setName(descriptiveName);
            publisher.loadParam(params);
            Log.system("Successfully created publisher: " + descriptiveName);
            return publisher;
            
        }
        catch ( InstantiationException ex )
        {
            throw new StartupException("Unexpected InstantiationException", ex);
        }
        catch ( IllegalAccessException ex )
        {
            throw new StartupException("Unexpected IllegalAccessException", ex);
        }
        catch ( ClassNotFoundException ex )
        {
            throw new StartupException("Class not found: " + className, ex);
        }

    }

    private void stopWitnessPublication()
    {
        try
        {
            if ( scheduler != null )
            {
                scheduler.shutdown(true);
            }
        }
        catch ( SchedulerException ex )
        {
            throw new StartupException("Unexpected exception: " +
                    ex.getMessage(),ex);
        }
    }
    
    private void registerDigestProviders(Parameters params)
    {
        DigestFactory digestFactory = DigestFactory.getInstance();

        boolean done = false;
        for ( int i = 0; !done; i++ )
        {
            String providerParameter = IMSParameters.DIGEST_PROVIDER +
                    "." + i;
            if ( !params.exists(providerParameter) )
            {
                done = true;
            }
            else
            {
                String providerName = params.getString(providerParameter);
                try
                {
                    digestFactory.registerProvider(providerName);
                }
                catch ( DigestException de )
                {
                    throw new StartupException("Unable to register provider (" + 
                            providerName + "): " + de.getMessage(), de);
                }
            }
        }

        List<DigestProvider> providers = digestFactory.getProviders();
        if ( providers.isEmpty() )
        {
            throw new StartupException("At least one digest provider must " + 
                    "be configured");
        }
        
        for ( DigestProvider provider : providers )
        {
            String services =
                    new StringListBuilder(provider.getServices()).toString();

            Log.system("Digest provider " + provider.getName() +
                    " " + provider.getVersion() + ": " + services +
                    " (" + provider.getClassName() + ")");
        }
    }

    private void configureContext(Parameters params)
    {
        IMSContext context = IMSContext.getInstance();
        
        try
        {
            context.setMaximumQueuedRequests(params.getInt(
                    MAXIMUM_QUEUED_REQUESTS, 
                    DEFAULT_MAXIMUM_QUEUED_REQUESTS));
            context.setTokenPersistenceBatchSize(params.getInt( 
                    TOKEN_PERSISTENCE_BATCH_SIZE, 
                    DEFAULT_TOKEN_PERSISTENCE_BATCH_SIZE));
            context.setRequestPermitTimeout(params.getInt(
                    REQUEST_PERMIT_TIMEOUT,
                    DEFAULT_REQUEST_PERMIT_TIMEOUT));
            context.setThreadShutdownTimeout(params.getInt( 
                    THREAD_SHUTDOWN_TIMEOUT, 
                    DEFAULT_THREAD_SHUTDOWN_TIMEOUT));
            context.setImmediateTokenResponseTimeout(params.getInt( 
                    IMMEDIATE_TOKEN_RESPONSE_TIMEOUT, 
                    DEFAULT_IMMEDIATE_TOKEN_RESPONSE_TIMEOUT));
            context.setMaxTokenAge(params.getLong(
                    MAXIMUM_TOKEN_STORE_AGE,
                    DEFAULT_MAXIMUM_TOKEN_STORE_AGE));
        }
        catch ( Exception pe )
        {
            throw new StartupException("Error in configuration: " + 
                    pe.getMessage(), pe);
        }
    }
    
    private void initializeDatabase()
    {
        print.debug("Initializing database");
        try
        {
            initializeBean.development();
        }
        catch ( Exception e )
        {
            throw new StartupException("Unable to initialize database: " +
                    e.getMessage(), e);
        }
        print.info("Database initialized");
    }

    private void startProcessors()
    {
        print.debug("Starting processors");
        try
        {
            List<TokenClass> tokenClasses = tokenClassBean.list();
            RoundProcessors processors = RoundProcessors.getInstance();
            for ( TokenClass tokenClass: tokenClasses )
            {
                processors.installProcessor(tokenClass);
            }
        }
        catch ( Exception e )
        {
            throw new StartupException("Unable to start processors: " + 
                    e.getMessage(), e);
        }
        print.info("Processors started");        
    }
    
    public void contextDestroyed(ServletContextEvent arg0)
    {
        print.info("Stopping context");
        try
        {
            RoundProcessors.getInstance().shutdown();
            stopWitnessPublication();
        }
        catch ( InterruptedException ie )
        {
            print.info("Interrupted");
            Thread.currentThread().interrupt();
        }
        Log.system("Offline");
    }
}

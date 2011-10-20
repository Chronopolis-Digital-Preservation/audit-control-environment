/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
// $Id$
package edu.umiacs.ace.monitor.reporting;

import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

/**
 * Context listener to control the starting and stopping of the quartz scheduler used to generate
 * reports. 
 * @author toaster
 */
public class SchedulerContextListener implements ServletContextListener {

    public static final String PARAM_SMTP_SERVER = "mail.server";
    public static final String PARAM_FROM = "mail.from";
    private SchedulerFactory schedFact = null;
    private static Scheduler sched = null;
    private static final Logger LOG = Logger.getLogger(
            SchedulerContextListener.class);
    private static String mailserver;
    private static String mailfrom;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        NDC.push("[schedstartup]");
        try {
            mailserver = event.getServletContext().getInitParameter(
                    PARAM_SMTP_SERVER);
            mailfrom = event.getServletContext().getInitParameter(PARAM_FROM);
            if (Strings.isEmpty(mailserver)) {
                mailserver = "127.0.0.1";
            }

            schedFact = new org.quartz.impl.StdSchedulerFactory();
            sched = schedFact.getScheduler();
            sched.start();
            LOG.debug("Starting the scheduler, registering all jobs");

            EntityManager em = PersistUtil.getEntityManager();
            Query q = em.createNamedQuery("ReportPolicy.listAll");
            for (Object o : q.getResultList()) {
                addJob((ReportPolicy) o);
            }
        } catch (Exception ex) {
            LOG.error("Error starting report scheduling", ex);
        } finally {
            NDC.pop();
        }
    }

    @Override
    public void contextDestroyed(
            ServletContextEvent arg0) {
        try {
            if (sched != null) { // npe if startup never worked
                sched.shutdown();
            }
            LOG.debug("Shutting down the scheduler");
        } catch (Exception ex) {
            LOG.error("Error stopping report scheduling", ex);
        }
    }

    public static final void removeJob(ReportPolicy job) {
        try {
            LOG.debug("Removing job " + job.getName());
            sched.deleteJob("report-" + job.getId(), null);
        } catch (SchedulerException e) {
            LOG.error("Error removing " + job.getName(), e);
            throw new RuntimeException("Error removeing job " + job.getName());
        }
    }

    public static final void addJob(ReportPolicy job) {
        JobDetail detail = new JobDetail("report-" + job.getId(), null,
                ReportJob.class);
        detail.getJobDataMap().put(ReportJob.ATTR_POLICY, job);

        try {
            Trigger cronTrig = new CronTrigger("reporttrigger-" + job.getId(),
                    null, job.getCronString());
            LOG.debug(
                    "Adding job " + job.getName() + " cron " + job.getCronString());
            sched.scheduleJob(detail, cronTrig);
        } catch (ParseException pe) {
            LOG.error("Error creating trigger", pe);
            throw new RuntimeException(
                    "Error parsing cron trigger in job" + job.getName());
        } catch (SchedulerException e) {
            LOG.error("Error adding " + job.getName(), e);
            throw new RuntimeException("Error adding job " + job.getName());
        }
    }

    public static void mailReport(ReportSummary report, String[] mailList)
            throws MessagingException {
        if (report == null || mailList == null || mailList.length == 0) {
            return;
        }

        boolean debug = false;

        //Set the host smtp address
        Properties props = new Properties();
        props.put("mail.smtp.host", mailserver);

        // create some properties and get the default Session
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);

        // create a message
        Message msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(mailfrom);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[mailList.length];
        for (int i = 0; i < mailList.length; i++) {
            addressTo[i] = new InternetAddress(mailList[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject("Ace Report: " + report.getReportName());
        msg.setContent(report.createReport(), "text/plain");
        Transport.send(msg);
        LOG.trace("Successfully mailed report to: " + Strings.join(',', mailList));

    }
}

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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Report Generation job for quartz
 * @author toaster
 */
public class ReportJob implements Job {

    public static final String ATTR_POLICY = "policy";
    private static final Logger LOG = Logger.getLogger(ReportJob.class);

    public ReportJob() {
    }

    @Override
    public void execute( JobExecutionContext arg0 ) throws JobExecutionException {
        NDC.push("[report]");
        ReportPolicy reportPolicy = null;
        reportPolicy = (ReportPolicy) arg0.getJobDetail().getJobDataMap().get(
                ATTR_POLICY);

        LOG.debug("Starting job " + reportPolicy.getName());
        try {
            CronTrigger cron = (CronTrigger) arg0.getTrigger();

            CronExpression expression = new CronExpression(
                    cron.getCronExpression());

            Date startDate = extractDuration(expression,
                    arg0.getScheduledFireTime());

            SummaryGenerator sg = new SummaryGenerator(reportPolicy.getName(),
                    reportPolicy.getCollection(),
                    startDate);

            ReportSummary report = sg.generateReport();
            SchedulerContextListener.mailReport(report, createMailList(reportPolicy));

        } catch ( ParseException ex ) {
            LOG.error("Unknown parse exception", ex);
            throw new JobExecutionException(ex);
        } catch ( Exception e ) {
            LOG.error("Unknown exception", e);
            throw new JobExecutionException(e);
        } finally {
            LOG.debug("Ending job " + reportPolicy.getName());
            NDC.pop();
        }
    }

    private String[] createMailList( ReportPolicy rp ) {
        return rp.getEmailList().split("\\s*,\\s*");
    }

    private Date extractDuration( CronExpression expression, Date triggerDate )
            throws JobExecutionException {

        Calendar prevCal = new GregorianCalendar();

        int count = 0;
        prevCal.setTime(triggerDate);
        prevCal.add(Calendar.MONTH, -1);
        while ( !expression.isSatisfiedBy(prevCal.getTime()) && count < 12 ) {
            count++;
            prevCal.add(Calendar.MONTH, -1);
        }

        if ( !expression.isSatisfiedBy(prevCal.getTime()) ) {
            throw new JobExecutionException(
                    "Count not find previous execution time, curr " + triggerDate + " expression "
                    + expression.getCronExpression());
        }

        return prevCal.getTime();
    }
}

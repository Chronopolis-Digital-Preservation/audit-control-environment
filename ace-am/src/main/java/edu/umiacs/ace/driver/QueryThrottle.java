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
package edu.umiacs.ace.driver;

import edu.umiacs.ace.monitor.settings.SettingsParameter;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.util.Strings;
import java.util.Timer;
import java.util.TimerTask;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class QueryThrottle implements ServletContextListener {

    private static long minWait = 0;
    private static long lastRun = 0;
    private static Timer checkTimer = null;
    private static long maxBps = 0;
    private static final String PARAM_TIME = "throttle.wait";
    private static final String PARAM_BPS = "throttle.bps";
    private static final Logger LOG = Logger.getLogger(QueryThrottle.class);

    private static void setMinWait( long minWait ) {
        QueryThrottle.minWait = minWait;
    }

    private static void setMaxBps( long maxBps ) {
        QueryThrottle.maxBps = maxBps;
    }

    public static long getMaxBps() {
        return maxBps;
    }

    public static void waitToRun() throws InterruptedException {
        if ( minWait <= 0 ) {
            return;
        }

        synchronized ( PARAM_TIME ) {
            long currentTime = System.currentTimeMillis();
            if ( (currentTime - lastRun) >= minWait ) {
                lastRun = currentTime;
                return;
            }
            PARAM_TIME.wait();
            lastRun = System.currentTimeMillis();
        }

    }


    private static synchronized void workCheck() {
        synchronized ( PARAM_TIME ) {
            PARAM_TIME.notify();
        }
    }

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        System.out.println("[QUERY]");
        EntityManager em = PersistUtil.getEntityManager();
        Query q = em.createNamedQuery("SettingsParameter.getAttr");
        q.setParameter("attr", PARAM_TIME);
        SettingsParameter s = (SettingsParameter) q.getSingleResult();
        String time = s.getValue();
        System.out.println("[QUERY] RESULT " + s.getName() + " " + s.getValue());

        if ( Strings.isValidInt(time) ) {
            setMinWait(Integer.parseInt(time));
            LOG.info("Setting query throttle minwait to " + minWait);
        }

        q.setParameter("attr", PARAM_BPS);
        s = (SettingsParameter) q.getSingleResult();
        String maxBpsString = s.getValue();
        if ( Strings.isValidLong(maxBpsString) ) {
           setMaxBps(Long.parseLong(maxBpsString));
        }


        if ( minWait > 0 ) {
            checkTimer = new Timer("srb timer");
            checkTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    workCheck();
                }
            }, 0, minWait);
        }


    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ) {
        synchronized ( PARAM_TIME ) {
            PARAM_TIME.notifyAll();
        }
        if ( checkTimer != null ) {
            checkTimer.cancel();
        }
        checkTimer = null;
    }
}

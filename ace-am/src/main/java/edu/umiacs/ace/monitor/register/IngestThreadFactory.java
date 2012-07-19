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

package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.Token;
import edu.umiacs.ace.util.PersistUtil;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 *
 * @author shake
 */
public class IngestThreadFactory extends Thread{
    private Map<String, Token> tokens;
    private List<IngestThread> threads;
    private IngestDirectory idThread;
    private Collection coll;
    // private EntityTransaction trans;
    // private EntityManager em = PersistUtil.getEntityManager();


    public IngestThreadFactory(Map<String, Token> tokens, Collection coll){
        this.tokens = tokens;
        this.coll = coll;
        //this.trans = em.getTransaction();
        threads = new ArrayList<IngestThread>();

        // Separate single thread for registering directories to avoid any race
        // conditions
        idThread = new IngestDirectory(tokens.keySet(), coll);

        // Ingesting can be _really_ slow with large stores, so split the
        // process up between 1-4 threads
        double numThreads = (tokens.size()/4.0 > 4) ? 4.0 : Math.ceil(tokens.size()/4.0);


        int begin = 0;
        List<String> keyList = new ArrayList<String>(tokens.keySet());
        for( int i = 0;i<numThreads;i++ ) {
            int end = (int) (tokens.size() * ((i + 1) / numThreads));
            IngestThread thread = new IngestThread(tokens, coll,
                    keyList.subList(begin, end));
            threads.add(thread);
            begin = end;
        }

    }


    @Override
    public void run() {
        executeThreads();
    }

    public void joinThreads() {
        try{
            idThread.join();
            for (IngestThread it: threads ) {
                it.join();
            }
        }catch (InterruptedException ex) {
            Logger.getLogger(
                    IngestThreadFactory.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if ( !getNewTokens().isEmpty() ) {
                coll.setState('E');
                EntityManager em = PersistUtil.getEntityManager();
                EntityTransaction trans = em.getTransaction();
                trans.begin();
                em.merge(coll);
                trans.commit();
                trans = null;
            }
        }
    }

    // For jsp
    public boolean isRunning() {
        boolean running = false;
        for ( IngestThread it: threads) {
            if (it.isRunning()){
                running = true;
            }
        }
        return running;
    }

    public void executeThreads() {
        idThread.start();
        for (Thread t: threads) {
            t.start();
        }
        joinThreads();
    }

    public Set<String> getUpdatedTokens() {
        Set<String> updated = new HashSet<String>();
        for (IngestThread it: threads) {
            updated.addAll(it.getUpdatedTokens());
        }
        return updated;
    }

    public Set<String> getNewTokens() {
        Set<String> newTokens = new HashSet<String>();
        for (IngestThread it: threads) {
            newTokens.addAll(it.getNewTokens());
        }
        return newTokens;
    }

    public int getUpdatedTokensSize() {
        int size = 0;
        for (IngestThread it: threads) {
            size += it.getUpdatedTokensSize();
        }
        return size;
    }

    public int getNewTokensSize() {
        int size = 0;
        for (IngestThread it: threads) {
            size += it.getNewTokensSize();
        }
        return size;    }

    // For jsp, display approximate % of ingested tokens
    public String getStatus() {
        if ( tokens.isEmpty() ) {
            return "There are no tokens to process";
        }
        int totalFinished = 0;
        for (IngestThread it: threads) {
            // This is only an estimate, no need to lock {new,updated}Tokens
            totalFinished += it.getNewTokensSize() + it.getUpdatedTokensSize()
                    + it.getUnchangedSize();
        }

        DecimalFormat format = new DecimalFormat("#.##");
        double percent = 100*(totalFinished/(double)tokens.size());
        return "Ingested " + format.format(percent) + "% of tokens";
    }

}

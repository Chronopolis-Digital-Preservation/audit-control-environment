package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.Token;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * A private class to supervise token ingestion. We use it to keep track of
 * what collections we have seen
 *
 * @author shake
 */
public class IngestSupervisor implements Runnable {
    private static final Logger LOG = Logger.getLogger(IngestSupervisor.class);
    private final Collection coll;
    private final ForkJoinPool pool;

    // I wonder if we should combine these and have some type of work unit to encapsulate
    // Token, IngestState
    private final Map<String, Token> tokens;
    private ConcurrentMap<IngestState, ConcurrentSkipListSet<String>> states;

    public IngestSupervisor(final Map<String, Token> tokens, final Collection coll) {
        this.tokens = tokens;
        this.coll = coll;
        this.pool = new ForkJoinPool();
        this.states = new ConcurrentHashMap<>();

        // so we don't have to worry about npes
        states.put(IngestState.NEW, new ConcurrentSkipListSet<>());
        states.put(IngestState.MATCH, new ConcurrentSkipListSet<>());
        states.put(IngestState.QUEUED, new ConcurrentSkipListSet<>());
        states.put(IngestState.UPDATED, new ConcurrentSkipListSet<>());
    }

    public void run() {
        LOG.info("Starting Supervisor");

        ConcurrentSkipListSet<String> queued = states.get(IngestState.QUEUED);
        queued.addAll(tokens.keySet());
        ForkJoinTask dirTask = pool.submit(new IngestDirectory(tokens.keySet(), coll));

        // Split the token store we're given up equally among our threads
        // and submit jobs to the thread pool
        List<String> keyList = new ArrayList<>(tokens.keySet());

        ForkJoinTask fileTask = pool.submit(new IngestThread(tokens, coll, keyList, states));

        dirTask.quietlyJoin();
        fileTask.quietlyJoin();
        pool.shutdown();
        LOG.info("Leaving Supervisor");
    }

    public ConcurrentMap<IngestState, ConcurrentSkipListSet<String>> getState() {
        return states;
    }

    // jsp helpers
    public int getQueuedSize() {
        return states.get(IngestState.QUEUED).size();
    }

    public Set<String> getQueued() {
        return states.get(IngestState.QUEUED);
    }

    public int getNewSize() {
        return states.get(IngestState.NEW).size();
    }

    public Set<String> getNewItems() {
        return states.get(IngestState.NEW);
    }

    public int getUpdatedSize() {
        return states.get(IngestState.UPDATED).size();
    }

    public Set<String> getUpdated() {
        return states.get(IngestState.UPDATED);
    }

    public int getMatchSize() {
        return states.get(IngestState.MATCH).size();
    }

    public Set<String> getMatched() {
        return states.get(IngestState.MATCH);
    }
}

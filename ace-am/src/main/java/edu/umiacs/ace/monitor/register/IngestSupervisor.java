package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.Token;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * A private class to supervise token ingestion. We use it to keep track of
 * what collections we have seen
 */
public class IngestSupervisor implements Runnable {
    private static final Logger LOG = Logger.getLogger(IngestSupervisor.class);
    private static Map<Collection, Set<String>> hasSeen = new HashMap<>();
    private final Map<String, Token> tokens;
    private final Collection coll;
    private final ForkJoinPool pool;

    public IngestSupervisor(final Map<String, Token> tokens, final Collection coll) {
        this.tokens = tokens;
        this.coll = coll;
        this.pool = new ForkJoinPool();
    }

    public void run() {
        LOG.info("Starting Supervisor");
        ForkJoinTask dirTask = pool.submit(new IngestDirectory(tokens.keySet(), coll));

        // Remove any tokens we've already seen and can possibly be in progress
        // Possibly release tokens after the thread has finished merging them
        /*
        Set<String> tokensSeen = hasSeen.get(coll);
        if (tokensSeen == null) {
            tokensSeen = new HashSet<>();
            tokensSeen.addAll(tokens.keySet());
        } else {
            tokens.keySet().removeAll(hasSeen.get(coll));
            tokensSeen.addAll(tokens.keySet());
        }
        hasSeen.put(coll, tokensSeen);
        */

        // Split the token store we're given up equally among our threads
        // and submit jobs to the thread pool
        List<String> keyList = new ArrayList<>(tokens.keySet());

        ForkJoinTask fileTask = pool.submit(new IngestThread(tokens, coll, keyList));

        dirTask.quietlyJoin();
        fileTask.quietlyJoin();
        pool.shutdown();
        LOG.info("Leaving Supervisor");
    }
}

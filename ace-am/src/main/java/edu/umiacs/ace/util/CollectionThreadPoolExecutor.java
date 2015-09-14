package edu.umiacs.ace.util;

import edu.umiacs.ace.monitor.audit.AuditThread;
import edu.umiacs.ace.monitor.audit.AuditTokens;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.register.IngestThreadPool;
import edu.umiacs.ace.monitor.settings.SettingsConstants;
import edu.umiacs.ace.monitor.settings.SettingsParameter;
import edu.umiacs.ace.monitor.settings.SettingsUtil;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static edu.umiacs.ace.monitor.settings.SettingsConstants.PARAM_THROTTLE_MAXAUDIT;

/**
 * ThreadPoolExecutor for managing the various types of threads which can be run on collections.
 * Each thread can mutate the collection, so we use this as a way to control which types of threads
 * get run and when.
 *
 * Created by shake on 9/8/15.
 */
public class CollectionThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger LOG = Logger.getLogger(CollectionThreadPoolExecutor.class);

    private static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final Set<Submittable> set = new ConcurrentSkipListSet<>();

    public CollectionThreadPoolExecutor(int corePoolSize,
                                        int maximumPoolSize,
                                        long keepAliveTime,
                                        TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    // Initialization
    static class ExecutorHolder {
        static final CollectionThreadPoolExecutor INSTANCE =
                createThreadPool();

        private static CollectionThreadPoolExecutor createThreadPool() {
            SettingsParameter attr = SettingsUtil.getItemByAttr(PARAM_THROTTLE_MAXAUDIT);
            int max = Integer.parseInt(attr.getValue());
            if (max <= 0) {
                max = Integer.parseInt(SettingsConstants.maxAudit);
            }
            return new CollectionThreadPoolExecutor(max, max, 1, TimeUnit.MINUTES, queue);
        }
    }

    public static CollectionThreadPoolExecutor getExecutor() {
        return ExecutorHolder.INSTANCE;
    }

    // Public interface to interact with the TPE

    public KSFuture submitFileAudit(Collection c, AuditThread at) {
        LOG.info("Submitting [FILE_AUDIT]" + " for " + c.getName());
        Submittable submittable = new Submittable(c, Submittable.RunType.FILE_AUDIT, at);
        return submitThread(at, submittable, c);
    }

    public KSFuture submitIngestThread(Collection c, IngestThreadPool.IngestSupervisor is) {
        Submittable submittable = new Submittable(c, Submittable.RunType.TOKEN_INGEST, is);
        return submitThread(is, submittable, c);
    }

    public KSFuture submitTokenAudit(Collection c, AuditTokens at) {
        LOG.info("Submitting [TOKEN_AUDIT]" + " for " + c.getName());
        Submittable submittable = new Submittable(c, Submittable.RunType.TOKEN_AUDIT, at);
        return submitThread(at, submittable, c);
    }

    public Runnable getRunnableForSubmittable(Collection c, Submittable.RunType t) {
        // hm...
        Submittable s = new Submittable(c, t, null);
        for (Submittable submittable : set) {
            if (submittable.equals(s)) {
                return submittable.getThread();
            }
        }

        return null;
    }

    public Submittable getSubmission(Collection c, Submittable.RunType t) {
        Submittable s = new Submittable(c, t, null);
        for (Submittable submittable : set) {
            if (submittable.equals(s)) {
                return submittable;
            }
        }
        return null;
    }

    /**
     * Method for submitting threads. We wrap it in a KnownFuture so that we
     * can extract the Submittable without blocking on the thread.
     *
     * @param r The runnable to submit
     * @param s The submittable to use
     * @return true if a new thread was created, false otherwise
     */
    private KSFuture submitThread(Runnable r, Submittable s, Collection c) {
        if (set.add(s)) {
            KSFuture f = new KSFuture(r, s);
            execute(f);
            return f;
        }

        Submittable.RunType t = s.getType();

        LOG.info("Already submitted RunType [" + t.name() + "]" + " for " + c.getName());
        return null;
    }

    // Overrides to allow us to display information about running threads in ACE

    protected void beforeExecute(Thread t, Runnable r) {
        // Find our Submittable and update the state
        // Type erasure :(
        if (r instanceof KnownFuture) {
            KnownFuture future = (KnownFuture) r;
            Object known = future.getKnownResult();

            if (known instanceof Submittable) {
                Submittable s = (Submittable) known;
                s.setState(Submittable.RunState.RUNNING);
            }
        }

        super.beforeExecute(t, r);
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (r instanceof KnownFuture) {
            KnownFuture future = (KnownFuture) r;
            Object known = future.getKnownResult();
            if (known instanceof Submittable) {
                set.remove(known);
            }
        }
    }

}

package edu.umiacs.ace.util;

import edu.umiacs.ace.monitor.audit.AuditThread;
import edu.umiacs.ace.monitor.audit.AuditTokens;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.register.IngestThreadPool;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolExecutor for managing the various types of threads which can be run on collections.
 * Each thread can mutate the collection, so we use this as a way to control which types of threads
 * get run and when.
 *
 * Created by shake on 9/8/15.
 */
public class CollectionThreadPoolExecutor extends ThreadPoolExecutor {

    private final Set<Submittable> set = new ConcurrentSkipListSet<Submittable>();

    public CollectionThreadPoolExecutor(int corePoolSize,
                                        int maximumPoolSize,
                                        long keepAliveTime,
                                        TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    // Public interface to interact with the TPE

    public boolean submitFileAudit(Collection c, AuditThread at) {
        Submittable submittable = new Submittable(c, RunType.FILE_AUDIT);
        return submitThread(at, submittable);
    }

    public boolean submitIngestThread(Collection c, IngestThreadPool.IngestSupervisor is) {
        Submittable submittable = new Submittable(c, RunType.TOKEN_INGEST);
        return submitThread(is, submittable);
    }

    public boolean submitTokenAduit(Collection c, AuditTokens at) {
        Submittable submittable = new Submittable(c, RunType.TOKEN_AUDIT);
        return submitThread(at, submittable);
    }

    private boolean submitThread(Runnable r, Submittable s) {
        if (set.add(s)) {
            this.execute(new KnownFuture<Submittable>(r, s));
            return true;
        }

        return false;
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
                s.setState(RunState.RUNNING);
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

    private enum RunType {
        FILE_AUDIT, TOKEN_AUDIT, TOKEN_INGEST
    }

    public enum RunState {
        QUEUED, RUNNING
    }

    private class Submittable {
        final Collection collection;
        final RunType type;
        RunState state;

        private Submittable(Collection collection, RunType type) {
            this.collection = collection;
            this.type = type;
            this.state = RunState.QUEUED;
        }

        public void setState(RunState state) {
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Submittable that = (Submittable) o;

            if (!collection.equals(that.collection)) return false;
            return type == that.type;
        }

        @Override
        public int hashCode() {
            int result = collection.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }

}

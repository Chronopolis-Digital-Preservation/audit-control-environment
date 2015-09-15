package edu.umiacs.ace.util;

import com.google.common.util.concurrent.Striped;
import edu.umiacs.ace.monitor.core.Collection;
import org.apache.log4j.Logger;

import java.util.concurrent.Semaphore;

import static edu.umiacs.ace.util.Submittable.RunState.FINISHED;
import static edu.umiacs.ace.util.Submittable.RunState.RUNNING;

/**
 * KnownSubmittableFuture. Just so we have less generics going around.
 *
 * Created by shake on 9/14/15.
 */
public class KSFuture<V extends Runnable> extends KnownFuture<Submittable<V>> {
    private static final Logger LOG = Logger.getLogger(KSFuture.class);
    // idk
    private static final int NUM_PERMITS = 1;
    private static final int NUM_STRIPES = 128;

    // Could also use a read write lock which would allow file + token audits to run simultaneously
    private static Striped<Semaphore> locks = Striped.semaphore(NUM_STRIPES, NUM_PERMITS);

    public KSFuture(Runnable runnable, Submittable<V> result) {
        super(runnable, result);
    }

    private void beforeExecute(Submittable<V> result) {
        Collection c = result.collection;
        Semaphore semaphore = locks.get(c);
        // LOG.debug(info(result) + " acquiring semaphore");
        semaphore.acquireUninterruptibly();
        LOG.debug(info(result) + " acquired semaphore " + semaphore.toString());
        result.setState(RUNNING);
    }

    private void afterExecute(Submittable<V> result) {
        Collection c = result.collection;
        Semaphore semaphore = locks.get(c);
        semaphore.release();
        LOG.debug(info(result) + " released semaphore");
        result.setState(FINISHED);
    }

    @Override
    public void run() {
        Submittable<V> result = getKnownResult();
        beforeExecute(result);
        super.run();
        afterExecute(result);
    }

    private String info(Submittable s) {
        Collection c = s.collection;
        String group = c.getGroup();
        if (group == null) {
            group = "";
        }

        return group + "/" + c.getName() + "::" + s.getType();
    }

}

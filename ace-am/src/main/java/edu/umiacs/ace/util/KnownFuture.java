package edu.umiacs.ace.util;

import java.util.concurrent.FutureTask;

/**
 * Class which we can get back our result without running the runnable
 *
 * Created by shake on 9/11/15.
 */
public class KnownFuture<V> extends FutureTask<V> {

    private final V knownResult;

    public KnownFuture(Runnable runnable, V result) {
        super(runnable, result);
        this.knownResult = result;
    }

    public V getKnownResult() {
        return knownResult;
    }
}

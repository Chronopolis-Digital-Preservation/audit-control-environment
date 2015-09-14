package edu.umiacs.ace.util;

/**
 * KnownSubmittableFuture. Just so we have less generics going around.
 *
 * Created by shake on 9/14/15.
 */
public class KSFuture<V extends Runnable> extends KnownFuture<Submittable<V>> {
    public KSFuture(Runnable runnable, Submittable<V> result) {
        super(runnable, result);
    }
}

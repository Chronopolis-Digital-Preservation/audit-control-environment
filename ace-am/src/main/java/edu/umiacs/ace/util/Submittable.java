package edu.umiacs.ace.util;

import edu.umiacs.ace.monitor.core.Collection;

/**
 * Created by shake on 9/11/15.
 */
public class Submittable implements Comparable<Submittable> {
    final Collection collection;
    final Runnable runnable;
    final RunType type;
    RunState state;

    Submittable(Collection collection, RunType type, Runnable runnable) {
        this.collection = collection;
        this.runnable = runnable;
        this.type = type;
        this.state = RunState.QUEUED;
    }

    public void setState(RunState state) {
        this.state = state;
    }

    public RunState getState() {
        return state;
    }

    public Runnable getThread() {
        return runnable;
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

    public int compareTo(Submittable submittable) {
        return (int) (this.collection.getId() - submittable.collection.getId());
    }

    public enum RunType {
        FILE_AUDIT, TOKEN_AUDIT, TOKEN_INGEST
    }

    public enum RunState {
        QUEUED, RUNNING
    }
}

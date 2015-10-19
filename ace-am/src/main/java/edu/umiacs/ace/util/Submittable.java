package edu.umiacs.ace.util;

import edu.umiacs.ace.monitor.core.Collection;

/**
 * Created by shake on 9/11/15.
 */
public class Submittable<V extends Runnable> implements Comparable<Submittable> {
    final Collection collection;
    final V runnable;
    final RunType type;
    RunState state;

    Submittable(Collection collection, RunType type, V runnable) {
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

    public V getThread() {
        return runnable;
    }

    public RunType getType() {
        return type;
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
        long myId = this.collection.getId();
        long oId = submittable.collection.getId();
        long mOrd = this.getType().ordinal();
        long oOrd = submittable.getType().ordinal();

        int eq = 0;

        if (myId < oId) {        // t -> eq = -2
            eq -= 2;
        } else if (myId > oId) { // t -> eq_2 = 2
            eq += 2;
        }

        if (mOrd < oOrd) {       // t -> eq_2 = 1
            eq -= 1;
        } else if (mOrd > oOrd) { // t -> eq = -1
            eq += 1;
        }

        return eq;
    }

    public enum RunType {
        FILE_AUDIT, TOKEN_AUDIT, TOKEN_INGEST
    }

    public enum RunState {
        QUEUED, RUNNING, FINISHED
    }
}

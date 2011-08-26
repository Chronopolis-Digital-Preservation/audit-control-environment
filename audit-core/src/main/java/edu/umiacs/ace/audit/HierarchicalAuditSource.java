/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.audit;

import java.util.LinkedList;
import java.util.Queue;

/**
 * How should fileQueue be synchronized
 * @author toaster
 */
public abstract class HierarchicalAuditSource<D, F> extends AuditSource {

    private Queue<D> directoryQueue = new LinkedList<D>();
    private Queue<F> fileQueue = new LinkedList<F>();
    private D root;

    public HierarchicalAuditSource(D root) {
        this.root = root;
        directoryQueue.offer(root);
    }

    public D getRoot() {
        return root;
    }

    protected void pushDirectory(D directory) {
        directoryQueue.offer(directory);
    }

    protected void pushFile(F file) {
        fileQueue.offer(file);
    }

    @Override
    public int available() {
        return fileQueue.size();
    }

    @Override
    public void queueNext(int preferred) {
        // just return if we have enough
        if (preferred > 0 && fileQueue.size() >= preferred) {
            return;
        }

        while (fileQueue.size() < preferred && !directoryQueue.isEmpty() && !isAborted()) {
            loadChildren(directoryQueue.poll());
        }

    }

    @Override
    protected boolean finished() {
        return fileQueue.isEmpty() && directoryQueue.isEmpty();
    }

    @Override
    public AuditItem getNext() {
        F next = fileQueue.poll();

        if (next != null) {
            return convertFileToItem(next);

        }
        return null;

    }

    /**
     * Convert the supplied file into an auditItem
     * 
     * @param file file to convert
     * @return item to be audited
     */
    protected abstract AuditItem convertFileToItem(F file);

    /**
     * Load all children in the supplied directory. Do NOT recurse into any
     * discovered subdirectories, but rather queue them using pushDirectory. Any
     * discovered files should be loaded using pushFile
     *
     * @param dir directory to load
     */
    protected abstract void loadChildren(D dir);
}

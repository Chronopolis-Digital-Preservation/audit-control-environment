package edu.umiacs.ace.monitor.support;

/**
 * Encapsulate the result when trying to submit a collection for processing. Separate from the
 * {@link edu.umiacs.ace.util.CollectionThreadPoolExecutor} because that will return the future.
 * This is more for the API to signal what response code we should return.
 *
 * @author shake
 */
public enum SubmissionResult {
    SUCCESS, CONFLICT
}

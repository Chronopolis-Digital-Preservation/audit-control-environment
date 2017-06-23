package edu.umiacs.ace.rest.models;

import edu.umiacs.ace.rest.CompareController;

import java.util.List;

/**
 * The request body sent to the {@link CompareController}
 *
 * Created by shake on 3/9/17.
 */
public class CompareRequest {

    private List<CompareFile> comparisons;

    public List<CompareFile> getComparisons() {
        return comparisons;
    }

    public CompareRequest setComparisons(List<CompareFile> comparisons) {
        this.comparisons = comparisons;
        return this;
    }
}

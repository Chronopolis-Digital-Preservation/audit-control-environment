package edu.umiacs.ace.rest.models;

import edu.umiacs.ace.rest.CompareController;

import java.util.HashSet;
import java.util.Set;

/**
 * Response object returned by the {@link CompareController}
 *
 * Created by shake on 3/9/17.
 */
public class CompareResponse {

    private Set<String> diff;
    private Set<String> match;
    private Set<String> notFound;

    public CompareResponse() {
        this.diff = new HashSet<>();
        this.match = new HashSet<>();
        this.notFound = new HashSet<>();
    }

    public CompareResponse addDiff(String path) {
        diff.add(path);
        return this;
    }

    public Set<String> getDiff() {
        return diff;
    }

    public CompareResponse setDiff(Set<String> diff) {
        this.diff = diff;
        return this;
    }

    public CompareResponse addMatch(String path) {
        match.add(path);
        return this;
    }

    public Set<String> getMatch() {
        return match;
    }

    public CompareResponse setMatch(Set<String> match) {
        this.match = match;
        return this;
    }

    public CompareResponse addNotFound(String path) {
        notFound.add(path);
        return this;
    }

    public Set<String> getNotFound() {
        return notFound;
    }

    public CompareResponse setNotFound(Set<String> notFound) {
        this.notFound = notFound;
        return this;
    }
}

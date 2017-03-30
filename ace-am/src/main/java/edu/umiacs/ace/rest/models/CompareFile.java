package edu.umiacs.ace.rest.models;

/**
 * Individual item in a Compare Request
 *
 * Created by shake on 3/9/17.
 */
public class CompareFile {

    private String path;
    private String digest;

    public String getPath() {
        return path;
    }

    public String getDigest() {
        return digest;
    }

    public CompareFile setPath(String path) {
        this.path = path;
        return this;
    }

    public CompareFile setDigest(String digest) {
        this.digest = digest;
        return this;
    }
}

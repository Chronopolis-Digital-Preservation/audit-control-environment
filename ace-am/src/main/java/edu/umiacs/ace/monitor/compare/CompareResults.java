/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.monitor.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * target = local ace install
 * source = remote/supplied digest list
 * @author toaster
 */
public final class CompareResults {

    private Set<String> unseenSupplied;
    private Set<String> unseenTarget;
    private Set<DifferingName> differingNames = new TreeSet<DifferingName>();
    private Set<DifferingDigest> differingDigests = new TreeSet<DifferingDigest>();
    private boolean running = true;
    private String message = null;

    public CompareResults(CollectionCompare2 cc2, int hint) {
        unseenTarget = new TreeSet<String>();
//differingNames = new TreeSet<DifferingName>(hint);
        unseenSupplied = new TreeSet<String>(cc2.getSourceMap().keySet());
    }

    void finished() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public String getMessage() {
        return message;
    }

    void fileExistsAtTarget(String file) {
        unseenSupplied.remove(file);
    }

    void sourceFileNotFound(String file) {
        unseenTarget.add(file);
    }

    void mismatchedDigests(String file, String sourceDigest, String targetDigest) {
        differingDigests.add(new DifferingDigest(file, sourceDigest, targetDigest));
    }

    void mismatchedNames(String digest, String sourcename, String targetName) {
        differingNames.add(new DifferingName(sourcename, targetName, digest));
    }

    /**
     * Files that exist in the collection, but not target
     * @return
     */
    public Set<String> getUnseenSuppliedFiles() {

        return unseenSupplied;
    }

    /**
     * Files that exist in the target but not collection
     * @return
     */
    public Set<String> getUnseenTargetFiles() {

        return Collections.unmodifiableSet(unseenTarget);
    }

    public Set<DifferingDigest> getDifferingDigests() {
        return differingDigests;
    }

    public Set<DifferingName> getDifferingNames() {
        return differingNames;
    }

    public static class DifferingName implements Comparable<DifferingName> {

        private String compString;
        private String sourceName;
        private String destinationName;
        private String digest;

        private DifferingName(String sourceName, String destinationName,
                String digest) {
            this.sourceName = sourceName;
            this.destinationName = destinationName;
            this.digest = digest;
            compString = sourceName + destinationName + digest;
        }

        public String getDestinationName() {
            return destinationName;
        }

        public String getDigest() {
            return digest;
        }

        public String getSourceName() {
            return sourceName;
        }

        public void setDestinationName(String destinationName) {
            this.destinationName = destinationName;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        @Override
        public int hashCode() {
            return compString.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DifferingName) {
                DifferingName dd = (DifferingName) obj;
                return compString.equals(dd.compString);
            }
            return false;
        }

        @Override
        public int compareTo(DifferingName o) {
            return compString.compareTo(o.compString);
        }
    }

    public static class DifferingDigest implements Comparable<DifferingDigest> {

        private String name;
        private String sourceDigest;
        private String targetDigest;
        private String compString;

        private DifferingDigest(String name, String sourceDigest,
                String targetDigest) {
            this.name = name;
            this.sourceDigest = sourceDigest;
            this.targetDigest = targetDigest;
            compString = name + sourceDigest + targetDigest;
        }

        public String getName() {
            return name;
        }

        public String getSourceDigest() {
            return sourceDigest;
        }

        public String getTargetDigest() {
            return targetDigest;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setTargetDigest(String targetDigest) {
            this.targetDigest = targetDigest;
        }

        public void setSourceDigest(String sourceDigest) {
            this.sourceDigest = sourceDigest;
        }

        @Override
        public int compareTo(DifferingDigest o) {
            return compString.compareTo(o.compString);
        }

        @Override
        public int hashCode() {
            return compString.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DifferingDigest) {
                DifferingDigest dd = (DifferingDigest) obj;
                return compString.equals(dd.compString);
            }
            return false;
        }
    }
}

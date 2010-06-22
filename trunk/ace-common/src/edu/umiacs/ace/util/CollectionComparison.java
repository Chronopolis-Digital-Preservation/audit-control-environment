/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
// $Id: CollectionComparison.java 3192 2010-06-22 16:54:09Z toaster $

package edu.umiacs.ace.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author toaster
 */
public class CollectionComparison {

    private Set<DifferingName> differingNames = new TreeSet<DifferingName>(); // source name, dest name
    private Set<String> unseenFiles = new TreeSet<String>(); // unseen source files
    private Set<DifferingDigest> differingDigests = new TreeSet<DifferingDigest>();
    private Set<String> unseenTargetFiles;

    public CollectionComparison() {
    }

    public void compare( InputStream sourceColl, CollectionTarget targetColl ) {
        compare(sourceColl, targetColl, null);
    }

    public void compare( InputStream sourceColl, CollectionTarget targetColl, String prefix ) {

        Map<String, String[]> hashToFile = new HashMap<String, String[]>(); // hash,filenames[]
        Set<String> okSourceFiles = new HashSet<String>();
//        long item = 0;
//        long startTime;

        for ( SourceFile source : readFile(sourceColl, prefix) ) {

//            startTime = System.currentTimeMillis();
//            System.out.println("Processing " + item + " time " + startTime);
//            item++;


            // easy case, get hash for file from target.
            String hash;
            if ( (hash = targetColl.getHash(source.getName())) != null ) {

                if ( source.getDigest().equals(hash) ) {
                    // hashes match, remove from differing in case we have identical files (ie 0-length)
                    okSourceFiles.add(source.getName());
                    targetColl.markSeenName(source.getName());

                } else {
                    // Same name, different hashes
                    differingDigests.add(new DifferingDigest(source.getName(),
                            source.getDigest(), hash));
                }

            }

            if ( hashToFile.containsKey(source.getDigest()) ) {
                String[] old = hashToFile.get(source.getDigest());
                String[] newAr = Arrays.copyOf(old, old.length + 1);
                newAr[old.length] = source.getName();
                hashToFile.put(source.getDigest(), newAr);
            } else {
                String newAr[] = new String[]{
                    source.getName()
                };
                hashToFile.put(source.getDigest(), newAr);

            }
//            System.out.println(
//                    "Finished item, time " + (System.currentTimeMillis() - startTime));
        }

//        item = 0;
        for ( String digest : hashToFile.keySet() ) {
            String[] targetNames;
            String[] sourceNames = hashToFile.get(digest);
//            item++;

            // test to see if this item's hash has anything in the target attached
            if ( (targetNames = targetColl.getName(digest)) != null ) {
                // we have hashes in target
//                System.out.println("hash " + item );
                for ( String sourceName : sourceNames ) {
                    if ( !okSourceFiles.contains(sourceName) ) {
                        // all targets are possible matches, add
                        for ( String targetName : targetNames ) {
                            DifferingName dn = new DifferingName(sourceName,
                                    targetName, digest);
                            differingNames.add(dn);
                            targetColl.markSeenName(targetName);
                        }
                    }
                }
            } else {
                // everything w/ this digest is missing
//                System.out.println(
//                        "No target file list for digest, adding all to unseen");
                unseenFiles.addAll(Arrays.asList(sourceNames));
            }
        }



        // now get all unseen files from the target
        unseenTargetFiles = targetColl.listUnSeen();
    }

    public Set<DifferingDigest> getDifferingDigests() {
        return differingDigests;
    }

    public Set<DifferingName> getDifferingNames() {
        return differingNames;
    }

    public Set<String> getUnseenSourceFiles() {
        return unseenFiles;
    }

    public Set<String> getUnseenTargetFiles() {
        return unseenTargetFiles;
    }

    public class DifferingDigest implements Comparable<DifferingDigest> {

        private String name;
        private String sourceDigest;
        private String targetDigest;
        private String compString;

        public DifferingDigest( String name, String sourceDigest,
                String targetDigest ) {
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

        public void setName( String name ) {
            this.name = name;
        }

        public void setTargetDigest( String targetDigest ) {
            this.targetDigest = targetDigest;
        }

        public void setSourceDigest( String sourceDigest ) {
            this.sourceDigest = sourceDigest;
        }

        public int compareTo( DifferingDigest o ) {
            return compString.compareTo(o.compString);
        }

        @Override
        public int hashCode() {
            return compString.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if ( obj instanceof DifferingDigest ) {
                DifferingDigest dd = (DifferingDigest) obj;
                return compString.equals(dd.compString);
            }
            return false;
        }
    }

    public class DifferingName implements Comparable<DifferingName> {

        private String compString;
        private String sourceName;
        private String destinationName;
        private String digest;

        public DifferingName( String sourceName, String destinationName,
                String digest ) {
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

        public void setDestinationName( String destinationName ) {
            this.destinationName = destinationName;
        }

        public void setDigest( String digest ) {
            this.digest = digest;
        }

        public void setSourceName( String sourceName ) {
            this.sourceName = sourceName;
        }

        @Override
        public int hashCode() {
            return compString.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if ( obj instanceof DifferingName ) {
                DifferingName dd = (DifferingName) obj;
                return compString.equals(dd.compString);
            }
            return false;
        }

        public int compareTo( DifferingName o ) {
            return compString.compareTo(o.compString);
        }
    }

    private Iterable<SourceFile> readFile( final InputStream sourceColl, final String prefix ) {
        return new Iterable<SourceFile>() {

            public Iterator<SourceFile> iterator() {
                return new ReadIterator(sourceColl, prefix);
            }
        };
    }

    class ReadIterator implements Iterator<SourceFile> {

        private BufferedReader input;
        private SourceFile next = null;
        private String prefix = null;

        public ReadIterator( InputStream input, String prefix ) {
            this.prefix = prefix;
            this.input = new BufferedReader(new InputStreamReader(input));
            next = readNext();

        }

        public boolean hasNext() {
            return next != null;
        }

        public SourceFile next() {
            SourceFile oldNext = next;

            next = readNext();

            return oldNext;
        }

        private SourceFile readNext() {
            String[] tokens = readLine();

            while ( tokens != null && (tokens.length != 2
                    || (prefix != null && !tokens[1].startsWith(prefix))) ) {
                tokens = readLine();
            }

            if ( tokens != null ) {
                return new SourceFile(tokens[1], tokens[0]);
            } else {
                return null;
            }
        }

        private String[] readLine() {
            try {
                String line = input.readLine();
                if ( line == null ) {
                    return null;
                }
                String[] tokens = line.split("\t");
                return tokens;

            } catch ( IOException e ) {
                throw new RuntimeException(e);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    class SourceFile {

        private String name;
        private String digest;

        public SourceFile( String name, String digest ) {
            this.name = name;
            this.digest = digest;
        }

        public void setDigest( String digest ) {
            this.digest = digest;
        }

        public void setName( String name ) {
            this.name = name;
        }

        public String getDigest() {
            return digest;
        }

        public String getName() {
            return name;
        }
    }
}

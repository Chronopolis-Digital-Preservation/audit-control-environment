/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.audit.localfile;

import edu.umiacs.ace.audit.AuditItem;
import edu.umiacs.ace.audit.HierarchicalAuditSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author toaster
 */
public class LocalFileSource extends HierarchicalAuditSource<File, File> {

    public LocalFileSource(File root) {
        super(root);
    }

    @Override
    protected AuditItem convertFileToItem(File file) {

        return new FileAuditItem(file);
    }

    private String[] convertFilePath(File file) {
        int substrLength = getRoot().getPath().length();

        // build directory path
        List<String> dirPathList = new ArrayList<String>();
        File currFile = file;
        while (!currFile.equals(getRoot())) {
//                LOG.trace("Adding dir to path: " + currFile.getPath().substring(
//                        substrLength));
            String pathToAdd = currFile.getPath().substring(substrLength);
            pathToAdd = pathToAdd.replace(File.separatorChar, '/');
            dirPathList.add(pathToAdd);
            currFile = currFile.getParentFile();
        }
        return dirPathList.toArray(new String[dirPathList.size()]);
    }

    @Override
    protected void loadChildren(File dir) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public class FileAuditItem extends AuditItem {

        public FileAuditItem(File f) {
            super(convertFilePath(f), LocalFileSource.this);
        }

        @Override
        public InputStream openStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

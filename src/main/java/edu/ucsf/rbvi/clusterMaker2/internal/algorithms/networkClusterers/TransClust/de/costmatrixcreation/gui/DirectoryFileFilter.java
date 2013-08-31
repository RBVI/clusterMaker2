package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.gui;

import java.io.File;

public class DirectoryFileFilter extends javax.swing.filechooser.FileFilter
    implements java.io.FileFilter {
    
    private static final String DESCRIPTION = "Directories only";
    
    public DirectoryFileFilter() {
    }

    public boolean accept(File f) {
        return f.isDirectory();
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    
}

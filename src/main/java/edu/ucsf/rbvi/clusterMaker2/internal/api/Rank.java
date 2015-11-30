package edu.ucsf.rbvi.clusterMaker2.internal.api;

import org.cytoscape.work.TaskMonitor;

public interface Rank {
    String getShortName();
    String getName();
    Object getContext();
    void run(TaskMonitor monitor);
    boolean isAvailable();
}

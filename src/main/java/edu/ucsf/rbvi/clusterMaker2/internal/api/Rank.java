package edu.ucsf.rbvi.clusterMaker2.internal.api;

import org.cytoscape.work.TaskMonitor;

public interface Rank {
    final public static String GROUP_ATTRIBUTE = "ranklust";

    String getShortName();
    String getName();
    Object getContext();
    void run(TaskMonitor monitor);
}

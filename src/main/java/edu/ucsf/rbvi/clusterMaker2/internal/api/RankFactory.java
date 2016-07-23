package edu.ucsf.rbvi.clusterMaker2.internal.api;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;

public interface RankFactory extends TaskFactory {
    String getShortName();
    String getName();
    Object getContext();
}

package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterVizFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import java.util.Collections;
import java.util.List;

public class CreateRankingPanelTaskFactory implements ClusterVizFactory {

    ClusterManager clusterManager = null;
    boolean checkAvailable = false;
    public static String RANKNAME = "Show results from ranking clusters";
    public static String RANKSHORTNAME = "createRankingPanel";

    public CreateRankingPanelTaskFactory(ClusterManager clusterManager, boolean checkAvailable) {
        this.clusterManager = clusterManager;
        this.checkAvailable = checkAvailable;
    }

    @Override
    public String getShortName() {
        if (checkAvailable) {
            return RANKSHORTNAME;
        }

        return null;
    }

    @Override
    public String getName() {
        if (checkAvailable) {
            return RANKNAME;
        }

        return null;
    }

    @Override
    public ClusterViz getVisualizer() {
        return null;
    }

    @Override
    public boolean isAvailable(CyNetwork network) {
        if (!checkAvailable) {
            return true;
        }

        return network != null && RankingPanelTask.isReady(network);
    }

    @Override
    public List<ClusterType> getTypeList() {
        return Collections.singletonList(ClusterType.UI);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new RankingPanelTask(clusterManager, checkAvailable, true));
    }

    @Override
    public boolean isReady() {
        if (!checkAvailable) {
            return true;
        }

        return RankingPanelTask.isReady(clusterManager.getNetwork());
    }
}

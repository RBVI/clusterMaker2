package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.algorithms;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units.PREdge;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units.PRNode;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ClusterUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.util.HashMap;
import java.util.List;

public class PageRank extends AbstractTask implements Rank {
    private ClusterManager manager;
    final public static String NAME = "Create rank from the Page Rank algorithm with priors";
    final public static String SHORTNAME = "PRP";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public PRContext context;

    public PageRank(PRContext context, ClusterManager manager) {
        this.context = context;
        this.manager = manager;

        if (network == null) {
            network = this.manager.getNetwork();
        }

        this.context.setNetwork(network);
        this.context.updateContext();
    }

    @Override
    public String getShortName() {
        return SHORTNAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setProgress(0.0);
        taskMonitor.setTitle("PageRank with Priors ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Fetching clusters...");
        taskMonitor.setProgress(0.1);
        List<NodeCluster> clusters = ClusterUtils.fetchClusters(network);
        taskMonitor.setProgress(0.5);

        List<String> nodeAttributes = context.getSelectedNodeAttributes();
        List<String> edgeAttributes = context.getSelectedEdgeAttributes();

        Hypergraph<PRNode, PREdge> graph = new DirectedSparseGraph<>();
        HashMap<Long, PRNode> idToNode = new HashMap<>();
        List<CyNode> nodeList = network.getNodeList();
        List<CyEdge> edgeList = network.getEdgeList();
        CyTable nodeTable = network.getDefaultNodeTable();
        CyTable edgeTable = network.getDefaultEdgeTable();

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node scores in clusters");

        clusters.forEach(NodeCluster::initNodeScores);

        for (CyNode node : nodeList) {
            PRNode prNode = new PRNode(node);
            insertNodeScore(prNode, nodeTable, nodeAttributes);
            graph.addVertex(prNode);
            idToNode.put(node.getSUID(), prNode);
        }

        for (CyEdge edge : edgeList) {
            PRNode sourceNode = idToNode.get(edge.getSource().getSUID());
            PRNode targetNode = idToNode.get(edge.getTarget().getSUID());
            PREdge prEdge = new PREdge(edge);
            insertEdgeScore(prEdge, edgeTable, edgeAttributes);
            graph.addEdge(prEdge, new Pair<>(sourceNode, targetNode), EdgeType.DIRECTED);
        }

        PageRankWithPriors<PRNode, PREdge> pageRank = new PageRankWithPriors<>(graph, transformEdge(), transformNode(), 0.7d);
        pageRank.setMaxIterations(1000);
        pageRank.evaluate();

        for (PRNode node : graph.getVertices()) {
            node.setPRScore(pageRank.getVertexScore(node));

            for (NodeCluster cluster : clusters) {
                if (cluster.getNodeScores().containsKey(node.getCyNode().getSUID())) {
                    cluster.increaseRankScore(node.getPRScore());
                }
            }
        }

        ClusterUtils.insertResultsInColumns(network, clusters, SHORTNAME);

        taskMonitor.setProgress(0.6);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node scores in clusters");
        taskMonitor.setProgress(0.75);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting edge scores in clusters");
        taskMonitor.setProgress(0.80);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Sorting and ranking clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Insert cluster information in tables");
        taskMonitor.setProgress(1.0);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    private void insertNodeScore(PRNode prNode, CyTable nodeTable, List<String> nodeAttributes) {
        Double totalNodeScore = 0.0d;

        for (String nodeAttribute : nodeAttributes) {
            double singleAttributeScore = 0.0d;

            try { // Double
                singleAttributeScore = nodeTable.getRow(prNode.getCyNode().getSUID())
                        .get(nodeAttribute, Double.class, 0.0d);
            } catch (ClassCastException cce) {
                try { // Integer
                    singleAttributeScore = nodeTable.getRow(prNode.getCyNode().getSUID())
                            .get(nodeAttribute, Integer.class, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                totalNodeScore += singleAttributeScore;
            }
        }

        prNode.setScore(totalNodeScore);
    }

    private void insertEdgeScore(PREdge prEdge, CyTable edgeTable, List<String> edgeAttributes) {
        Double totalEdgeScore = 0.0d;

        for (String edgeAttribute : edgeAttributes) {
            double singleEdgeAttributeScore = 0.0d;

            try { // Double
                singleEdgeAttributeScore = edgeTable.getRow(prEdge.getCyEdge().getSUID())
                .get(edgeAttribute, Double.class, 0.0d);
            } catch (ClassCastException cce) {
                try { // Integer
                    singleEdgeAttributeScore = edgeTable.getRow(prEdge.getCyEdge().getSUID())
                            .get(edgeAttribute, Integer.class, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                totalEdgeScore += singleEdgeAttributeScore;
            }
        }

        prEdge.setScore(totalEdgeScore);
    }

    private Function<PREdge, Double> transformEdge() {
        return PREdge::getScore;
    }

    private Function<PRNode, Double> transformNode() {
        return PRNode::getScore;
    }

    public String getClusterColumnName() {
        return this.network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class, "");
    }
}

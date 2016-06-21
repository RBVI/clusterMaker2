package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.HITS;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
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

public class HyperlinkInducedTopicSearch extends AbstractTask implements Rank {
    private ClusterManager manager;
    public static final String NAME = "Create rank from the HyperlinkInducedTopicSearch algorithm with priors";
    public static final String SHORTNAME = "HyperlinkInducedTopicSearch";
    private Graph<PRNode, PREdge> graph;
    private List<CyNode> nodeList;
    private HashMap<Long, PRNode> idToNode;
    private List<CyEdge> edgeList;
    private CyTable nodeTable;
    private CyTable edgeTable;
    private List<String> nodeAttributes;
    private List<String> edgeAttributes;

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public HITSContext context;

    public HyperlinkInducedTopicSearch(HITSContext context, ClusterManager manager) {
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
        taskMonitor.setTitle("Hyperlink-Induced Topic Search ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Fetching clusters...");
        taskMonitor.setProgress(0.1);
        List<NodeCluster> clusters = ClusterUtils.fetchClusters(network);
        taskMonitor.setProgress(0.5);

        initVariables();
        clusters.forEach(NodeCluster::initNodeScores);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node scores in clusters");
        addNodes();
        taskMonitor.setProgress(0.6);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting edge scores in clusters");
        addEdges();
        taskMonitor.setProgress(0.7);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Performing HITS algorithm");
        HITS<PRNode, PREdge> hyperlinkInducedTopicSearchPriors = performHITS(graph);
        taskMonitor.setProgress(0.8);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting cluster scores");
        setScores(clusters, graph, hyperlinkInducedTopicSearchPriors);
        taskMonitor.setProgress(0.9);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Insert cluster information in tables");
        ClusterUtils.insertResultsInColumns(network, clusters, SHORTNAME);

        taskMonitor.setProgress(1.0);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    private void initVariables() {
        nodeAttributes = context.getSelectedNodeAttributes();
        edgeAttributes = context.getSelectedEdgeAttributes();
        graph = new DirectedSparseMultigraph<>();
        idToNode = new HashMap<>();
        nodeList = network.getNodeList();
        edgeList = network.getEdgeList();
        nodeTable = network.getDefaultNodeTable();
        edgeTable = network.getDefaultEdgeTable();
    }

    private HITS<PRNode, PREdge> performHITS(Graph<PRNode, PREdge> graph) {
        HITS<PRNode, PREdge> hits = new HITS<>(graph, context.getAlpha());
        hits.setMaxIterations(1000);
        hits.evaluate();
        return hits;
    }

    private void setScores(List<NodeCluster> clusters, Graph<PRNode, PREdge> graph, HITS<PRNode, PREdge> hits) {
        for (PRNode node : graph.getVertices()) {
            node.setPRScore(hits.getVertexScore(node).authority);

            for (NodeCluster cluster : clusters) {
                if (cluster.getNodeScores().containsKey(node.getCyNode().getSUID())) {
                    cluster.increaseRankScore(node.getPRScore());
                }
            }
        }
    }

    private void addEdges() {
        for (CyEdge edge : edgeList) {
            PRNode sourceNode = idToNode.get(edge.getSource().getSUID());
            PRNode targetNode = idToNode.get(edge.getTarget().getSUID());
            PREdge prEdge = new PREdge(edge);
            insertEdgeScore(prEdge, edgeTable, edgeAttributes);
            graph.addEdge(prEdge, new Pair<>(sourceNode, targetNode), EdgeType.DIRECTED);
        }
    }

    private void addNodes() {
        for (CyNode node : nodeList) {
            PRNode prNode = new PRNode(node);
            insertNodeScore(prNode, nodeTable, nodeAttributes);
            graph.addVertex(prNode);
            idToNode.put(node.getSUID(), prNode);
        }
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
}

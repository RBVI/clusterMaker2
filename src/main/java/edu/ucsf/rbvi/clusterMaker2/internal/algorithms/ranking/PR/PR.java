package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.PR;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units.PREdge;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units.PRNode;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ClusterUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PR extends AbstractTask implements Rank, ObservableTask {
    private ClusterManager manager;
    final public static String NAME = "Create rank from the PageRank algorithm ";
    final public static String SHORTNAME = "PR";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public PRContext context;

    private Hypergraph<PRNode, PREdge> graph;
    private HashMap<Long, PRNode> idToNode;
    private List<CyNode> nodeList;
    private List<CyEdge> edgeList;
    private CyTable nodeTable;
    private CyTable edgeTable;
    private List<String> edgeAttributes;
    private AbstractClusterResults results;

    public PR(PRContext context, ClusterManager manager) {
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
    @ProvidesTitle
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
        taskMonitor.setTitle("PR ranking of clusters");
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

        // Normalize the scores based on the source node
        normalizeEdges();

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Calculating PageRank scores");
        PageRank<PRNode, PREdge> pageRank = performPageRank();
        taskMonitor.setProgress(0.8);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Inserting scores into clusters");
        insertScores(clusters, pageRank);
        taskMonitor.setProgress(0.9);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Insert cluster information in tables");
        ClusterUtils.insertResultsInColumns(network, clusters, SHORTNAME);
        results = new AbstractClusterResults(network, clusters);

        taskMonitor.setProgress(1.0);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    @Override
    public List<Class<?>> getResultClasses() {
        return results.getResultClasses();
    }

    @Override
    public <R> R getResults(Class<? extends R> clzz) {
        return results.getResults(clzz);
    }

    private void normalizeEdges() {
      // The PageRank algorithm requires that edge weights represent a transition probability -- that is,
      // they must sum to 1.  We need to adjust our edges to reflect that
      for (PRNode node: graph.getVertices()) {
        double sum = 0d;
        for (PREdge edge: graph.getOutEdges(node)) {
          sum += edge.getScore();
        }

        for (PREdge edge: graph.getOutEdges(node)) {
          edge.setScore(edge.getScore()/sum);
        }
      }
    }

    private void insertScores(List<NodeCluster> clusters, PageRank<PRNode, PREdge> pageRank) {
        for (PRNode node : graph.getVertices()) {
            node.setPRScore(pageRank.getVertexScore(node));

            for (NodeCluster cluster : clusters) {
                if (cluster.getNodeScores().containsKey(node.getCyNode().getSUID())) {
                    cluster.addScoreToAvg(pageRank.getVertexScore(node));
                }
            }
        }
    }

    private PageRank<PRNode, PREdge> performPageRank() {
        PageRank<PRNode, PREdge> pageRank = new PageRank<>(graph, transformEdge(), context.getAlpha());
        pageRank.setMaxIterations(1000);
        pageRank.evaluate();
        return pageRank;
    }

    private void addEdges() {
				Map<CyIdentifiable, double[]> edgeMap = null;
        context.normalizationContext.normalize(edgeAttributes, edgeList);
        if (edgeAttributes.size() > 0 && !edgeAttributes.get(0).equals(ModelUtils.NONEATTRIBUTE))
          edgeMap = context.normalizationContext.normalize(edgeAttributes, edgeList);
        for (CyEdge edge : edgeList) {
            PRNode sourceNode = idToNode.get(edge.getSource().getSUID());
            PRNode targetNode = idToNode.get(edge.getTarget().getSUID());
            PREdge prEdge = new PREdge(edge);
            if (edgeMap != null)
              insertEdgeScore(prEdge, edgeMap.get(edge));
            else
              prEdge.setScore(0.0d);
            if (edge.isDirected())
              graph.addEdge(prEdge, new Pair<>(sourceNode, targetNode), EdgeType.DIRECTED);
            else
              graph.addEdge(prEdge, new Pair<>(sourceNode, targetNode), EdgeType.UNDIRECTED);
        }
    }

    private void addNodes() {
        for (CyNode node : nodeList) {
            PRNode prNode = new PRNode(node);
            graph.addVertex(prNode);
            idToNode.put(node.getSUID(), prNode);
        }
    }

    private void initVariables() {
        edgeAttributes = context.getSelectedEdgeAttributes();

        graph = new DirectedSparseMultigraph<>();
        idToNode = new HashMap<>();
        nodeList = network.getNodeList();
        edgeList = network.getEdgeList();
        nodeTable = network.getDefaultNodeTable();
        edgeTable = network.getDefaultEdgeTable();
    }


    private void insertEdgeScore(PREdge prEdge, double[] edgeAttrValues) {
        double totalEdgeScore = 0.0d;

				for (double value: edgeAttrValues) {
					totalEdgeScore += value;
				}

        prEdge.setScore(totalEdgeScore);
    }

    private Function<PREdge, Double> transformEdge() {
        return PREdge::getScore;
    }
}

/*
 * Created on 25. September 2007
 * 
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure;

import java.util.ArrayList;
import java.util.Hashtable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.fixedparameterclustering.FixedParameterTreeNode;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.greedy.GreedyClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.IPostProcessing;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PostProcessingFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidTypeException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * This class describes a connected component of a graph.
 * 
 * @author Sita Lange
 */
public class ConnectedComponent {

	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		ConnectedComponent clone = new ConnectedComponent(this.ccEdges.clone(), this.objectIDs.clone(), this.ccPath);
		clone.ccPositions = this.ccPositions.clone();
		return clone;
	}

	private ConnectedComponent reducedConnectedComponent = null;
	
	public ConnectedComponent getReducedConnectedComponent() {
		return reducedConnectedComponent;
	}

	public void setReducedConnectedComponent(
			ConnectedComponent reducedConnectedComponent) {
		this.reducedConnectedComponent = reducedConnectedComponent;
	}

	/* edges of the graph with weights */
	private ICCEdges ccEdges = null;

	/* positions of the vertices; not initialised at instance creation */
	private double[][] ccPositions = null;

	/*
	 * IDs of the objects in the correct order according to the starting
	 * allocation
	 */
	private String[] objectIDs = null;

	/* total number of nodes in the component */
	private int node_no = -1;

	/* cluster number for object in the same order as in objectIDs and is initialised at instance creation*/
	private int[] clusters = null;

	/* score for the clustering done on this connected component */
	private double clusteringScore = -1;

	/* total number of clusters for this connected component */
	private int numberOfClusters = -1;

	/*
	 * number of objects in each cluster, which have the same value as in the
	 * clusters array and needs to be initialised and filled after clustering process
	 */
	private int[] clusterDistribution = null;
	
	/* path to the cost matrix of this ConnectedComponent instance */
	private String ccPath = "";
	
//	/* boolean for reduced matrices */
//	private boolean isReduced = false;
	
	/* the costs that were accumulated during the reduction process.
	 * this needs to be added to the clustering score at the end! 
	 */
	private double reductionCost = 0.0;

	public ConnectedComponent(ICCEdges ccEdges, String[] object_ids, 
			String ccPath) {

		this.ccEdges = ccEdges;
		this.objectIDs = object_ids;
		this.node_no = object_ids.length;
		this.clusters = new int[node_no];
		this.ccPath = ccPath;
//		this.ccEdges.normalise();
		
		
//		if(TaskConfig.dummy==null){
//			TaskConfig.dummy = new Hashtable<Integer,Vector<Integer>>();
//		}
		
//		long time = System.currentTimeMillis();
//		int reducedsize = node_no;
//		if(buildReducedCC()){
//			reducedsize = this.reducedConnectedComponent.getNodeNumber();
//		}
//		if(!TaskConfig.dummy.containsKey(node_no)){
//			Vector<Integer> v = new Vector<Integer>();
//			TaskConfig.dummy.put(node_no, v);
//		}
//		TaskConfig.dummy.get(node_no).add(reducedsize);
		
//		System.out.println(node_no + "\t" + reducedsize + "\t" + (System.currentTimeMillis()-time));
		
		if(this.node_no<200) buildReducedCC();
		
		
	}
	
	private boolean buildReducedCC() {
		
		
		ConnectedComponent ccCopy = this.copy();
		new GreedyClusterer(ccCopy);
		IPostProcessing pp = PostProcessingFactory.EnumPostProcessingClass.
		PP_REARRANGE_AND_MERGE_BEST.createPostProcessor();
		pp.initPostProcessing(ccCopy);
		/* run post processing */
		pp.run();
		
		
		ConnectedComponent ccCopy2 = this.copy();
		ccCopy2.setClusteringScore(Double.MAX_VALUE);
//		new TreeClusterer(ccCopy2);
//		new TreeClusterer(ccCopy2);
//		IPostProcessing pp2 = PostProcessingFactory.EnumPostProcessingClass.
//		PP_REARRANGE_AND_MERGE_BEST.createPostProcessor();
//		pp2.initPostProcessing(ccCopy2);
//		/* run post processing */
//		pp2.run();
		
		
		
		if(ccCopy.getClusteringScore()<ccCopy2.getClusteringScore()){
			boolean isChanged = reduceCC(ccCopy, (float) (Math.rint(ccCopy.getClusteringScore()*1000)+1)/1000);
			if(isChanged) {
				this.reducedConnectedComponent = ccCopy;
				if(ccCopy.buildReducedCC()){
					this.reducedConnectedComponent = ccCopy.reducedConnectedComponent;
				}
			}
			return isChanged;
		}else{
			boolean isChanged = reduceCC(ccCopy2, (float) (Math.rint(ccCopy2.getClusteringScore()*1000)+1)/1000);
			if(isChanged) {
				this.reducedConnectedComponent = ccCopy2;
				if(ccCopy2.buildReducedCC()){
					this.reducedConnectedComponent = ccCopy2.reducedConnectedComponent;
				}
			}
			return isChanged;
		}
		
		
		
	}

	public ConnectedComponent(ICCEdges ccEdges, String[] object_ids, 
			String ccPath, boolean reduced) {

		this.ccEdges = ccEdges;
		this.objectIDs = object_ids;
		this.node_no = object_ids.length;
		this.clusters = new int[node_no];
		this.ccPath = ccPath;

	}

	/**
	 * This class checks whether two positions are equal. NOTE both input arrays
	 * must be of equal size for this method to work correctly, but in the
	 * context of this program they always will be. This method only takes long
	 * if the positions are equal, as soon as one axis-position is unequal it
	 * returns false.
	 * 
	 * @param pos_a
	 *            First position in a double array.
	 * @param pos_b
	 *            Second position in a double array.
	 * @return boolean if it is equal or not.
	 */
	public boolean isPositionEqual(double[] pos_a, double[] pos_b) {
		// if(pos_a.length != pos_b.length) return false;
		for (int i = 0; i < pos_a.length; i++) {
			if (pos_a[i] != pos_b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the ICCEdges object where the edge costs are saved in.
	 * 
	 * @return The object with the edge costs.
	 */
	public ICCEdges getCCEdges() {
		return ccEdges;
	}

	/**
	 * Sets the ICCEdges object where the edge costs are saved in.
	 * 
	 * @param ccEdges
	 *            The object with the edge costs.
	 */
	public void setCCEdges(ICCEdges ccEdges) {
		this.ccEdges = ccEdges;
	}

	/**
	 * Gets the array with the node positions, which is a 2-dimensional double
	 * array where each row i represents the position for node i. The size of
	 * the array is no. of nodes x dimension.
	 */
	public double[][] getCCPositions() {
		return ccPositions;
	}

	/**
	 * Gets the  node position of node i. This will be returned as double array where the size is the dimension
	 * @param i Node i
	 * @return double array position
	 */
	public double[] getCCPostions(int i){
		return ccPositions[i];
	}
	
	/**
	 * Sets the array with the node positions, which is a 2-dimensional double
	 * array where each row i represents the position for node i. The size of
	 * the array is no. of nodes x dimension.
	 * 
	 * @param ccPositions
	 *            The array with the node positions.
	 */
	public void setCCPositions(double[][] ccPositions) {
		this.ccPositions = ccPositions;
	}

	/**
	 * Gets the array where the proper object IDs are saved in. The ordering in
	 * the array is equal to the integers that are used throughout the program
	 * to address a certain object/node.
	 * 
	 * @return The array where the proper object IDs are saved in.
	 */
	public String[] getObjectIDs() {
		return objectIDs;
	}

	/**
	 * Sets the array where the proper object IDs are saved in. The ordering in
	 * the array is equal to the integers that are used throughout the program
	 * to address a certain object/node.
	 * 
	 * @param objectIDs
	 *            The array where the proper object IDs are saved in.
	 */
	public void setObjectIDs(String[] objectIDs) {
		this.objectIDs = objectIDs;
	}

	/**
	 * Gets the number of nodes for the connected component.
	 * 
	 * @return The number of nodes for the connected component.
	 */
	public int getNodeNumber() {
		return node_no;
	}

	/**
	 * Sets the number of nodes for the connected component.
	 * 
	 * @param node_no
	 *            The number of nodes for the connected component.
	 */
	public void setNodeNumber(int node_no) {
		this.node_no = node_no;
	}
	
	/**
	 * Initialises the clusterDistribution int[] with the size of the number of clusters for this
	 * component. Also it sets the numberOfClusters class variable.
	 * @param noOfClusters The number of clusters for this instance.
	 */
	public void initialiseClusterInfo(int noOfClusters){
		this.clusterDistribution = new int[noOfClusters];
//		setNumberOfClusters(noOfClusters);
		this.numberOfClusters = noOfClusters;
	}
	
	/**
	 * sets the clusterDistribution int[] with the size for each cluster for this
	 * component. 
	 * @param clusterDistribution The distribution of clustersizes.
	 */
	public void setClusterInfo(int[] clusterDistribution){
		this.clusterDistribution = clusterDistribution;
	}
	
	/**
	 * gets the clusterDistribution int[] with the size of each cluster for this
	 * component. 
	 * @return clusterDistribution The distribution of clustersizes.
	 */
	public int[] getClusterInfo(){
		return this.clusterDistribution;
	}
	
	/**
	 * calculates the clusterDistribution int[] with the size of the number of clusters for this
	 * component. This method should only be used after clustering.
	 * 
	 */
	public void calculateClusterDistribution(){
		for (int i = 0; i < this.clusters.length; i++) {
			this.clusterDistribution[this.clusters[i]]++;
		}
	}
	
	/**
	 * Sets the clusters. The array clusters consists of the clusternumbers (between 0 and noOfCluster-1). 
	 * @param clusters array of clusters where position is the proteinnumber and the value the clusternumber
	 */
	public void setClusters(int[] clusters){
		this.clusters = clusters;
	}
	
	/**
	 * gets the clusters. The array clusters consists of the clusternumbers (between 0 and noOfCluster-1). 
	 * @return clusters array of clusters where position is the proteinnumber and the value the clusternumber
	 */
	public int[] getClusters(){
		return this.clusters;
	}

	/**
	 * Gets the number objects in the given cluster. 
	 * This method should only be used after the clustering has been performed. If
	 * this is not the case, -1 is returned.
	 * 
	 * @param cluster_no
	 *            The number of the cluster compliant with the numbering within
	 *            the connected component object.
	 * @return The number of objects in the given cluster.
	 */
	public int getClusterMagnitude(int cluster_no) {
		try {
			return clusterDistribution[cluster_no];
		} catch (NullPointerException ex) {
			// TODO print out in log file or handle this exception
			System.err
					.println("ERROR: Either the clusterDistribution variable in the"
							+ "ConnectedComponent object hasn't been initialised, or it doesn't"
							+ "contain the cluster number "
							+ cluster_no
							+ ". == "
							+ ex.getMessage());
			return -1;
		}
	}

	/**
	 * Sets the number of objects in the given cluster. 
	 * This method should only be used after the clustering has been performed.
	 * @param cluster_no The number of the cluster compliant with this instance.
	 * @param magnitude The number of objects in this cluster.
	 */
	public void setClusterMagnitude(int cluster_no, int magnitude) {
		try{
			this.clusterDistribution[cluster_no] = magnitude;
		} catch (NullPointerException ex){
			// TODO print out in log file or handle this exception
			System.err
					.println("ERROR: Either the clusterDistribution variable in the"
							+ "ConnectedComponent object hasn't been initialised, or it doesn't"
							+ "contain this cluster number "
							+ cluster_no
							+ ". == "
							+ ex.getMessage());
			
		}
	}
	

	/**
	 * Gets the cluster number to which the given object number (compliant with this instance) was assigned to.
	 * This method should only be used after the clustering has been performed.
	 * @param object_no The object number compliant with this instance.
	 * @return The cluster number to which this object was assigned to.
	 */
	public int getClusterNoForObject(int object_no) {
			return clusters[object_no];		
	}

	/**\
	 * Sets the cluster number to which the given object number (compliant with this instance) was assigned to.
	 * This method should only be used after the clustering has been performed.
	 * @param object_no The object number compliant with this instance.
	 * @param cluster_no The cluster number to which this object was assigned to.
	 */
	public void setClusterNoForObject(int object_no, int cluster_no) {
			this.clusters[object_no] = cluster_no;
	}

	/**
	 * Gets the total number of clusters for this instance.
	 * This method should only be used after the clustering has been performed.
	 * @return The number of clusters for this instance.
	 */
	public int getNumberOfClusters() {
		return numberOfClusters;
	}


	/**
	 * Gets the score for the clustering performed on this instance.
	 * This method should only be used after the clustering has been performed.
	 * @return The score for the clustering done on this instance.
	 */
	public double getClusteringScore() {
		return clusteringScore;
	}

	/**
	 * Sets the score for the clustering performed on this instance.
	 * This method should only be used after the clustering has been performed.
	 * @param score The score for the clustering done on this instance.
	 */
	public void setClusteringScore(double score) {
		this.clusteringScore = score;
	}
	
	/**
	 * Calculate the score for the clustering performed on this instance.
	 * This method should only be used after the clustering has been performed.
	 * @return score The score for the clustering done on this instance.
	 * @param clusters The clustering obtained e.g. from singleLinkageClustering
	 */
	public double calculateClusteringScore(int[] clusters) {
		double score = 0;
		double edgeCost;
		for (int i = 0; i < clusters.length; i++) {
			for (int j = i+1; j < clusters.length; j++) {
				edgeCost = this.ccEdges.getEdgeCost(i, j);
//				boolean sameCluster = (clusters[i]==clusters[j]);
                                if(clusters[i]!=clusters[j]){
                                    if(edgeCost>0){
                                        score+=edgeCost;
                                    }
                                }else if(edgeCost<0){
                                    score-=edgeCost;
                                }
//				if(!sameCluster&&edgeCost>0){   //node_i and node_j are not in the same cluster but there exists an edge between them
//					score+=edgeCost;
//				}else if(sameCluster&&edgeCost<0){  //node_i and node_j are  in the same cluster but there exists no edge between them
//					score-=edgeCost;
//				}
			}		
		}
		
		return score;
	}
	
	public void printClusters(){
		System.out.println("Clusters:");
		for (int i = 0; i < clusters.length; i++) {
			System.out.println("Item "+i+" is in Cluster "+clusters[i]);
		}
	}
	
//	//TODO just for testing
//	public void printPositions(){
//		StringBuffer posBuff = new StringBuffer();
//		posBuff.append("\n==== CURRENT POSITIONS ====\n");
//		for(int n=0;n<getNodeNumber();n++){
//			posBuff.append("node ");
//			posBuff.append(n);
//			posBuff.append(": (");
//			double[] posn = getCCPostions(n);
//			for(int i=0;i<posn.length-1;i++){
//				posBuff.append(posn[i]);
//				posBuff.append(", ");
//			}
//			posBuff.append(posn[posn.length-1]);
//			posBuff.append(")\n");			
//		}
//		posBuff.append("====================\n");
//		
//		System.out.println(posBuff.toString());
//	}

	/**
	 * @return the ccPath
	 */
	public String getCcPath() {
		return ccPath;
	}
	
	/**
	 * Copies the connected component with only the information which it had
	 * at the initialisation. Since these properties stay the same, they share
	 * the same storage. The other properties are set to null, so the positions
	 * array still needs to be initialised!!
	 * @return A copy of this ConnectedComponent object.
	 */
	public ConnectedComponent copy(){
		ConnectedComponent newCC = new ConnectedComponent(this.ccEdges,
				this.objectIDs.clone(), this.ccPath,true);
		newCC.reductionCost = this.reductionCost;
		return newCC;
	}
	
	public ConnectedComponent copy(boolean withReduction){
		ConnectedComponent newCC = new ConnectedComponent(this.ccEdges,
				this.objectIDs, this.ccPath,true);
		newCC.reductionCost = this.reductionCost;
		try {
			newCC.setReducedConnectedComponent(this.getReducedConnectedComponent().copy());
		} catch (Exception e) {
		}
		return newCC;
	}
	
	
	public double[][] copyCCPositions(){
		int dim = TaskConfig.dimension;
		double[][] copiedPos = new double[this.node_no][dim];
		for(int i=0;i<this.node_no;i++){
			for(int j=0;j<dim;j++){
				copiedPos[i][j] = this.ccPositions[i][j];
			}
		}
		
		return copiedPos;
	}
	
	/**
	 * Returns the id of an object.
	 * @param num
	 * @return the id for the specified number
	 */
	public String getObjectID(int num) {
		return objectIDs[num];
	}
	
//	/**
//	 * Returns the boolean if this ConnectedComponent object is reduced
//	 * or not. A reduced matrix has merged all objects into one node that share
//	 * a similarity above a given threshold. This means the object name can consist
//	 * of several single names that are tab delimited.
//	 * @return boolean If the ConnectedComponent is reduced or not.
//	 */
//	public boolean isReduced(){
//		return isReduced;
//	}
//	
//	/** 
//	 * Sets the isReduced tag to the given boolean value.
//	 * @param isReduced If the matrix has been reduced or not.
//	 */
//	public void setIsReducedTag(boolean isReduced){
//		this.isReduced = isReduced;
//	}
	
	/**
	 * This method is to take the given cluster number and create a new ConnectedComponent object
	 * for the nodes of this cluster. This instance shares the {@link ICCEdges} object to save space!
	 * Instead of containing the actual object IDs as a string array, the original node numbers
	 * are saved in objectIDs.
	 * @param clusterNo This is the number of the cluster.
	 * @param intsInCluster The integer values that occur in the cluster.
	 * @return The ConnectedComponent object for the given cluster.
	 */
	public ConnectedComponent createConnectedComponentForCluster(int clusterNo, ArrayList<Integer> intsInCluster) {
		
		int size = intsInCluster.size();
		
		/* instead of the object IDs, the original node numbers are saved here as strings 
		 *	needed for end clustering */
		String[] subIDs = new String[size];
		for (int i = 0; i < size; i++) {
			subIDs[i] = intsInCluster.get(i).toString();
		}
		
		try {
			ICCEdges subEdges = LayoutFactory.getCCEdgesEnumByClass(TaskConfig.ccEdgesClass).createCCEdges(size);
			for (int i = 0; i < subIDs.length; i++) {
				for (int j = 0; j < i; j++) {
					subEdges.setEdgeCost(i, j, this.ccEdges.getEdgeCost(Integer.parseInt(subIDs[i]), Integer.parseInt(subIDs[j])));
				}			
			}

		ConnectedComponent subCC = new ConnectedComponent(subEdges, subIDs, this.ccPath); 
		
		return subCC;
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	/**
	 * Gets the cost that accured when merging nodes above the user-given upper bound. 
	 * @return the reductionCost
	 */
	public double getReductionCost() {
		return reductionCost;
	}

	/**
	 * Gets the cost that accured when merging nodes above the user-given upper bound. 
	 * @param reductionCost the reductionCost to set
	 */
	public void setReductionCost(double reductionCost) {
		this.reductionCost = reductionCost;
	}

	private boolean reduceCC(ConnectedComponent cc,
			float clusteringScore2) {
		
		
		FixedParameterTreeNode fptn = initFirstTreeNode(cc, clusteringScore2);
		
		if(fptn.size==cc.getNodeNumber()) return false;
		
		ICCEdges ccedges = TaskConfig.ccEdgesEnum.createCCEdges(fptn.size);
		for (int i = 0; i < fptn.size; i++) {
			for (int j = i+1; j < fptn.size; j++) {
				ccedges.setEdgeCost(i, j,  fptn.edgeCosts[i][j]);
			}
		}
		
		String objectIds[] = new String[fptn.size];
		
		for (int i = 0; i < objectIds.length; i++) {
			String id = "";
			for (int j = 0; j < fptn.clusters[i].length; j++) {
				if(fptn.clusters[i][j]){
					id+=cc.getObjectID(j)+";";
				}
			}
			objectIds[i] = id;
		}
		
		cc.setNodeNumber(fptn.size);
		cc.setCCEdges(ccedges);
		cc.setObjectIDs(objectIds);
		cc.setClusters(new int[fptn.size]);
		cc.setReductionCost(fptn.costs);
		
		return true;
	}

	public void rebuildCC() {
		
		this.initialiseClusterInfo(this.reducedConnectedComponent.getNumberOfClusters());
		
		int clusters[] = new int[this.getNodeNumber()];
		
		Hashtable<String, Integer> h = new Hashtable<String, Integer>();
		for (int i = 0; i < this.getNodeNumber(); i++) {
			h.put(this.getObjectID(i), i);
		}
		
		for (int j = 0; j < this.reducedConnectedComponent.getNodeNumber(); j++) {
			String id =this.reducedConnectedComponent.getObjectID(j);
			String[] split = id.split(";");
			for (int i = 0; i < split.length; i++) {
				if(split[i].trim().equals("")) continue;
				clusters[h.get(split[i])] = this.reducedConnectedComponent.getClusters()[j];
			}
		}
		
		
		
		this.setClusteringScore(this.calculateClusteringScore(clusters));
		this.setClusters(clusters);
		this.calculateClusterDistribution();
	}
	
	public static float calculateCostsForMerging(FixedParameterTreeNode fptn,
			int node_i, int node_j) {
		float costsForMerging = 0;

		for (int i = 0; i < fptn.size; i++) {
			if (i == node_i || i == node_j)
				continue;
			if ((fptn.edgeCosts[i][node_i] > 0 && fptn.edgeCosts[i][node_j] > 0)
					|| (fptn.edgeCosts[i][node_i] <= 0 && fptn
							.edgeCosts[i][node_j] <= 0))
				continue;
			costsForMerging += Math.min(Math
					.abs(fptn.edgeCosts[i][node_i]), Math.abs(fptn
					.edgeCosts[i][node_j]));
		}
		if(fptn.edgeCosts[node_i][node_j]< 0) costsForMerging-=fptn.edgeCosts[node_i][node_j];
		return costsForMerging;
	}

	public static float calculateCostsForSetForbidden(FixedParameterTreeNode fptn,
			int node_i, int node_j) {

		float costs = 0;

		for (int i = 0; i < fptn.size; i++) {
			if (fptn.edgeCosts[node_i][i] > 0
					&& fptn.edgeCosts[node_j][i] > 0) {
				costs += Math.min(fptn.edgeCosts[node_i][i], fptn
						.edgeCosts[node_j][i]);
			}
		}
		if(fptn.edgeCosts[node_i][node_j]>0) costs += fptn.edgeCosts[node_i][node_j];
		return costs;
	}

	

	
	public static FixedParameterTreeNode initFirstTreeNode(ConnectedComponent cc, double maxK) {

		FixedParameterTreeNode fptn = new FixedParameterTreeNode(cc
				.getNodeNumber(), 0, cc.getNodeNumber());

		for (int i = 0; i < fptn.size; i++) {
			fptn.clusters[i][i] = true;
			for (int j = i + 1; j < fptn.size; j++) {
				fptn.edgeCosts[i][j] = fptn.edgeCosts[j][i] = cc
						.getCCEdges().getEdgeCost(i, j);
			}
		}
		fptn = reductionicf(fptn,maxK,cc);
		return fptn;
	}

	public static FixedParameterTreeNode mergeNodes(FixedParameterTreeNode fptn,
			int node_i, int node_j, float costsForMerging,ConnectedComponent cc) {

		FixedParameterTreeNode fptnNew = new FixedParameterTreeNode(
				fptn.size - 1, fptn.costs, cc.getNodeNumber());
		fptnNew.costs = (fptn.costs + costsForMerging);

		int mappingOld2New[] = new int[fptn.size];
		for (int i = 0, j = 0; i < fptn.size; i++) {
			if (i == node_i || i == node_j)
				continue;
			mappingOld2New[i] = j;
			fptnNew.clusters[j] = fptn.clusters[i];
			j++;
		}

		for (int i = 0; i < mappingOld2New.length; i++) {
			if (i == node_i || i == node_j)
				continue;
			for (int j = i + 1; j < mappingOld2New.length; j++) {
				if (j == node_i || j == node_j)
					continue;
				fptnNew.edgeCosts[mappingOld2New[i]][mappingOld2New[j]] = fptnNew
						.edgeCosts[mappingOld2New[j]][mappingOld2New[i]] = fptn
						.edgeCosts[i][j];
			}
		}

		for (int i = 0; i < cc.getNodeNumber(); i++) {
			fptnNew.clusters[fptnNew.size - 1][i] = (fptn.clusters[node_i][i] || fptn
					.clusters[node_j][i]);
		}

		for (int i = 0; i < fptn.size; i++) {
			if (i == node_i || i == node_j)
				continue;
			fptnNew.edgeCosts[mappingOld2New[i]][fptnNew.size - 1] = fptnNew
					.edgeCosts[fptnNew.size - 1][mappingOld2New[i]] = fptn
					.edgeCosts[i][node_i]
					+ fptn.edgeCosts[i][node_j];
		}
		return fptnNew;
	}

	public static FixedParameterTreeNode reductionicf(FixedParameterTreeNode fptnNew, double maxK,ConnectedComponent cc) {

		for (int i = 0; i < fptnNew.size; i++) {
			for (int j = i + 1; j < fptnNew.size; j++) {
				float sumIcf = calculateCostsForSetForbidden(fptnNew, i, j);
				if (sumIcf + fptnNew.costs > maxK) {
					float costsForMerging = calculateCostsForMerging(fptnNew,
							i, j);
					FixedParameterTreeNode fptnNew2 = mergeNodes(fptnNew, i, j,
							costsForMerging,cc);
					fptnNew2 = reductionicf(fptnNew2,maxK,cc);
					return fptnNew2;
				} 
			}
		}
		return fptnNew;
	}

	
}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.Edges;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.gui.Console;

public class Splitter {
	
	private  float threshold;
		
	public void run(HashMap<Integer, String> proteins2integers, HashMap<String, Integer> integers2proteins) throws IOException {
	
		this.threshold = Config.threshold;
		
		if(Config.gui)	Console.println("Start reading similarity file ... ");
		//else // System.out.println("Start reading similarity file ... ");
		Edges es = InOut.readSimilarityFile(Config.similarityFile, proteins2integers, integers2proteins);
		if(Config.gui)Console.println();
		//else // System.out.println();
		
		if(Config.gui){
			Console.println("Start splitting ...");
			Console.setBarValue(0);
			Console.setBarText("splitting into connected components");
		}//else // System.out.println("Start splitting ...");
		
		Vector<Vector<Integer>> clusters = splitIntoConnectedComponents(es, proteins2integers,threshold,false);
		if(Config.gui) Console.println();
		//else // System.out.println();
		
		if(Config.gui){
			Console.println("Writing costmatrices ...");
			Console.setBarValue(0);
			Console.restartBarTimer();
			Console.setBarText("writing costmatrices");
		}//else // System.out.println("Writing costmatrices ...");
		
		InOut.writeCostMatrices(es, clusters, proteins2integers, integers2proteins);
		if(Config.gui) Console.println();
		//else // System.out.println();
		
	}
	
	public static Vector<Vector<Integer>> splitIntoConnectedComponents(Edges es, HashMap<Integer,String> proteins2integers,float threshold,boolean mergenNodes){
		
		Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
		int[] distribution = new int[es.size2()+1];
		boolean[] already = new boolean[es.size2()];
		for (int i = 0; i < already.length; i++) {
			if(!already[i]){
				Vector<Integer> cluster = new Vector<Integer>();
				cluster.add(i);
				already[i] = true;
				findCluster(es, cluster,proteins2integers,i,already,threshold,mergenNodes);
				v.add(cluster);
				distribution[cluster.size()]++;
			}
		}
		
//		for (int i = 0; i < distribution.length; i++) {
//			if(distribution[i]!=0){
//				Console.println(i + "\t" + distribution[i]);
//			}
//		}
			
		return v;
		
	}
	
	private static void findCluster(Edges es, Vector<Integer> cluster, HashMap<Integer, String> proteins2integers, Integer element, boolean[] already, float threshold,boolean mergeNodes) {

		int startPosition=0;
		int endPosition = 0;

		if(mergeNodes){
			startPosition = es.getStartPosition(Integer.parseInt(proteins2integers.get(element)));
			endPosition = es.getEndPosition(Integer.parseInt(proteins2integers.get(element)));
		}else{
			startPosition = es.getStartPosition(element);
			endPosition = es.getEndPosition(element);
		}
		
		//TODO
//		if(Config.defaultCostsForMissingEdges>threshold){
//			for (int i = 1; i < es.proteinNumber; i++) {
//				cluster.add(i);
//				already[i]= true;
//			}
//		}
//		
//		
//		
//		if(Config.useMinSimilarityOfBothDirections){
//			for (int i = startPosition; i <=endPosition; i++) {
//				if(endPosition==-1)	continue;
//				int target = es.getTarget(i);
//				if(already[target]) continue;
//				if(InOut.getEdgeValue(element, target, es)>threshold){
//					cluster.add(target);
//					already[target]= true;
//					findCluster(es, cluster,proteins2integers, target, already, threshold,mergeNodes);
//				}
//			}
//		}else{
//			for (int i = 0; i < es.proteinNumber; i++) {
//				if(already[i]) continue;
//				if(InOut.getEdgeValue(element, i, es)>threshold){
//					cluster.add(i);
//					already[i]= true;
//					findCluster(es, cluster,proteins2integers, i, already, threshold,mergeNodes);
//				}
//			}
//		}
		
		
		
		for (int i = startPosition; i < endPosition; i++) {
			int target = es.getTarget(i);
			if(!already[target]){
				double value = es.getValue(i);
				if(value>threshold){
					cluster.add(target);
					already[target]= true;
					findCluster(es, cluster,proteins2integers, target, already, threshold,mergeNodes);
				}
			}
		}
		
	}// end findClusters

}

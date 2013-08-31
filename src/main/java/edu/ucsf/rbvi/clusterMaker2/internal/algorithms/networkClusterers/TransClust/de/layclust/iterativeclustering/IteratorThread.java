package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.iterativeclustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.TransClustCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.clusteranalysis.Fmeassure;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.Edges;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.ArgsParseException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.Config;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.InOut;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.Splitter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ICCEdges;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.ClusteringManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.ClusteringTask;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidInputFileException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidTypeException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;


public class IteratorThread extends Thread {
	
	// private static Logger log = Logger.getLogger(ClusteringTask.class.getName());
	private float upperBound;
	private Edges es;
	private HashMap<Integer,String>  proteins2integers;
	private HashMap<String,Integer> integers2proteins;
	public StringBuffer resultsStringBuffer;
	public double dummyCosts= 0;
	private Semaphore s;
	
	public IteratorThread(float upperBound){
		this.upperBound = upperBound;
	}
	
	public IteratorThread(){
		upperBound = TaskConfig.upperBound;
	}
	
	public IteratorThread(Edges es, HashMap<String,Integer> integers2proteins, HashMap<Integer,String>  proteins2integers, Semaphore s){
		upperBound = TaskConfig.upperBound;
		this.es = es;
		this.proteins2integers = proteins2integers;
		this.integers2proteins = integers2proteins;
		this.s = s;
	}
	
	 @Override
	public void run(){
//		boolean dummy1 = TaskConfig.useThreads;
//		TaskConfig.useThreads = false;
		try{
			Hashtable<String,Hashtable<String,Boolean>> clusterReference =null;
			HashMap<String, String> referenceHash = new HashMap<String, String>();
			if(TaskConfig.goldstandardPath!=null){
				clusterReference = readGoldStandardFile();
				for (String key: clusterReference.keySet()) {
					Hashtable<String, Boolean> h = clusterReference.get(key);
					for (String id : h.keySet()) {
						referenceHash.put(id, key);
					}
				}
			}
			boolean dummy = Config.createSimilarityFile;
			Config.createSimilarityFile = false;
//			HashMap<String,Integer> integers2proteins = new HashMap<String, Integer>();
//			HashMap<Integer,String>  proteins2integers = new HashMap<Integer, String>();
//			Edges es = InOut.readSimilarityFile(Config.similarityFile, proteins2integers, integers2proteins);
		
			if(clusterReference!=null){
//				System.out.println(clusterReference.size());
				ArrayList<String> dummyRemove2 = new ArrayList<String>();
				for (String key: clusterReference.keySet()) {
					Hashtable<String, Boolean> h = clusterReference.get(key);
					ArrayList<String> dummyRemove = new ArrayList<String>();
					for (String string: h.keySet()) {
						if(!integers2proteins.containsKey(string)){
							dummyRemove.add(string);
//							System.out.println(string);
						}else{
//							System.out.println("\t" + string);
						}
					}
					for (String string : dummyRemove) {
						h.remove(string);
					}
					if(h.isEmpty()) dummyRemove2.add(key);
				}
				for (String string : dummyRemove2) {
					clusterReference.remove(string);
				} 
//				System.out.println(clusterReference.size());
//				System.out.println();
				
			}
			
			
			if(TaskConfig.knownAssignmentsFile!=null){
				HashMap<String, Boolean> knownAssignments = new HashMap<String, Boolean>();
				Vector<Vector<String>> v = new Vector<Vector<String>>();
				BufferedReader knownAssignmentsReader = new BufferedReader(new FileReader(TaskConfig.knownAssignmentsFile));
				String line;
				String old = "";
				Vector<String> v2 = new Vector<String>();
				boolean isTwocolumn = false;
				while((line=knownAssignmentsReader.readLine())!=null){
					if(line.trim().equals("")) continue;
					String tabs[] = line.split("\t");
					if(tabs.length==2){
						isTwocolumn = true;
						if(old.equals("")){
							old = tabs[1];
							v2 = new Vector<String>();
							v2.add(tabs[0]);
						}else if(tabs[1].equals(old)){
							v2.add(tabs[0]);
						}else{
							old = tabs[1];
							v.add((Vector<String>) v2.clone());
							v2 = new Vector<String>();
							v2.add(tabs[0]);
							
						}
					}else if(tabs.length==3){
						String key = integers2proteins.get(tabs[0]) + "#" + integers2proteins.get(tabs[1]);
						if(tabs[2].equals("1")){
							knownAssignments.put(key, true);
						}else if(tabs[2].equals("-1")){
							knownAssignments.put(key, false);
						}
					}
				}
				knownAssignmentsReader.close();
				if(isTwocolumn){
					v.add(v2);
					for (Vector<String> vector : v) {
						for (int i = 0; i < vector.size(); i++) {
							for (int j = i+1; j < vector.size(); j++) {
								String source = vector.get(i);
								String target = vector.get(j);
								String key = integers2proteins.get(source)+"#" +integers2proteins.get(target) ;
								knownAssignments.put(key, true);
							}
						}
					}
				}
				for (int i = 0; i < es.size(); i++) {
					int source = es.getSource(i);
					int target = es.getTarget(i);
					String key1 = source + "#" + target;
					String key2 = target + "#" + source;
					if(knownAssignments.containsKey(key1)){
						if(knownAssignments.get(key1)){
							es.setValue(i, Float.POSITIVE_INFINITY);
						}else{
							es.setValue(i, -10000);
						}
					}else if(knownAssignments.containsKey(key2)){
						if(knownAssignments.get(key2)){
							es.setValue(i, Float.POSITIVE_INFINITY);
						}else{
							es.setValue(i, -10000);
						}
					}
				}
				
				
				
			}
			
			
			
			
			Config.createSimilarityFile = dummy;
			if(TaskConfig.mode==TaskConfig.COMPARISON_MODE){
//				BufferedWriter bw = new BufferedWriter(new FileWriter(TaskConfig.clustersPath));
				resultsStringBuffer = new StringBuffer();
				TaskConfig.monitor.setStatusMessage("");
				TaskConfig.monitor.setStatusMessage("");
				TaskConfig.monitor.setStatusMessage("Clustering Mode: cluster iterativ");
				TaskConfig.monitor.setStatusMessage("");
				TaskConfig.monitor.setStatusMessage("Threshold range: " + TaskConfig.minThreshold + " to " + TaskConfig.maxThreshold + "");
				TaskConfig.monitor.setStatusMessage("Stepsize: " + TaskConfig.thresholdStepSize + "");
				TaskConfig.monitor.setStatusMessage("");
				TaskConfig.monitor.setStatusMessage("");
				
				for (double threshold = TaskConfig.minThreshold; threshold <= TaskConfig.maxThreshold; threshold=Math.rint((threshold+TaskConfig.thresholdStepSize)*100000)/100000) {
					
					TaskConfig.monitor.setStatusMessage("calculating clusters for threshold " + threshold + "");
					TaskConfig.monitor.setStatusMessage("");
					Vector<Vector<Integer>> v = Splitter.splitIntoConnectedComponents(es, proteins2integers, (float) threshold, false);
					Vector<Vector<Integer>> mergableNodes = Splitter.splitIntoConnectedComponents(es, proteins2integers, upperBound, false);
//					for (int i = 0; i < es.targets.length; i++) {
//						System.out.println(es.sources[i] + "\t" + es.targets[i] + "\t" + es.values[i]);
//					}
//					
//					for (int i = 0; i < es.startPositions.length; i++) {
//						System.out.println(es.startPositions[i] + "\t" + es.endPositions[i]);
//					}
//					
//					System.out.println(v.size());
					
					Hashtable<Integer, Vector<Integer>> mergedNodes = new Hashtable<Integer, Vector<Integer>>();
					for (int i = 0; i < mergableNodes.size(); i++) {
						Vector<Integer> v2 = mergableNodes.get(i);
						for (int j = 0; j < v2.size(); j++) {
							mergedNodes.put(v2.get(j),v2);
						}
					}
					Vector<ConnectedComponent> connectedComponents = new Vector<ConnectedComponent>();
					boolean already[] = new boolean[proteins2integers.size()];
					for (Vector<Integer> vector : v) {
						int count = 0;
						Vector<Integer> representants = new Vector<Integer>();
						for (int i = 0; i < vector.size(); i++) {
							if(!already[vector.get(i)]){
								representants.add(vector.get(i));
								Vector<Integer> v2 = mergedNodes.get(vector.get(i));
								for (int j = 0; j < v2.size(); j++) {
									already[v2.get(j)] = true;
								}
								count++;
							}
						}
						ICCEdges cc2d2 = TaskConfig.ccEdgesEnum.createCCEdges(count);
						String[] ids = new String[count];
						Arrays.fill(ids, "");
						for (int i = 0; i < representants.size(); i++) {
							Vector<Integer> merged1 = mergedNodes.get(representants.get(i));
							for (int j = 0; j < merged1.size(); j++) {
								if(j==0) ids[i]+=proteins2integers.get(merged1.get(j));
								else ids[i]+= "," +proteins2integers.get(merged1.get(j));
							}
							for (int j = i+1; j < representants.size(); j++) {
								Vector<Integer> merged2 = mergedNodes.get(representants.get(j));
								float costs = 0;
								for (int k = 0; k < merged1.size(); k++) {
									for (int k2 = 0; k2 < merged2.size(); k2++) {
										if(InOut.getEdgeValue(merged1.get(k), merged2.get(k2), es)<TaskConfig.lowerBound){
											costs-=100000;
										}else{
											costs+=(float) (InOut.getEdgeValue(merged1.get(k), merged2.get(k2), es)-threshold);
										}
									}
								}
								cc2d2.setEdgeCost(i, j, costs);
							}
						}
						
						ConnectedComponent cc = new ConnectedComponent(cc2d2,ids,null);
						connectedComponents.add(cc);
					}
					
					Hashtable<String,Hashtable<String,Boolean>> clusters = new Hashtable<String, Hashtable<String,Boolean>>();
					ClusteringManager cm = new ClusteringManager(null);
					ArrayList<Semaphore> allSemaphores = new ArrayList<Semaphore>();
					Semaphore maxThreadSemaphore = new Semaphore(TaskConfig.maxNoThreads, true);
					for(int i=0;i<connectedComponents.size();i++){
						Semaphore semaphore = new Semaphore(1);
						allSemaphores.add(semaphore);
						cm.runClusteringForOneConnectedComponent(connectedComponents.get(i), null, semaphore, maxThreadSemaphore,System.currentTimeMillis());
						int[] elements2cluster = connectedComponents.get(i).getClusters();
						
						for (int j = 0; j < connectedComponents.get(i).getNumberOfClusters(); j++) {
							Hashtable<String, Boolean> cluster = new Hashtable<String, Boolean>();
							for (int k = 0; k < elements2cluster.length; k++) {
								if(elements2cluster[k]==j){
									String ids[] = connectedComponents.get(i).getObjectID(k).split(",");
									for (int l = 0; l < ids.length; l++) {
										String dummyids[] = ids[l].split("_HSP");
										cluster.put(dummyids[0], true);
									}
									
								}
							}
							if(cluster.size()!=0){
								clusters.put(new Random().nextDouble()+"", cluster);
							}
						}
					}
					
					
					HashSet<String> singletons = new HashSet<String>();
					for (Iterator<String> iterator = clusters.keySet().iterator(); iterator
							.hasNext();) {
						String key = iterator.next();
						Hashtable<String,Boolean> h = clusters.get(key);
						if(h.size()==1){
							String id = h.keySet().iterator().next();
							singletons.add(id);
						}
						
					}
	
					
					
//					================Overlapping========================
//					
					
					if(TaskConfig.overlap){
						Vector<Vector<Integer>> clustersVector = new Vector<Vector<Integer>>();
						
						for (String key : clusters.keySet()) {
							Hashtable<String,Boolean> cluster = clusters.get(key);
							Vector<Integer> clusterVector = new Vector<Integer>();
							for (String id: cluster.keySet()) {
								clusterVector.add(integers2proteins.get(id));
							}
							clustersVector.add(clusterVector);
							
						}
						
						calculateOverlapping(clustersVector,es,threshold,new int[proteins2integers.size()]);
						clusters = new Hashtable<String, Hashtable<String,Boolean>>();
						int coun = 0;
						for (Vector<Integer> clusterVector : clustersVector) {
							Hashtable<String,Boolean> cluster = new Hashtable<String, Boolean>();
							for (Integer id : clusterVector) {
								cluster.put(proteins2integers.get(id), true);
							}
							clusters.put(coun+"",cluster);
							coun++;
						}
					}else if(TaskConfig.fuzzy){
						float fuzzy[][] = new float[clusters.size()][proteins2integers.size()];
						float simSum[] = new float[proteins2integers.size()];
						
						int row = 0;
						for (String key : clusters.keySet()) {
							Hashtable<String,Boolean> cluster = clusters.get(key);
							int column = 0;
							for (String id1 : integers2proteins.keySet()) {
								float sim = 0;
								for (String id2:cluster.keySet()) {
									if(id2.equals(id1))continue;
									else sim+=InOut.getEdgeValue(integers2proteins.get(id1), integers2proteins.get(id2), es);
								}
								sim/=cluster.size();
								fuzzy[row][column] = sim;
								simSum[column]+=sim;
								column++;
							}
							row++;
						}
						
						for (int i = 0; i < fuzzy[0].length; i++) {
							for (int j = 0; j < fuzzy.length; j++) {
								fuzzy[j][i]/=simSum[i];
							}
						}
						
						row = 0;
						int countoverlaps = 0;
						
						for (String key : clusters.keySet()) {
							Hashtable<String,Boolean> cluster = clusters.get(key);
							int column = 0;
							for (String id1 : integers2proteins.keySet()) {
								if(singletons.contains(id1)){
									column++;
									continue;
								}
								if(cluster.containsKey(id1)||cluster.size()==1) {
									column++;
									continue;
								}
								if(fuzzy[row][column]>TaskConfig.fuzzyThreshold){
									cluster.put(id1, true);
									countoverlaps++;
								}
								column++;
							}
							row++;
						}
					}else if(TaskConfig.UseLimitK){
						
						ICCEdges cc2d2 =TaskConfig.ccEdgesEnum.createCCEdges(proteins2integers.size());
						String[] ids = new String[proteins2integers.size()];
						for (String key : integers2proteins.keySet()) {
							ids[integers2proteins.get(key)] = key;
							for (String key2 : integers2proteins.keySet()) {
								if(key.equals(key2)) continue;
								cc2d2.setEdgeCost(integers2proteins.get(key2), integers2proteins.get(key), (InOut.getEdgeValue(integers2proteins.get(key2), integers2proteins.get(key), es)- (float) threshold));
							}
						}
						ConnectedComponent cc = new ConnectedComponent(cc2d2,ids,null);
						int[] elements2cluster = cc.getClusters();
						clusters = new Hashtable<String, Hashtable<String,Boolean>>();
						for (int j = 0; j < cc.getNumberOfClusters(); j++) {
							Hashtable<String, Boolean> cluster = new Hashtable<String, Boolean>();
							for (int k = 0; k < elements2cluster.length; k++) {
								if(elements2cluster[k]==j){
									cluster.put(ids[k], true);
								}
							}
							if(cluster.size()!=0){
								clusters.put(new Random().nextDouble()+"", cluster);
							}
						}
					}
					
					
					/* wait for all clustering tasks to finish */
					for (Semaphore s : allSemaphores) {
						try {
							s.acquire();
						} catch (InterruptedException e) {
							TaskConfig.monitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
							// e.printStackTrace();
						}
					}
					
					resultsStringBuffer.append(threshold + "\t");
					HashMap<String, String> clusterHash = new HashMap<String, String>();
					for (String key: clusters.keySet()) {
						Hashtable<String, Boolean> h = clusters.get(key);
						for (String id : h.keySet()) {
							clusterHash.put(id, key);
						}
					}
					if(clusterReference!=null){
						
//						for (Iterator iterator = referenceHash.keySet().iterator(); iterator
//								.hasNext();) {
//							String id = (String) iterator.next();
//							if(!clusterHash.containsKey(id)){
//								Hashtable<String, Boolean> h = new Hashtable<String, Boolean>();
//								h.put(id, true);
////								clusters.put(h.toString(), h);
//							}
//						}
						
						ArrayList<String> dummyRemove3 = new ArrayList<String>();
						for (String key : clusters.keySet()) {
							Hashtable<String, Boolean> h = clusters.get(key);
							ArrayList<String> dummyRemove = new ArrayList<String>();
							for (String string : h.keySet()) {
								if(!referenceHash.containsKey(string)){
									dummyRemove.add(string);
								}
							}
							for (String string : dummyRemove) {
								h.remove(string);
							}
							if(h.isEmpty()) dummyRemove3.add(key);
						}
						for (String string : dummyRemove3) {
							clusters.remove(string);
						} 
						
//						double BCubed = Fmeassure.BCubedPrecision(clusterReference, clusters);
//						double vidistance = Fmeassure.vidistance(clusterReference, clusters);
//						bw.write(vidistance+"\t");
//						TaskConfig.monitor.setStatusMessage("vidistance: " + vidistance);
						double fmeasure = Fmeassure.fMeassure(clusterReference,clusters);
//						double meanSilhouette = Fmeassure.silhouette(clusters,es,proteins2integers,integers2proteins);
//						double fmeasure2 = Fmeassure.fMeassure2(clusterReference,clusters);
						resultsStringBuffer.append(fmeasure+"\t");
//						TaskConfig.monitor.setStatusMessage(clusterReference.size() + "\t" + clusters.size());
						TaskConfig.monitor.setStatusMessage("fmeasure: " + fmeasure);
//						TaskConfig.monitor.setStatusMessage("meanSilhouette: " + meanSilhouette);
//						double ppv = Fmeassure.PPV(clusterReference, clusters);
//						TaskConfig.monitor.setStatusMessage("PPV = " + ppv);
//						double sensitivity = Fmeassure.Sensitivity(clusterReference, clusters);
//						TaskConfig.monitor.setStatusMessage("Sensitivity = " + sensitivity);
//						double accuracy = Math.sqrt(ppv*sensitivity);
//						TaskConfig.monitor.setStatusMessage("accuracy = " + accuracy);
//						double sepco = Fmeassure.ComplexWiseSeperation(clusterReference, clusters);
//						TaskConfig.monitor.setStatusMessage("sepco = " + sepco);
//						double sepcl = Fmeassure.ClusterWiseSeperation(clusterReference, clusters);
//						TaskConfig.monitor.setStatusMessage("sepcl = " + sepcl);
//						double seperation = Math.sqrt(sepco*sepcl);
//						TaskConfig.monitor.setStatusMessage("seperation = " + seperation);
//						TaskConfig.monitor.setStatusMessage("fmeasure2: " + fmeasure2);
//						TaskConfig.monitor.setStatusMessage("fmeasure3: " + fmeasure3);
//						System.out.println(threshold + "\t" + ppv + "\t" + sensitivity + "\t" + accuracy + "\t" + sepcl + "\t" + sepco + "\t" + seperation);
						
					}else{
						resultsStringBuffer.append("-\t");
					}
					
					int[] distribution = new int[1000000];
					int max = 0;
					int count = 0;
					boolean first= true;
					for (Iterator<String> iterator = clusters.keySet().iterator(); iterator
							.hasNext();) {
						String key =  iterator.next();
						Hashtable<String,Boolean> h = clusters.get(key);
						if(!first) resultsStringBuffer.append(";");
						first = true;
						for (Iterator<String> iterator2 = h.keySet().iterator(); iterator2.hasNext();) {
							String id = iterator2.next();
							if(first){
								first = false;
								resultsStringBuffer.append(id);
							}else resultsStringBuffer.append("," + id);
						}
						distribution[h.size()]++;
						count+=h.size();
						if(h.size()>max) max= h.size();
					}
					
					StringBuffer sb = new StringBuffer("cluster distribution: ");
					
					for (int i = max; i >=0 ; i--) {
						if(distribution[i]>0) sb.append(i + ":" + distribution[i] + ", ");
					}
					TaskConfig.monitor.setStatusMessage(sb.toString());
					TaskConfig.monitor.setStatusMessage("");
					resultsStringBuffer.append("\n");
				}
				 
			}else if(TaskConfig.mode==TaskConfig.HIERARICHAL_MODE){
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(TaskConfig.clustersPath));
				
				TaskConfig.monitor.setStatusMessage("");
				TaskConfig.monitor.setStatusMessage("");
				TaskConfig.monitor.setStatusMessage("Clustering Mode: cluster hierarchical");
				TaskConfig.monitor.setStatusMessage("");
				TaskConfig.monitor.setStatusMessage("Threshold range: " + TaskConfig.minThreshold + " to " + TaskConfig.maxThreshold + "");
				TaskConfig.monitor.setStatusMessage("Stepsize: " + TaskConfig.thresholdStepSize + "");
				TaskConfig.monitor.setStatusMessage("");
				TaskConfig.monitor.setStatusMessage("");
				
				
				if(TaskConfig.clusterHierarchicalComplete){
					if(!TaskConfig.reducedMatrix) upperBound = Float.POSITIVE_INFINITY;
					Hashtable<String, Hashtable<String, Boolean>> clusters = new Hashtable<String, Hashtable<String,Boolean>>();
					Vector<Vector<Integer>> mergableNodes = Splitter.splitIntoConnectedComponents(es, proteins2integers, (float) upperBound, false);
					Hashtable<Integer, Vector<Integer>> mergedNodes = new Hashtable<Integer, Vector<Integer>>();
					for (int i = 0; i < mergableNodes.size(); i++) {
						Vector<Integer> v2 = mergableNodes.get(i);
						for (int j = 0; j < v2.size(); j++) {
							mergedNodes.put(v2.get(j),v2);
						}
					}
					for (double threshold = TaskConfig.maxThreshold; threshold >= TaskConfig.minThreshold; threshold=Math.rint((threshold-TaskConfig.thresholdStepSize)*100000)/100000) {
						TaskConfig.monitor.setStatusMessage("calculating clusters for threshold " + threshold + "");
						TaskConfig.monitor.setStatusMessage("");
						clusters = calculateHierarichal2(threshold,bw,es,proteins2integers,integers2proteins,clusterReference,clusters,mergedNodes,referenceHash);
						if(clusters.size()==1) break;
					}
					bw.flush();
					bw.close();
					
				}else{
					
					if(!TaskConfig.reducedMatrix) upperBound = Float.POSITIVE_INFINITY;
					Hashtable<String, Hashtable<String, Boolean>> clusters = new Hashtable<String, Hashtable<String,Boolean>>();
					Vector<String> singletons = new Vector<String>();
					Vector<Vector<Integer>> mergableNodes = Splitter.splitIntoConnectedComponents(es, proteins2integers, (float) upperBound, false);
					Hashtable<Integer, Vector<Integer>> mergedNodes = new Hashtable<Integer, Vector<Integer>>();
					for (int i = 0; i < mergableNodes.size(); i++) {
						Vector<Integer> v2 = mergableNodes.get(i);
						for (int j = 0; j < v2.size(); j++) {
							mergedNodes.put(v2.get(j),v2);
						}
					}
					for (double threshold = TaskConfig.minThreshold; threshold <= TaskConfig.maxThreshold; threshold=Math.rint((threshold+TaskConfig.thresholdStepSize)*100000)/100000) {
						TaskConfig.monitor.setStatusMessage("calculating clusters for threshold " + threshold + "");
						TaskConfig.monitor.setStatusMessage("");
						clusters = calculateHierarichal(threshold,bw,es,proteins2integers,integers2proteins,clusterReference,clusters,singletons,mergedNodes);
						if(clusters.isEmpty()) break;
					}
					bw.flush();
					bw.close();
				}
				if(TaskConfig.gui){
//					this.gui.visualizationTab.removeAll();
//					this.gui.visualizationTab.add(this.gui.visualizationTab.buildVisualizationsPanelHierarchical(TaskConfig.clustersPath));
//					this.gui.tabsPanelOptionsVisualizations.addComponentatIndex("Visualization", null, this.gui.visualizationTab, "Visualization", TransClustGui.VISUALIZATION_TAB);
//					this.gui.visualizationTab.g2dView.fitContent();
				}
				
			}
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		s.release();
	 }


	private void rearrange(
			Hashtable<String, Hashtable<String, Boolean>> clusters, Edges es,
			HashMap<String, Integer> integers2proteins) {
		
		for (Iterator iterator = clusters.keySet().iterator(); iterator.hasNext();) {
			String key1 = (String) iterator.next();
			Hashtable<String,Boolean> cluster1 = clusters.get(key1);
			for (Iterator iterator2 = cluster1.keySet().iterator(); iterator2
					.hasNext();) {
				String id = (String) iterator2.next();
				double costs = 0;
				for (Iterator iterator3 = cluster1.keySet().iterator(); iterator3
						.hasNext();) {
					String id2 = (String) iterator3.next();
					if(id.equals(id2)) continue;
					costs+=InOut.getEdgeValue(integers2proteins.get(id), integers2proteins.get(id2), es);
				}
				
				
				for (Iterator iterator3 = clusters.keySet().iterator(); iterator3
				.hasNext();) {
					String key2 = (String) iterator3.next();
					if(key1.equals(key2)) continue;
					Hashtable<String,Boolean> cluster2 = clusters.get(key2);
					double sum = costs;
					for (Iterator iterator4 = cluster2.keySet().iterator(); iterator4
							.hasNext();) {
						String id2 = (String) iterator4.next();
						sum-=InOut.getEdgeValue(integers2proteins.get(id), integers2proteins.get(id2), es);
					}
					if(sum<0){
						dummyCosts+=sum;
//						System.out.println(dummyCosts);
						cluster1.remove(id);
						cluster2.put(id, true);
						rearrange(clusters, es, integers2proteins);
						return;
					}
				}
			}
			
		}
		
		
	}

	private void merge(Hashtable<String, Hashtable<String, Boolean>> clusters,
			Edges es, HashMap<String, Integer> integers2proteins, double threshold) {
		
		while(clusters.size()>TaskConfig.limitK){
//			System.out.println(clusters.size() + "\t" + TaskConfig.limitK);
			int i = 0;
			double bestCosts = Double.NEGATIVE_INFINITY;
			Hashtable<String,Boolean> bestcluster1 = new Hashtable<String, Boolean>();
			Hashtable<String,Boolean> bestcluster2 = new Hashtable<String, Boolean>();
			String deleteKey = "";
			for (Iterator iterator = clusters.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
//				System.out.println(key);
				Hashtable<String,Boolean> cluster1 = clusters.get(key);
				int j = 0;
				for (Iterator iterator2 = clusters.keySet().iterator(); iterator2
						.hasNext();) {
					if(i>=j){
						j++;
						continue;
					}
					String key2 = (String) iterator2.next();
//					System.out.println("\t" + key2);
					if(key.equals(key2)) continue;
					Hashtable<String,Boolean> cluster2 = clusters.get(key2);
					double costs = calculateCosts(cluster1,cluster2,es,integers2proteins,threshold);
//					System.out.println(costs);
					if(costs>bestCosts){
						bestCosts = costs;
						bestcluster1 = cluster1;
						bestcluster2 = cluster2;
						deleteKey = key2;
					}
					j++;
				}
				i++;
			}
			
//			System.out.println(deleteKey);
//			System.out.println(bestCosts);
//			System.out.println(bestcluster1.size() + "\t" + bestcluster2.size());
			dummyCosts-=bestCosts;
			bestcluster1.putAll(bestcluster2);
			clusters.remove(deleteKey);
		}
	}

	private double calculateCosts(Hashtable<String, Boolean> cluster1,
			Hashtable<String, Boolean> cluster2, Edges es,
			HashMap<String, Integer> integers2proteins, double threshold) {
		double costs = 0;
		for (Iterator iterator = cluster1.keySet().iterator(); iterator.hasNext();) {
			String id1 = (String) iterator.next();
			for (Iterator iterator2 = cluster2.keySet().iterator(); iterator2
					.hasNext();) {
				String id2 = (String) iterator2.next();
				costs+=(InOut.getEdgeValue(integers2proteins.get(id1), integers2proteins.get(id2), es)-threshold);
			}
		}
		return costs;
	}

	private Hashtable<String, Hashtable<String, Boolean>> calculateHierarichal2(
			double threshold, BufferedWriter bw, Edges es,
			HashMap<Integer, String> proteins2integers,
			HashMap<String, Integer> integers2proteins,
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> clusters,
			Hashtable<Integer, Vector<Integer>> mergedNodes, HashMap<String, String> referenceHash) throws IOException, InvalidInputFileException {
		
		
		Vector<ConnectedComponent> connectedComponents = new Vector<ConnectedComponent>();
		
		if(threshold==TaskConfig.maxThreshold){
			Vector<Vector<Integer>> v = Splitter.splitIntoConnectedComponents(es, proteins2integers, (float) threshold, false);
			
			boolean already[] = new boolean[proteins2integers.size()];
			for (Vector<Integer> vector : v) {
				int count = 0;
				Vector<Integer> representants = new Vector<Integer>();
				for (int i = 0; i < vector.size(); i++) {
					if(!already[vector.get(i)]){
						representants.add(vector.get(i));
						Vector<Integer> v2 = mergedNodes.get(vector.get(i));
						for (int j = 0; j < v2.size(); j++) {
							already[v2.get(j)] = true;
						}
						count++;
					}
				}
				ICCEdges cc2d2 = TaskConfig.ccEdgesEnum.createCCEdges(count);
				String[] ids = new String[count];
				Arrays.fill(ids, "");
				for (int i = 0; i < representants.size(); i++) {
					Vector<Integer> merged1 = mergedNodes.get(representants.get(i));
					for (int j = 0; j < merged1.size(); j++) {
						if(j==0) ids[i]+=proteins2integers.get(merged1.get(j));
						else ids[i]+= "," +proteins2integers.get(merged1.get(j));
					}
					for (int j = i+1; j < representants.size(); j++) {
						Vector<Integer> merged2 = mergedNodes.get(representants.get(j));
						float costs = 0;
						for (int k = 0; k < merged1.size(); k++) {
							for (int k2 = 0; k2 < merged2.size(); k2++) {
								costs+=(float) (InOut.getEdgeValue(merged1.get(k), merged2.get(k2), es)-threshold);
							}
						}
						cc2d2.setEdgeCost(i, j, costs);
					}
				}
				
				ConnectedComponent cc = new ConnectedComponent(cc2d2,ids,null);
				connectedComponents.add(cc);
			}	
		}else{
			Vector<Vector<Integer>> mergableNodes = new Vector<Vector<Integer>>();
			for (Iterator iterator = clusters.keySet().iterator(); iterator
					.hasNext();) {
				String key = (String) iterator.next();
				Hashtable<String,Boolean> h = clusters.get(key);
				Vector<Integer> v= new Vector<Integer>();
				for (Iterator iterator2 = h.keySet().iterator(); iterator2.hasNext();) {
					String id = (String) iterator2.next();
					v.add(integers2proteins.get(id));
				}
				mergableNodes.add(v);
			}
			
			
//			Vector<Vector<Integer>> mergableNodes = Splitter.splitIntoConnectedComponents(es, proteins2integers, (float) upperBound, false);
			mergedNodes = new Hashtable<Integer, Vector<Integer>>();
			for (int i = 0; i < mergableNodes.size(); i++) {
				Vector<Integer> v2 = mergableNodes.get(i);
				for (int j = 0; j < v2.size(); j++) {
					mergedNodes.put(v2.get(j),v2);
				}
			}
			Vector<Vector<Integer>> v = Splitter.splitIntoConnectedComponents(es, proteins2integers, (float) threshold, false);
			
			boolean already[] = new boolean[proteins2integers.size()];
			for (Vector<Integer> vector : v) {
				int count = 0;
				Vector<Integer> representants = new Vector<Integer>();
				for (int i = 0; i < vector.size(); i++) {
					if(!already[vector.get(i)]){
						representants.add(vector.get(i));
						Vector<Integer> v2 = mergedNodes.get(vector.get(i));
						for (int j = 0; j < v2.size(); j++) {
							already[v2.get(j)] = true;
						}
						count++;
					}
				}
				ICCEdges cc2d2 =TaskConfig.ccEdgesEnum.createCCEdges(count);
				String[] ids = new String[count];
				Arrays.fill(ids, "");
				for (int i = 0; i < representants.size(); i++) {
					Vector<Integer> merged1 = mergedNodes.get(representants.get(i));
					for (int j = 0; j < merged1.size(); j++) {
						if(j==0) ids[i]+=proteins2integers.get(merged1.get(j));
						else ids[i]+= "," +proteins2integers.get(merged1.get(j));
					}
					for (int j = i+1; j < representants.size(); j++) {
						Vector<Integer> merged2 = mergedNodes.get(representants.get(j));
						float costs = 0;
						for (int k = 0; k < merged1.size(); k++) {
							for (int k2 = 0; k2 < merged2.size(); k2++) {
								costs+=(float) (InOut.getEdgeValue(merged1.get(k), merged2.get(k2), es)-threshold);
							}
						}
						cc2d2.setEdgeCost(i, j, costs);
					}
				}
				
				ConnectedComponent cc = new ConnectedComponent(cc2d2,ids,null);
				connectedComponents.add(cc);
			}	
		}
				
		
		clusters = new Hashtable<String, Hashtable<String,Boolean>>();
		ClusteringManager cm = new ClusteringManager(null);
		ArrayList<Semaphore> allSemaphores = new ArrayList<Semaphore>();
		Semaphore maxThreadSemaphore = new Semaphore(TaskConfig.maxNoThreads, true);
		for(int i=0;i<connectedComponents.size();i++){
			Semaphore semaphore = new Semaphore(1);
			allSemaphores.add(semaphore);
			cm.runClusteringForOneConnectedComponent(connectedComponents.get(i), null, semaphore, maxThreadSemaphore,System.currentTimeMillis());
			int[] elements2cluster = connectedComponents.get(i).getClusters();
			for (int j = 0; j < connectedComponents.get(i).getNumberOfClusters(); j++) {
				Hashtable<String, Boolean> cluster = new Hashtable<String, Boolean>();
				for (int k = 0; k < elements2cluster.length; k++) {
					if(elements2cluster[k]==j){
						String ids[] = connectedComponents.get(i).getObjectID(k).split(",");
						for (int l = 0; l < ids.length; l++) {
							cluster.put(ids[l], true);	
						}
						
					}
				}
				clusters.put(new Random().nextDouble()+"", cluster);
			}
		}

		/* wait for all clustering tasks to finish */
		for (Semaphore s : allSemaphores) {
			try {
				s.acquire();
			} catch (InterruptedException e) {
				TaskConfig.monitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
				// e.printStackTrace();
			}
		}
		
		Hashtable<String,Hashtable<String,Boolean>> clustersCopy = (Hashtable<String, Hashtable<String, Boolean>>) clusters.clone();
		if(TaskConfig.goldstandardPath!=null){
			ArrayList<String> dummyRemove2 = new ArrayList<String>();
			
			for (Iterator iterator2 = clusters.keySet().iterator(); iterator2
			.hasNext();) {
				String key = (String) iterator2.next();
				Hashtable<String, Boolean> h = clusters.get(key);
				ArrayList<String> dummyRemove = new ArrayList<String>();
				for (Iterator iterator = h.keySet().iterator(); iterator
						.hasNext();) {
					String string = (String) iterator.next();
					if(!referenceHash.containsKey(string)){
						dummyRemove.add(string);
					}
				}
				for (String string : dummyRemove) {
					h.remove(string);
				}
				if(h.isEmpty()) dummyRemove2.add(key);
			}
			for (String string : dummyRemove2) {
				clustersCopy.remove(string);
			} 

		}
				
		
		bw.write(threshold + "\t");
		if(clusterReference!=null){
			double fmeasure = Fmeassure.fMeassure(clusterReference,clustersCopy);
			bw.write(fmeasure+"\t");
			TaskConfig.monitor.setStatusMessage("fmeasure: " + fmeasure);
		}else{
			bw.write("-\t");
		}
		
		int[] distribution = new int[1000000];
		int max = 1;
		boolean first= true;
		Vector<String> keysToRemove = new Vector<String>();
		for (Iterator<String> iterator = clusters.keySet().iterator(); iterator
				.hasNext();) {
			String key =  iterator.next();
			Hashtable<String,Boolean> h = clusters.get(key);
			if(!first) bw.write(";");
			first = true;
			for (Iterator<String> iterator2 = h.keySet().iterator(); iterator2.hasNext();) {
				String id = iterator2.next();
				if(first){
					first = false;
					bw.write(id);
				}else bw.write("," + id);
			}
			distribution[h.size()]++;
			if(h.size()>max) max= h.size();				
		}
		StringBuffer sb = new StringBuffer("cluster distribution: ");
		
		for (int i = max; i >=0 ; i--) {
			if(distribution[i]>0) sb.append(i + ":" + distribution[i] + ", ");
		}
		
		TaskConfig.monitor.setStatusMessage(sb.toString());
		TaskConfig.monitor.setStatusMessage("");
		bw.newLine();
		mergedNodes.clear();
		for (Iterator iterator = clusters.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Hashtable<String,Boolean> currentCluster = clusters.get(key);
			Vector<Integer> currentClusterVector = new Vector<Integer>();
			for (Iterator iterator2 = currentCluster.keySet().iterator(); iterator2
					.hasNext();) {
				String id = (String) iterator2.next();
				currentClusterVector.add(integers2proteins.get(id));
			}
			for (Integer integer : currentClusterVector) {
				mergedNodes.put(integer, currentClusterVector);
			}
		}
		
		
		
		
		return clusters;
		
	}

	public static Hashtable<String, Hashtable<String, Boolean>> readGoldStandardFile() throws IOException {
		
		Hashtable<String, Hashtable<String, Boolean>> clusters = new Hashtable<String, Hashtable<String,Boolean>>();
		
		BufferedReader br = new BufferedReader(new FileReader(TaskConfig.goldstandardPath));
		
		String line;
		String currentCluster=null;
		while((line=br.readLine())!=null){
			if(line.trim().equals("")) continue;
			String tabs[] = line.split("\t");
			if(currentCluster==null){
				Hashtable<String, Boolean> h = new Hashtable<String, Boolean>();
				h.put(tabs[0], true);
				currentCluster = tabs[1];
				clusters.put(tabs[1], h);
//			}else if(tabs[1].equals(currentCluster)){
			}else if(clusters.containsKey(tabs[1])){
				clusters.get(tabs[1]).put(tabs[0], true);
			}else{
				Hashtable<String, Boolean> h = new Hashtable<String, Boolean>();
				h.put(tabs[0], true);
				currentCluster = tabs[1];
				clusters.put(tabs[1], h);
			}
		}
		return clusters;
	}
	
	private Hashtable<String, Hashtable<String, Boolean>> readGoldStandardFile(String file) throws IOException {
		
		Hashtable<String, Hashtable<String, Boolean>> clusters = new Hashtable<String, Hashtable<String,Boolean>>();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		String currentCluster=null;
		while((line=br.readLine())!=null){
			if(line.trim().equals("")) continue;
			String tabs[] = line.split("\t");
			if(currentCluster==null){
				Hashtable<String, Boolean> h = new Hashtable<String, Boolean>();
				h.put(tabs[0], true);
				currentCluster = tabs[1];
				clusters.put(tabs[1], h);
//			}else if(tabs[1].equals(currentCluster)){
			}else if(clusters.containsKey(tabs[1])){
				clusters.get(tabs[1]).put(tabs[0], true);
			}else{
				Hashtable<String, Boolean> h = new Hashtable<String, Boolean>();
				h.put(tabs[0], true);
				currentCluster = tabs[1];
				clusters.put(tabs[1], h);
			}
		}
		return clusters;
	}


	private Hashtable<String, Hashtable<String,Boolean>> calculateHierarichal(double threshold, BufferedWriter bw, Edges es, HashMap<Integer, String> proteins2integers, HashMap<String, Integer> integers2proteins, Hashtable<String, Hashtable<String, Boolean>> clusterReference, Hashtable<String, Hashtable<String, Boolean>> clusters, Vector<String> singletons, Hashtable<Integer, Vector<Integer>> mergedNodes) throws IOException, ArgsParseException, InvalidInputFileException, InvalidTypeException {
		
		Vector<ConnectedComponent> connectedComponents = new Vector<ConnectedComponent>();
		if(threshold==TaskConfig.minThreshold){
			Vector<Vector<Integer>> v = Splitter.splitIntoConnectedComponents(es, proteins2integers, (float) threshold, false);
			
			boolean already[] = new boolean[proteins2integers.size()];
			for (Vector<Integer> vector : v) {
				int count = 0;
				Vector<Integer> representants = new Vector<Integer>();
				for (int i = 0; i < vector.size(); i++) {
					if(!already[vector.get(i)]){
						representants.add(vector.get(i));
						Vector<Integer> v2 = mergedNodes.get(vector.get(i));
						for (int j = 0; j < v2.size(); j++) {
							already[v2.get(j)] = true;
						}
						count++;
					}
				}
				ICCEdges cc2d2 = TaskConfig.ccEdgesEnum.createCCEdges(count);
				String[] ids = new String[count];
				Arrays.fill(ids, "");
				for (int i = 0; i < representants.size(); i++) {
					Vector<Integer> merged1 = mergedNodes.get(representants.get(i));
					for (int j = 0; j < merged1.size(); j++) {
						if(j==0) ids[i]+=proteins2integers.get(merged1.get(j));
						else ids[i]+= "," +proteins2integers.get(merged1.get(j));
					}
					for (int j = i+1; j < representants.size(); j++) {
						Vector<Integer> merged2 = mergedNodes.get(representants.get(j));
						float costs = 0;
						for (int k = 0; k < merged1.size(); k++) {
							for (int k2 = 0; k2 < merged2.size(); k2++) {
								costs+=(float) (InOut.getEdgeValue(merged1.get(k), merged2.get(k2), es)-threshold);
							}
						}
						cc2d2.setEdgeCost(i, j, costs);
					}
				}
				
				ConnectedComponent cc = new ConnectedComponent(cc2d2,ids,null);
				connectedComponents.add(cc);
			}
		}else{
			boolean already[] = new boolean[proteins2integers.size()];
			for (Iterator<String> iterator = clusters.keySet().iterator(); iterator.hasNext();) {
				String key =  iterator.next();
				Hashtable<String, Boolean> cluster = clusters.get(key);
				Vector<Integer> vector = new Vector<Integer>();
				
				for (Iterator<String> iter = cluster.keySet().iterator(); iter.hasNext();) {
					String element = iter.next();
					vector.add(integers2proteins.get(element));
				}
				
				
				int count = 0;
				Vector<Integer> representants = new Vector<Integer>();
				for (int i = 0; i < vector.size(); i++) {
					if(!already[vector.get(i)]){
						representants.add(vector.get(i));
						Vector<Integer> v2 = mergedNodes.get(vector.get(i));
						for (int j = 0; j < v2.size(); j++) {
							already[v2.get(j)] = true;
						}
						count++;
					}
				}
				ICCEdges cc2d2 = TaskConfig.ccEdgesEnum.createCCEdges(count);
				String[] ids = new String[count];
				Arrays.fill(ids, "");
				for (int i = 0; i < representants.size(); i++) {
					Vector<Integer> merged1 = mergedNodes.get(representants.get(i));
					for (int j = 0; j < merged1.size(); j++) {
						if(j==0) ids[i]+=proteins2integers.get(merged1.get(j));
						else ids[i]+= "," +proteins2integers.get(merged1.get(j));
					}
					for (int j = i+1; j < representants.size(); j++) {
						Vector<Integer> merged2 = mergedNodes.get(representants.get(j));
						float costs = 0;
						for (int k = 0; k < merged1.size(); k++) {
							for (int k2 = 0; k2 < merged2.size(); k2++) {
								costs+=(float) (InOut.getEdgeValue(merged1.get(k), merged2.get(k2), es)-threshold);
							}
						}
						cc2d2.setEdgeCost(i, j, costs);
					}
				}
				
				ConnectedComponent cc = new ConnectedComponent(cc2d2,ids,null);
				connectedComponents.add(cc);
				
				
//				String key =  iterator.next();
//				Hashtable<String, Boolean> cluster = clusters.get(key);
//				CC2DArray cc2d = new CC2DArray(cluster.size());
//				String[] ids = new String[cluster.size()];
//				int iterator_i = 0;
//				for (Iterator<String> iterator2 = cluster.keySet().iterator(); iterator2.hasNext();) {
//					String key2 = iterator2.next();
//					ids[iterator_i] = key2;
//					iterator_i++;
//				}
//				for (int i = 0; i < ids.length; i++) {
//					for (int j = i+1; j < ids.length; j++) {
//						cc2d.setEdgeCost(i, j, (float) (InOut.getEdgeValue(integers2proteins.get(ids[i]), integers2proteins.get(ids[j]), es)-threshold));
//					}
//				}
//				ConnectedComponent cc = new ConnectedComponent(cc2d,ids,null);
//				connectedComponents.add(cc);
			}
		}
		
		clusters = new Hashtable<String, Hashtable<String,Boolean>>();
		ClusteringManager cm = new ClusteringManager(null);
		ArrayList<Semaphore> allSemaphores = new ArrayList<Semaphore>();
		Semaphore maxThreadSemaphore = new Semaphore(TaskConfig.maxNoThreads, true);
		for(int i=0;i<connectedComponents.size();i++){
			Semaphore semaphore = new Semaphore(1);
			allSemaphores.add(semaphore);
			cm.runClusteringForOneConnectedComponent(connectedComponents.get(i), null, semaphore, maxThreadSemaphore,System.currentTimeMillis());
			int[] elements2cluster = connectedComponents.get(i).getClusters();
			for (int j = 0; j < connectedComponents.get(i).getNumberOfClusters(); j++) {
				Hashtable<String, Boolean> cluster = new Hashtable<String, Boolean>();
				for (int k = 0; k < elements2cluster.length; k++) {
					if(elements2cluster[k]==j){
						String ids[] = connectedComponents.get(i).getObjectID(k).split(",");
						for (int l = 0; l < ids.length; l++) {
							cluster.put(ids[l], true);	
						}
						
					}
				}
				clusters.put(new Random().nextDouble()+"", cluster);
			}
		}

		/* wait for all clustering tasks to finish */
		for (Semaphore s : allSemaphores) {
			try {
				s.acquire();
			} catch (InterruptedException e) {
				TaskConfig.monitor.showMessage(TaskMonitor.Level.ERROR, e.getMessage());
				// e.printStackTrace();
			}
		}
		
		bw.write(threshold + "\t");
		if(clusterReference!=null){
			double fmeasure = Fmeassure.fMeassure(clusterReference,clusters);
			bw.write(fmeasure+"\t");
			TaskConfig.monitor.setStatusMessage("fmeasure: " + fmeasure);
		}else{
			bw.write("-\t");
		}
		
		int[] distribution = new int[1000000];
		int max = 1;
		boolean first= true;
		Vector<String> keysToRemove = new Vector<String>();
		for (Iterator<String> iterator = clusters.keySet().iterator(); iterator
				.hasNext();) {
			String key =  iterator.next();
			Hashtable<String,Boolean> h = clusters.get(key);
			if(!first) bw.write(";");
			if(h.size()==1){
				singletons.add(h.keySet().iterator().next());
				keysToRemove.add(key);
			}else{
				first = true;
				for (Iterator<String> iterator2 = h.keySet().iterator(); iterator2.hasNext();) {
					String id = iterator2.next();
					if(first){
						first = false;
						bw.write(id);
					}else bw.write("," + id);
				}
				distribution[h.size()]++;
				if(h.size()>max) max= h.size();				
			}
		}
		for (String key : keysToRemove) {
			clusters.remove(key);
		}
		for (String id : singletons) {
			bw.write(";" + id);
		}
		distribution[1] = singletons.size();
		
		StringBuffer sb = new StringBuffer("cluster distribution: ");
		
		for (int i = max; i >=0 ; i--) {
			if(distribution[i]>0) sb.append(i + ":" + distribution[i] + ", ");
		}
		
		TaskConfig.monitor.setStatusMessage(sb.toString());
		TaskConfig.monitor.setStatusMessage("");
		bw.newLine();
		return clusters;
	}
	
private void calculateOverlapping(Vector<Vector<Integer>> clustersVector, Edges es, double threshold,int[] overlaps) {
		
		for (Vector<Integer> cluster : clustersVector) {
			for (Integer id : cluster) {
				if(overlaps[id]>2) continue;
				for (Vector<Integer> cluster2 : clustersVector) {
					if(cluster2.size()<2) continue;
//					else if(cluster2.size()<=cluster.size()) continue;
					else if(cluster==cluster2) continue;
					else if(cluster2.contains(id)) continue;
					else if(cluster.size()==1||cluster2.size()==1) continue;
					double costs = 0;
					for (Integer id2 : cluster2) {
						if(id.equals(id2))continue;
						costs-= (InOut.getEdgeValue(id, id2, es)-threshold);
					}
					if(costs<=0) {
//						System.out.println(id + "\t" + costs);
						cluster2.add(id);
						overlaps[id]++;
						calculateOverlapping(clustersVector, es, threshold,overlaps);
						return;
					}
				}		
			}
		}
	}

}


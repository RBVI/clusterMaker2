package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.clusteranalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.Edges;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.InOut;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

public class Fmeassure {

	public static double fMeassure(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {

		int proteins = countProteins(clusterReference);

		double fmeasure = 0;

		int count = 0;
		Enumeration<String> e = clusterReference.keys();
		while (e.hasMoreElements()) {
			String clusterID = e.nextElement();
			Hashtable<String, Boolean> h = clusterReference.get(clusterID);
			int proteinsInReference = h.size();
			double maxValue = findMax(proteinsInReference, cluster, h);
			fmeasure += (maxValue * proteinsInReference);
			count++;
		}
		fmeasure /= proteins;

		return fmeasure;
	}

	public static double fMeassure2(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {

		double fmeasure = 0;

		int count = 0;
		Enumeration<String> e = clusterReference.keys();
		while (e.hasMoreElements()) {
			String clusterID = e.nextElement();
			Hashtable<String, Boolean> h = clusterReference.get(clusterID);
			int proteinsInReference = h.size();
			double maxValue = findMax2(proteinsInReference, cluster, h);
			fmeasure += (maxValue);
			count++;
		}
		fmeasure /= clusterReference.size();

		return fmeasure;
	}

	public static double fMeassure() throws IOException {

		Hashtable<String, Hashtable<String, Boolean>> clusterReference = readCluster(TaskConfig.goldstandardPath);
		int proteins = countProteins(clusterReference);
		String fileName = TaskConfig.clustersPath;

		Hashtable<String, Hashtable<String, Boolean>> cluster = readCluster(fileName);

		double fmeasure = 0;

		int count = 0;
		Enumeration<String> e = clusterReference.keys();
		while (e.hasMoreElements()) {
			String clusterID = e.nextElement();
			Hashtable<String, Boolean> h = clusterReference.get(clusterID);
			int proteinsInReference = h.size();
			double maxValue = findMax(proteinsInReference, cluster, h);
			fmeasure += (maxValue * proteinsInReference);
			count++;
		}
		fmeasure /= proteins;

		return fmeasure;

	}

	public static double fMeassure(String goldStandardFile, String clustersFile) {

		double value = 0;

		return value;

	}
	
	public static double ClusterWiseSeperation(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {
		
		double seperation=0;
		
		double tij[][] = new double[clusterReference.size()][cluster.size()];
		Enumeration<String> e = clusterReference.keys();
		int i = 0;
		while (e.hasMoreElements()) {
			String clusterID = e.nextElement();
			Hashtable<String, Boolean> h = clusterReference.get(clusterID);
			Enumeration<String> e2 = cluster.keys();
			int j = 0;
			while (e2.hasMoreElements()) {
				int common = 0;
				String id = e2.nextElement();
				Hashtable<String, Boolean> h2 = cluster.get(id);
				if (h.size() < h2.size()) {
					common = calculateCommonProteins(h, h2);
				} else {
					common = calculateCommonProteins(h2, h);
				}
				tij[i][j] = (double) common;
				j++;
			}
			i++;
		}

		
		double fcol[][] = new double[clusterReference.size()][cluster.size()];
		for (int j = 0; j < fcol.length; j++) {
			for (int j2 = 0; j2 < fcol[j].length; j2++) {
				fcol[j][j2] = tij[j][j2];
				double tj = 0;
				for (int k = 0; k < tij.length; k++) {
					tj += tij[k][j2];
				}
				if(fcol[j][j2]==0 &&tj==0){
					
				}else{
					fcol[j][j2] /= tj;
				}
			}
		}
		
		double frow[][] = new double[clusterReference.size()][cluster.size()];
		for (int j = 0; j < frow[0].length; j++) {
			for (int j2 = 0; j2 < frow.length; j2++) {
				frow[j2][j] = tij[j2][j];
				double tj = 0;
				for (int k = 0; k < tij[0].length; k++) {
					tj += tij[j2][k];
				}
//				if(tj == 0) tj = 1;
				if(frow[j2][j]==0 &&tj==0){
					
				}else{
					frow[j2][j] /= tj;
				}
			}
		}
		
		double sep[][] = new double[clusterReference.size()][cluster.size()];
		
		
		for (int j = 0; j < sep.length; j++) {
			for (int j2 = 0; j2 < sep[j].length; j2++) {
				sep[j][j2] = fcol[j][j2]*frow[j][j2];
				seperation+=sep[j][j2];
			}
		}
		
		
		
		seperation/=cluster.size();
		
		return seperation;
		
	}
	
	public static double ComplexWiseSeperation(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {
		
		double seperation=0;
		
		double tij[][] = new double[clusterReference.size()][cluster.size()];
		Enumeration<String> e = clusterReference.keys();
		int i = 0;
		while (e.hasMoreElements()) {
			String clusterID = e.nextElement();
			Hashtable<String, Boolean> h = clusterReference.get(clusterID);
			Enumeration<String> e2 = cluster.keys();
			int j = 0;
			while (e2.hasMoreElements()) {
				int common = 0;
				String id = e2.nextElement();
				Hashtable<String, Boolean> h2 = cluster.get(id);
				if (h.size() < h2.size()) {
					common = calculateCommonProteins(h, h2);
				} else {
					common = calculateCommonProteins(h2, h);
				}
				tij[i][j] = (double) common;
				j++;
			}
			i++;
		}

		double fcol[][] = new double[clusterReference.size()][cluster.size()];
		for (int j = 0; j < fcol.length; j++) {
			for (int j2 = 0; j2 < fcol[j].length; j2++) {
				fcol[j][j2] = tij[j][j2];
				double tj = 0;
				for (int k = 0; k < tij.length; k++) {
					tj += tij[k][j2];
				}
				if(fcol[j][j2]==0 &&tj==0){
					
				}else{
					fcol[j][j2] /= tj;
				}
			}
		}
		
		double frow[][] = new double[clusterReference.size()][cluster.size()];
		for (int j = 0; j < frow[0].length; j++) {
			for (int j2 = 0; j2 < frow.length; j2++) {
				frow[j2][j] = tij[j2][j];
				double tj = 0;
				for (int k = 0; k < tij[0].length; k++) {
					tj += tij[j2][k];
				}
//				if(tj == 0) tj = 1;
				if(frow[j2][j]==0 &&tj==0){
					
				}else{
					frow[j2][j] /= tj;
				}
			}
		}
		
		double sep[][] = new double[clusterReference.size()][cluster.size()];
		for (int j = 0; j < sep.length; j++) {
			for (int j2 = 0; j2 < sep[j].length; j2++) {
				sep[j][j2] = fcol[j][j2]*frow[j][j2];
				seperation+=sep[j][j2];
			}
		}
		
		
		
		seperation/=clusterReference.size();
		
		return seperation;
	}

	/**
	 * @param clusterReference
	 * @param cluster
	 * @return
	 */
	public static double PPV(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {

		double PPV = 0;

		double tij[][] = new double[clusterReference.size()][cluster.size()];
		Enumeration<String> e = clusterReference.keys();
		int i = 0;
		while (e.hasMoreElements()) {
			String clusterID = e.nextElement();
			Hashtable<String, Boolean> h = clusterReference.get(clusterID);
			Enumeration<String> e2 = cluster.keys();
			int j = 0;
			while (e2.hasMoreElements()) {
				int common = 0;
				String id = e2.nextElement();
				Hashtable<String, Boolean> h2 = cluster.get(id);
				if (h.size() < h2.size()) {
					common = calculateCommonProteins(h, h2);
				} else {
					common = calculateCommonProteins(h2, h);
				}
				tij[i][j] = (double) common;
				j++;
			}
			i++;
		}

		double ppv[][] = new double[clusterReference.size()][cluster.size()];
		double tjs[] = new double[cluster.size()];

		for (int j = 0; j < ppv.length; j++) {
			for (int j2 = 0; j2 < ppv[j].length; j2++) {
				ppv[j][j2] = tij[j][j2];
				double tj = 0;
				for (int k = 0; k < tij.length; k++) {
					tj += tij[k][j2];
				}
				ppv[j][j2] /= tj;
			}
		}

		for (int j = 0; j < tij[0].length; j++) {
			for (int j2 = 0; j2 < tij.length; j2++) {
				tjs[j] += tij[j2][j];
			}
		}

		double maxppvs[] = new double[cluster.size()];

		for (int j = 0; j < tij[0].length; j++) {
			double max = 0;
			for (int j2 = 0; j2 < tij.length; j2++) {
				if (ppv[j2][j] > max) {
					max = ppv[j2][j];
				}
			}
			maxppvs[j] = max;
		}

		double sum = 0;
		for (int j = 0; j < tjs.length; j++) {
			sum += tjs[j];
		}

		for (int j = 0; j < maxppvs.length; j++) {
			PPV += tjs[j] * maxppvs[j];
		}
		PPV /= sum;
		return PPV;
	}

	public static double Sensitivity2(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {

		double PPV = 0;

		double tij[][] = new double[clusterReference.size()][cluster.size()];
		Enumeration<String> e = clusterReference.keys();
		int i = 0;
		while (e.hasMoreElements()) {
			String clusterID = e.nextElement();
			Hashtable<String, Boolean> h = clusterReference.get(clusterID);
			Enumeration<String> e2 = cluster.keys();
			int j = 0;
			while (e2.hasMoreElements()) {
				int common = 0;
				String id = e2.nextElement();
				Hashtable<String, Boolean> h2 = cluster.get(id);
				if (h.size() < h2.size()) {
					common = calculateCommonProteins(h, h2);
				} else {
					common = calculateCommonProteins(h2, h);
				}
				tij[i][j] = (double) common;
				j++;
			}
			i++;
		}

		double ppv[][] = new double[clusterReference.size()][cluster.size()];
		double tjs[] = new double[clusterReference.size()];

		for (int j2 = 0; j2 < ppv[0].length; j2++) {
			for (int j = 0; j < ppv.length; j++) {
				ppv[j][j2] = tij[j][j2];
				double tj = 0;
				for (int k = 0; k < tij.length; k++) {
					tj += tij[j][k];
				}
				ppv[j][j2] /= tj;
			}
		}

		for (int j2 = 0; j2 < tij.length; j2++) {
			for (int j = 0; j < tij[0].length; j++) {
				tjs[j2] += tij[j2][j];
			}
		}

		double maxppvs[] = new double[clusterReference.size()];
		for (int j2 = 0; j2 < tij.length; j2++) {
			double max = 0;
			for (int j = 0; j < tij[0].length; j++) {
				if (ppv[j2][j] > max) {
					max = ppv[j2][j];
				}
			}
			maxppvs[j2] = max;
		}

		double sum = 0;
		for (int j = 0; j < tjs.length; j++) {
			sum += tjs[j];
		}

		for (int j = 0; j < maxppvs.length; j++) {
			PPV += tjs[j] * maxppvs[j];
		}
		PPV /= sum;
		return PPV;
	}
	
	public static double BCubedPrecision(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {
		
	
		Hashtable<String,ArrayList<String>> reference = new Hashtable<String, ArrayList<String>>();
		Hashtable<String,ArrayList<String>> clustering = new Hashtable<String, ArrayList<String>>();
		
		for (Iterator iterator = clusterReference.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Hashtable<String,Boolean> h = clusterReference.get(key);
			for (Iterator iterator2 = h.keySet().iterator(); iterator2
					.hasNext();) {
				String id = (String) iterator2.next();
				if(reference.containsKey(id)){
					reference.get(id).add(key);
				}else{
					ArrayList<String> al = new ArrayList<String>();
					al.add(key);
					reference.put(id, al);
				}
			}
		}
		
		for (Iterator iterator = cluster.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Hashtable<String,Boolean> h = cluster.get(key);
			for (Iterator iterator2 = h.keySet().iterator(); iterator2
					.hasNext();) {
				String id = (String) iterator2.next();
				if(clustering.containsKey(id)){
					clustering.get(id).add(key);
				}else{
					ArrayList<String> al = new ArrayList<String>();
					al.add(key);
					clustering.put(id, al);
				}
			}
		}
		
//		System.out.println(clustering.size() + "\t " + reference.size());
		double precision = 0;
		double recall = 0;
		for (Iterator iterator = reference.keySet().iterator(); iterator.hasNext();) {
			String id1 = (String) iterator.next();
			ArrayList<String> referenceList1 = reference.get(id1);
			ArrayList<String> clusteringList1 = clustering.get(id1);
			if(clusteringList1==null){
				clusteringList1 = new ArrayList<String>();
				clusteringList1.add(id1);
			}
			double elementWisePrecision = 0;
			double elementWiseRecall = 0;
			int countPrecision = 0;
			int countRecall = 0;
			for (Iterator iterator2 = reference.keySet().iterator(); iterator2
					.hasNext();) {
				String id2 = (String) iterator2.next();
				ArrayList<String> referenceList2 = reference.get(id2);
				ArrayList<String> clusteringList2 = clustering.get(id2);
				if(clusteringList2==null){
					clusteringList2 = new ArrayList<String>();
					clusteringList2.add(id2);
				}
				
				double commonReference = calculateCommonClusters(referenceList1,referenceList2);
				double commonClustering = calculateCommonClusters(clusteringList1, clusteringList2);
				if(commonReference!=0){
					elementWisePrecision += (Math.min(commonReference, commonClustering)/commonReference);
					countPrecision++;
				}
				if(commonClustering!=0){
					elementWiseRecall += (Math.min(commonReference, commonClustering)/commonClustering);
					countRecall++;
				}
			}
			elementWisePrecision/=countPrecision;
			elementWiseRecall/=countRecall;
			recall+=elementWiseRecall;
			precision+=elementWisePrecision;
		}
		precision/=reference.size();
		recall/=reference.size();
		
		double fmeasure = 2 * (recall * precision) / (recall + precision);
//		System.out.println("recall " + recall + " precision " + precision + " fmeasure " + fmeasure);
		
		return precision;
	}
	
	private static double calculateCommonClusters(
			ArrayList<String> referenceList1, ArrayList<String> referenceList2) {
		double common = 0;
		for (String cluster1 : referenceList2) {
			for (String cluster2 : referenceList1) {
				if(cluster1.equals(cluster2)) common++;
			}
		}
		
		
		return common;
	}

	public static double BCubedRecall(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {
		double recall = 0;
	
		return recall;
	}
	
	
	public static double Sensitivity(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster) {

		double sensitivity = 0;
		double count = 0;

		Enumeration<String> e = clusterReference.keys();
		while (e.hasMoreElements()) {
			double sensitivityForCluster = 0;
			String clusterID = e.nextElement();
			Hashtable<String, Boolean> h = clusterReference.get(clusterID);
			int proteinsInReference = h.size();
			Enumeration<String> e2 = cluster.keys();
			while (e2.hasMoreElements()) {
				double dummy = 0;
				int common = 0;
				String id = e2.nextElement();
				Hashtable<String, Boolean> h2 = cluster.get(id);
				if (h.size() < h2.size()) {
					common = calculateCommonProteins(h, h2);
				} else {
					common = calculateCommonProteins(h2, h);
				}
				dummy = (double) common / (double) h.size();
				if (dummy > sensitivityForCluster) {
					sensitivityForCluster = dummy;
				}
			}
			sensitivity += sensitivityForCluster * h.size();
			count += h.size();
		}
		sensitivity /= count;

		return sensitivity;
	}

	private static double findMax(int proteinsInReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster,
			Hashtable<String, Boolean> h) {
		double max = 0;
		double maxCommon = 0;
		double maxh2size = 0;
		Enumeration<String> e = cluster.keys();
		while (e.hasMoreElements()) {
			double dummy = 0;
			int common = 0;
			String id = e.nextElement();
			Hashtable<String, Boolean> h2 = cluster.get(id);
			if (h.size() < h2.size()) {
				common = calculateCommonProteins(h, h2);
			} else {
				common = calculateCommonProteins(h2, h);
			}
			double dummy2 = 2 * common;
			double dummy3 = h.size() + h2.size();
			dummy = (2 * common) / (h.size() + h2.size());
			dummy = dummy2 / dummy3;
			if (dummy > max) {
				max = dummy;
				maxCommon = common;
				maxh2size = h2.size();
			}
		}
		if (maxCommon == 0 && h.size() == 1) {
			return 1;
		}
//		System.out.println(h.size() + "\t" + maxCommon + "\t" + maxh2size);
		
		
		return max;
	}

	private static double findMax2(int proteinsInReference,
			Hashtable<String, Hashtable<String, Boolean>> cluster,
			Hashtable<String, Boolean> h) {
		double max = 0;
		double maxCommon = 0;
		double maxSizeCluster = 0;
		Enumeration<String> e = cluster.keys();
		// System.out.print(h.size() +":\t");
		while (e.hasMoreElements()) {
			double dummy = 0;
			int common = 0;
			String id = e.nextElement();
			Hashtable<String, Boolean> h2 = cluster.get(id);
			if (h.size() < h2.size()) {
				common = calculateCommonProteins(h, h2);
			} else {
				common = calculateCommonProteins(h2, h);
			}
			double tp = common;
			double fp = h2.size() - common;
			double fn = h.size() - common;
			double precision = (tp / (tp + fp));
			double recall = (tp / (tp + fn));
			double fmeasure = 2 * (recall * precision) / (recall + precision);

			// if(common!=0){
			// System.out.print(h2.size()+",");
			// }

			if (fmeasure > max) {
				max = fmeasure;
				maxCommon = common;
				maxSizeCluster = h2.size();
			}
		}
		if (maxCommon == 0 && h.size() == 1) {
			return 1;
		}
//		System.out.println();
//		System.out.println(h.size() + "\t" + maxCommon + "\t" + maxSizeCluster
//				+ "\t" + max);
		return max;
	}

	private static int countProteins(
			Hashtable<String, Hashtable<String, Boolean>> c) {
		int proteins = 0;
		Enumeration<String> e = c.keys();
		while (e.hasMoreElements()) {
			String id = e.nextElement();
			Hashtable<String, Boolean> h = c.get(id);
			proteins += h.size();
		}
		return proteins;
	}

	private static int calculateCommonProteins(Hashtable<String, Boolean> c1,
			Hashtable<String, Boolean> c2) {
		int common = 0;
		Enumeration<String> e = c1.keys();
		while (e.hasMoreElements()) {
			String id = e.nextElement();
			if (c2.containsKey(id)) {
				common++;
			}
		}
		return common;
	}

	private static Hashtable<String, Hashtable<String, Boolean>> readCluster(
			String fileName) throws IOException {
		Hashtable<String, Hashtable<String, Boolean>> cluster = new Hashtable<String, Hashtable<String, Boolean>>();
		BufferedReader br = myBufferedReader(fileName);
		String line;
		while ((line = br.readLine()) != null) {
			String tokens[] = line.split("\t");
			Hashtable<String, Boolean> h = new Hashtable<String, Boolean>();
			if (cluster.containsKey(tokens[1])) {
				h = cluster.get(tokens[1]);
			}
			h.put(tokens[0], true);
			cluster.put(tokens[1], h);
		}
		return cluster;
	}

	private static BufferedReader myBufferedReader(String file)
			throws IOException {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		return br;
	}

	public static double fMeassure(HashMap<String, String> referenceHash,
			HashMap<String, String> clusterHash) {

		double fmeasure = 0;

		double tp = 0;
		double fp = 0;
		double fn = 0;
		double tn = 0;

		for (Iterator iterator = clusterHash.keySet().iterator(); iterator
				.hasNext();) {
			String id1 = (String) iterator.next();
			for (Iterator iterator2 = clusterHash.keySet().iterator(); iterator2
					.hasNext();) {
				String id2 = (String) iterator2.next();
				boolean sameCluster = clusterHash.get(id1).equals(
						clusterHash.get(id2));
				boolean sameReference = referenceHash.get(id1).equals(
						referenceHash.get(id2));

				if (sameCluster && sameReference)
					tp++;
				else if (sameCluster && !sameReference)
					fp++;
				else if (!sameCluster && sameReference)
					fn++;
				else
					tn++;
			}
		}
		double precision = (tp / (tp + fp));
		double recall = (tp / (tp + fn));
		fmeasure = 2 * (recall * precision) / (recall + precision);

		return fmeasure;
	}

	public static double vidistance(
			Hashtable<String, Hashtable<String, Boolean>> clusterReference,
			Hashtable<String, Hashtable<String, Boolean>> clusters) {
		double numberOfObjects = 0;
		for (Iterator iterator = clusterReference.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Hashtable<String,Boolean> cluster1 = clusterReference.get(key);
			numberOfObjects+=cluster1.size();
		}
		
		double hxy = 0;
		double hyx = 0;
		for (Iterator iterator = clusterReference.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Hashtable<String,Boolean> cluster1 = clusterReference.get(key);
			
			for (Iterator iterator2 = clusters.keySet().iterator(); iterator2
					.hasNext();) {
				String key2 = (String) iterator2.next();
				Hashtable<String,Boolean> cluster2 = clusters.get(key2);
				
				double common = (double) calculateCommonProteins(cluster1, cluster2);
				if(common==0) continue;
//				System.out.println(common);
				double pxy = (common/numberOfObjects);
				double px = ((double) cluster2.size()/numberOfObjects);
				double py = ((double) cluster1.size()/numberOfObjects);
//				System.out.println(pxy + "\t" + px + "\t" + py);
//				System.out.println(Math.log(pxy/py));
				hxy += pxy*Math.log(pxy/py);
				hyx += pxy*Math.log(pxy/px);
//				System.out.println(hxy+"\t" + hyx);
			}
		}
		double vidistance = 0;
		vidistance-=hxy;
		vidistance-=hyx;
	
		
		return vidistance;
	}

	public static double silhouette(
			Hashtable<String, Hashtable<String, Boolean>> clusters, Edges es,
			HashMap<Integer, String> proteins2integers,HashMap<String, Integer> integers2proteins) {

		double silhouette = 0;
		
		HashMap<String, String> protein2cluster = new HashMap<String,String>();
		for (String clustername : clusters.keySet()) {
			Hashtable<String,Boolean> cluster = clusters.get(clustername);
			for (String object : cluster.keySet()) {
				protein2cluster.put(object, clustername);
			}
		}
		int count = 0;
		for (Integer proteinNr : proteins2integers.keySet()) {
			String protein = proteins2integers.get(proteinNr);
			String clusterName = protein2cluster.get(protein);
			Hashtable<String, Boolean> cluster = clusters.get(clusterName);
			double a = 0;
			for (String element : cluster.keySet()) {
				if(element.equals(protein)) continue;
				a+=InOut.getEdgeValue(proteinNr	, integers2proteins.get(element), es);
			}
			a/=(cluster.size()-1);
			
			
			double b = 0;

			
			for (String clusterName2 : clusters.keySet()) {
				if(clusterName.equals(clusterName2)) continue;
				double localb = 0;
				Hashtable<String, Boolean> cluster2 = clusters.get(clusterName2);
				for (String element : cluster2.keySet()) {
					localb+=InOut.getEdgeValue(proteinNr, integers2proteins.get(element), es);
				}
				localb/=(cluster2.size());
//				System.out.println("localb: " + localb);
				if(localb>b){
					b=localb;
				}
			}
//			System.out.println("a: "  + a);
//			System.out.println("b: " + b);
			if(cluster.size()!=1){
				double dum = (a-b)/(Math.max(a, b));
				silhouette+=dum;
//				System.out.println("dum: " + dum);
			}else{
				count++;
			}
			
			
			
//			System.out.println("silhouette: " + silhouette);
		}
		silhouette/=(protein2cluster.size()-count);
//		System.out.println("silhouette: " + silhouette);
		
		return silhouette;
	}

}

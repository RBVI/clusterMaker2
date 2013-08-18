package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.greedy;

public class AffinityObject implements Comparable{

	public int id;
	
	public double affinity;
	
	public AffinityObject(int id){
		this.id=id;
	}
	
	public int compareTo(AffinityObject a) {	
		if(this.affinity<a.affinity){
			return -1;
		}else if(this.affinity>a.affinity){
			return 1;
		}else{
			return 1;
		}
	}

	public int compareTo(Object o) {
                AffinityObject a = (AffinityObject) o;
                if(this.affinity<a.affinity){
			return -1;
		}else if(this.affinity>a.affinity){
			return 1;
		}else{
			return 1;
		}
//		int result = this.compareTo((AffinityObject) o);
//		return result;
	}

	

}

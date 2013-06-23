package org.cytoscape.myapp.internal.algorithms.networkClusterers.MCL;

import java.util.Vector;

public class Clustering {
	
	private double clusteringThresh; 
	private Vector <Vector> clusters;
	public int numClusters = -1;

	public Clustering(int size,double clusteringThresh)
	{
		clusters = new Vector(size);
		
		//initally, each element is in its own cluster
		for(int i=0; i<size; i++)
		{
			Vector <Integer> v = new Vector();
			v.add(new Integer(i));
			clusters.add(v);
		}
		
		clusteringThresh = this.clusteringThresh;
	}
	
	//Find the cluster that contains a particular element
		private int getClusterIndex(int element)
		{
			for(int i=0; i<clusters.size(); i++)
			{
				Vector v = (Vector)clusters.elementAt(i);
				
				if (v.contains(new Integer(element)))
					return i;
			}
			
			return -1;
		}
		
		//combine two clusters together into a single cluster
		private void combineClusters(int index1,int index2)
		{
			Vector <Integer> cluster1 = (Vector<Integer>)clusters.elementAt(index1);
			Vector <Integer> cluster2 = (Vector<Integer>)clusters.elementAt(index2);
			Vector <Integer> cluster3 = new Vector<Integer>(cluster1);
			cluster3.addAll(cluster2);
			clusters.add(cluster3);
			clusters.removeElementAt(index1);
			
			//take into account shift in indices after removal of first element
			if(index2 < index1)
				clusters.removeElementAt(index2);
			else
				clusters.removeElementAt(index2-1);
			
		
		}
		
		//checks indeces of two elements and clusters them together if they are not clustered allready
		private void clusterElements(int element1,int element2)
		{
			int index1 = getClusterIndex(element1);
			int index2 = getClusterIndex(element2);
			
			
			if (index1 != index2)
				if((index1 != -1) && (index2 != -1))
					combineClusters(index1,index2);
		}
		
		//Takes as input double array representing network and clusters the nodes. Returns an array mapping each node to a specific cluster
		public double[] clusterMatrix(double[][] graph)
		{
			double[] clusterArray = new double[graph.length];
			int element;
			
			//go through matrix and cluster connected elements
			for(int i=0; i<graph.length; i++)
				for(int j=0; j<graph.length; j++)
				{
					if(i == j)
						continue;
					
					if(graph[i][j] > clusteringThresh)
						clusterElements(i,j);
				}
			
			
			numClusters = clusters.size();
			
			//Assign each node to a cluster
			for(int i=0; i<clusters.size(); i++)
			{
				Vector v = (Vector)clusters.elementAt(i);
				
				for(int j=0; j < v.size(); j++)
				{
					element = ((Integer)v.elementAt(j)).intValue();
					clusterArray[element] = i;
				}
			}
			
			return clusterArray;
		}
}


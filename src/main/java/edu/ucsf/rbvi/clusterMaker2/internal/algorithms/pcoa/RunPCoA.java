package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;

public class RunPCoA {

	CalculationMatrix calculationMatrix;
	double inputdata[][];
	
	public RunPCoA(int rows,int columns,double inputdata[][]){
		calculationMatrix=new CalculationMatrix(rows, columns, inputdata);
			
			
	}
	public CalculationMatrix getCalculationMatrix(){
		return calculationMatrix;
	}
	
	public static void main(String args[]){
		double inputdata[][]={{1,2,3},{2,4,5},{3,5,6}};
		//RunPCoA runpcoa=new RunPCoA(3, 3, inputdata);
		//System.out.println(runpcoa.getCalculationMatrix().isSymmetrical());
		ComputationMatrix computationmatrix=new ComputationMatrix(inputdata);
		double compval[][]=computationmatrix.eigenVectors();
		double eigenval[]=computationmatrix.eigenValuesAll();
		System.out.println("Eigen vectors");
		for(int i=0;i<compval.length;i++){
			for(int j=0;j<compval.length;j++){
				System.out.print(compval[i][j]+" ");
			}
			System.out.println("");
		}
		System.out.println("Eigen values");
		int count=0;
		double tempeigen[]=new double[eigenval.length];
		double tolerance=Math.sqrt(Math.pow(2, -52));//get tolerance to reduce eigens
		for(int i=0;i<eigenval.length;i++){
			if(Math.abs(eigenval[i])>tolerance){
				tempeigen[i]=1;
				count++;
			}
			System.out.println(eigenval[i]);
		}
		
		double idx[]=new double[count];
		System.out.println("Temp Eigens");
		for(int i=0;i<tempeigen.length;i++){
			if(tempeigen[i]!=0){
				idx[i]=i+1;
			}
			System.out.print(tempeigen[i]+" ");
		}
		ArrayList<Double> arrayList=new ArrayList<Double>();
		System.out.println("IDX Values");
		for(int i=0;i<idx.length;i++){
			System.out.print(idx[i]+" ");
		}
		
	}
}

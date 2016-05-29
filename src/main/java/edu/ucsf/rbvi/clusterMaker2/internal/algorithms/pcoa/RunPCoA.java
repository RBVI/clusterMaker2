package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.netlib.util.doubleW;

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
		double inputdata[][]={{0,3,4,5},{3,0,6,3},{4,6,0,1},{5,3,1,0}};
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
		
		}
		
	}


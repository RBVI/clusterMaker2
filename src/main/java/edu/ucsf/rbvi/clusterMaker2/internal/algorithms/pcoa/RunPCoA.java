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
		calculationMatrix=new CalculationMatrix(rows, columns, inputdata,0,0,0);
			
			
	}
	public CalculationMatrix getCalculationMatrix(){
		return calculationMatrix;
	}
	
	public static void main(String args[]){
		//double inputdata[][]={{0,3,4,5},{3,0,6,3},{4,6,0,1},{5,3,1,0}};
		double inputdata[][]={{0,12,5,7,1},{12,0,8,3,11},{5,8,0,2,6},{7,3,2,0,4},{1,11,6,4,0}};
		//double inputdata[][]={{0,3,4},{3,0,1},{4,1,0}};
		RunPCoA runPCoA=new RunPCoA(inputdata.length, inputdata.length, inputdata);
		//runPCoA.getCalculationMatrix().setNeg(0);
		double scores[][]=runPCoA.getCalculationMatrix().getScores();
		double combine_array[][]=runPCoA.getCalculationMatrix().getCombine_array();
		double evals[]=runPCoA.getCalculationMatrix().getEigen_values();
		
		System.out.println("Eigen Values");
		for(int i=0;i<evals.length;i++){
			System.out.print(evals[i]+" ");
		}
		System.out.println("");
		System.out.println("Combine Array");
		for(int i=0;i<combine_array.length;i++){
			for(int j=0;j<2;j++){
				System.out.print(combine_array[i][j]+" ");	
			}
			System.out.println("");
		}
		System.out.println("Scores Array");
		for(int i=0;i<scores.length;i++){
			for(int j=0;j<scores.length;j++){
				System.out.print(scores[i][j]+" ");
			}
			System.out.println("");
		}
		}
	}


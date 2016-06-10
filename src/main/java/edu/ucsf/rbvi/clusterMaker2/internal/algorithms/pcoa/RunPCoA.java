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
		
			}
	}


/*
***********************************************************************************************
SOFTWARE USE AGREEMENT

Conditions of Use:
AutoSOME is freely available to the academic/non-profit community for non-commercial research
purposes.

All downloads are subject to the following terms: Software and source code Copyright (C) 2009
Aaron M. Newman. Permission to use this software and its documentation is hereby granted to
all academic and not-for-profit institutions for non-profit/non-commercial applications
without fee. The right to use this software for profit, by private companies or other organizations, or in
conjunction with for profit activities, are NOT granted except by prior arrangement and written
 consent of the copyright holder.

For these purposes, downloads of the software constitutes "use" and downloads of this software
 by for profit organizations and/or distribution to for profit institutions is explicitly
prohibited without the prior consent of the copyright holder.

The software is provided "AS-IS" and without warranty of any kind, express, implied or
otherwise. In no event shall the copyright holder be liable for any damages of any kind
arising out of or in connection with the use or performance of this software. This code was
written using Java and may be subject to certain additional restrictions as a result.
***********************************************************************************************
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch;

import java.io.Serializable;
import java.util.*;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct.dataItem;
/**
 * stores all parameter values set by user
 * @author Aaron
 */
public class Settings implements Serializable{
    
    public String inputFile = new String(); //input file
    public int threads = Runtime.getRuntime().availableProcessors(); //how many threads?
    public int som_iters = 500; //number of SOM iterations for each of 2 stages
    public int som_minGrid = 5; //minimum single dimension grid size of SOM (x or y)
    public int som_maxGrid = 30; //maximum single dimension grid size of SOM (x or y)
    public int som_gridSize = 0; //If 0, automatically set x/y to sqrt(number of data points * 2), o.w. use user value
    public boolean som_circle = true; //use SOM circular topology if true, square if false
    public double som_theta = 1.5; //error dampening factor; error is divided by theta
    public double som_DEScale = 3; //set error to this number as an exponent
    public int de_resolution = 32; //diffusion resolution: must be a power of 2
    public boolean doCart = true; //perform density-equalization
    public int sm_iters = 100; //number of sammon mapping iterations
    public boolean doSM = false; //do sammon mapping
    public int mst_MC = 10; //number of monte carlo simulations for minumum spanning tree
    public double mst_pval = .1;//p value cutoff for minumum spanning tree
    public int ensemble_runs = 50;//number of cluster runs to merge into ensemble clustering
    public int known_clusters = 0;//set number of known clusters a priori
    public boolean doKmeans = false;//do k-means clustering
    public boolean doHierarchical = false;//do hierarchical clustering
    public int hierarchical_choice = 4; //1=single,2=complete,3=average,4=wards linkage distance metric
    public String outputDirectory = "C:"; //print results to this directory
    public boolean htmlOut = true; //html output
    public boolean textOut = true;//text comma delimited output
    public boolean display3D = false; //show interactive 3D display of clustering if true
    public boolean display2D = false; //show 2D cluster browser if true
    public boolean noMapping = false; //do dimensional reduction on input data?
    public boolean verbose = false; //verbose output
    public boolean batch = false; //use batch mode : run on all files in specified directory
    public boolean benchmark = false; //calculate benchmark metrics
    public boolean unitVar = false; //if true, normalize input to unit variance for each column
    public boolean logNorm = false; //if true, normalize input to logarithm base 2
    public int scale = 0; //normalize input to range [0,scale] for each column
    public int trials = 10; //number of trials for each benchmarking dataset
    public int inputSize; //size of inputdataset
    public boolean confidence = true; //if true, print out ensemble confidence values
    public int conf_Thresh = 0; // only report data items with cluster membership confidence above this amount
    public dataItem[] input; //store input
    public float inputMin; //minimum value in input
    public float inputMax; //maximum value in input
    public List<String> discarded = new ArrayList<String>(); //store data items discarded by confidence threshold
    public String subFile = new String(); //store path to AA substitution alphabet for TR clustering
    public int startData = 1; //column from left where data item numerical values start (leftmost column = 0)
    public int yStartData = 1; //row from top where first numerical data item is located (top column=0)
    public boolean invokeViewerOnly = false; //if true, display previous clustering run in 2D viewer
    public String runTime = new String(); //running time of AutoSOME
    public String[] columnHeaders; //store names of columns
    public String[][] metaColumnLabels; //store meta-data for each column
    public boolean readColumns = false; //read in column headers if true
    public boolean trBM = false; //do tr benchmark
    public float[] center; //mean value for each attribute
    public boolean Pearson = false; //use pearson correlation coefficient for SOM distance metric
    public boolean unCentered = false; //use uncentered correlation for SOM distance metric
    public boolean distMatrix = false; //if true, transform input columns into distance matrix
    public int dmDist = 1; //if 1, use Euclidean distance for distance matrix, 2 = Pearson, 3 = Uncentered Corr
    public boolean medCenter = false; //if true, perform median centering of rows
    public boolean medCenterCol = false; //if true, perform median centering of columns
    public boolean unitVarAfterDM = false; //if true, apply unit variance normalization to distance matrix
    public boolean printConsMatrix = false; //if true, print cluster consensus matrix
    public boolean writeTemp = false; //if true, write intermediate data to disk to save memory
    public boolean sumSqrRows = false; //if true, sum of squares of data row = 1
    public boolean sumSqrCol = false; //if true, sum of squares of data column = 1
    public String add = new String(); //run information to be added to output files
    public boolean PCLformat = false; //read PCL (Eisen) format if true
    public boolean GEOformat = false; //read GEO format if true
    public double[] EWEIGHT; //hold array weights if PCL format
    public boolean mvMedian = true; //if true, use median to fill missing values, o.w. use mean
    public boolean mvCol = true; //if true, use median of columns for filling missing values, o.w. rows
    public int[] columnClusters; //record clustered columns as integer vector (1,1,1=cluster1)
    public int[] oldOrder; //original column order
    public HashMap kept = new HashMap(); //if data row kept after filtering, store row id
    public int printRowsCols=0; //if 1 print rows output separately from columns, 2 prints columns, 3 prints both
    public boolean fillMissing = false; //fill in missing values?
    public boolean FCNrows = false; //if false, do FCN on columns

    
    public void setParams(String[] args){
        if(args.length > 1) System.out.println("===================Settings====================");
        for(int i = 0; i < args.length; i++){
            if(args[i].charAt(0)=='-'){
                String setting = args[i].substring(1);
                switch(setting.charAt(0)){
                    
                    case 't':
                        threads = Integer.valueOf(setting.substring(1));
                        System.out.println(">Number Of CPUs Set To: "+threads);
                    break;
                    case 'i':
                        som_iters = Integer.valueOf(setting.substring(1));
                        System.out.println(">SOM Iterations Set To: "+som_iters);
                    break;
                    case 'm':
                        som_minGrid = Integer.valueOf(setting.substring(1));
                        System.out.println(">SOM Minimum Grid Size Set To: "+som_minGrid);
                    break;
                    case 'M':
                        som_maxGrid = Integer.valueOf(setting.substring(1));
                        System.out.println(">SOM Maximum Grid Size Set To: "+som_maxGrid);
                    break;
                    case 'g':
                        som_gridSize = Integer.valueOf(setting.substring(1));
                        System.out.println(">SOM square x/y grid size set to: "+som_gridSize);
                    break;
                    case 's':
                        som_circle = false;
                        System.out.println(">SOM Topology Set To Square");
                    break;
                    case 'd':
                        som_theta = Double.valueOf(setting.substring(1));
                        System.out.println(">SOM error dampener 'theta'  set to: "+som_theta);
                    break;
                    case 'x':
                        som_DEScale = Double.valueOf(setting.substring(1));
                        System.out.println(">SOM error raised to exponent: "+som_DEScale);
                    break;
                    case 'r':                        
                        de_resolution = Integer.valueOf(setting.substring(1));
                        System.out.println(">Cartogram x/y resolution set to: "+de_resolution);
                    break;
                    case 'E':
                        doCart = false;
                        System.out.println(">Disable Density-Equalizing Cartogram");
                    break;
                    case 'S':
                        doSM = true;
                        String xtra = new String();
                        if(setting.length()>1){
                            sm_iters = Integer.valueOf(setting.substring(1));
                            xtra = " with "+sm_iters+" iterations";
                        }                           
                        System.out.println(">Do Sammon Mapping"+xtra);
                    break;
                    case 'c':
                        mst_MC = Integer.valueOf(setting.substring(1));
                        System.out.println(">Monte Carlo simulations set to: "+mst_MC);
                    break;
                    case 'p':
                        mst_pval = Double.valueOf(setting.substring(1));
                        System.out.println(">Minimum Spanning Tree P-value set to: "+mst_pval);
                    break;
                    case 'e':
                        ensemble_runs = Integer.valueOf(setting.substring(1));
                        if(ensemble_runs==1) System.out.println(">Ensemble disabled");
                        else System.out.println(">Merge "+ensemble_runs+" Clustering Runs");
                    break;
                    case 'k':
                        known_clusters = Integer.valueOf(setting.substring(1));
                        System.out.println(">Number Of Clusters Set To: "+known_clusters);
                    break;
                    case 'K':
                        doKmeans = true;
                        System.out.println(">Clustering Algorithm: K-means");
                    break;
                    case 'A':
                        doHierarchical = true;
                        String choice = "Ward's method";
                        if(setting.length()>1) {
                            hierarchical_choice = Integer.valueOf(setting.substring(1));
      
                            switch(hierarchical_choice){
                                case 1:
                                    choice = "Single Linkage";
                                    break;
                                case 2:
                                    choice = "Complete Linkage";
                                    break;
                                case 3:
                                    choice = "Average Linkage";
                                    break;
                                case 4:
                                    choice = "Ward's Method";
                                    break;
                            }
                        }
                        System.out.println(">Clustering Algorithm: "+choice);
                    break;
                    case 'D':
                        outputDirectory = setting.substring(1);
                        System.out.println(">Write Output to: "+outputDirectory);
                    break;
                    case 'H':
                        htmlOut = false;
                        System.out.println(">No html Output");
                    break;
                    case 'O':
                        textOut = false;
                        System.out.println(">No Text Output");
                    break;
                    case 'v':
                        
                        if(setting.length()>1){
                            if(Integer.valueOf(setting.substring(1))==2){
                                System.out.println(">Display Clustering Results");
                                invokeViewerOnly = true;
                            }/*else
                            if(Integer.valueOf(setting.substring(1))==2){
                                System.out.println(">Launch 2D Cluster Browser");
                                display2D = true;
                            }
                            if(Integer.valueOf(setting.substring(1))==1){
                                System.out.println(">Launch 2D and 3D Cluster Viewers");
                                display2D = display3D = true;
                            }*/
                        }else{
                           display2D = true;
                           System.out.println(">Launch Interactive Cluster Viewer");
                        }
                    break;
                    case 'R':
                        noMapping = true;
                        System.out.println(">Do not perform dimensional reduction on input data");
                    break;
                    case 'V':
                        verbose = true;
                        System.out.println(">Print Verbose Output");
                    break;
                    case 'B':
                        batch = true;
                        System.out.println(">Using Batch Mode");
                    break;
                    case 'b':
                        benchmark = true;
                        System.out.println(">Print Benchmark Values");
                        if(setting.length()>1){
                            trials = Integer.valueOf(setting.substring(1));
                            System.out.println(">"+trials+" trial runs per dataset");
                        }
                        //htmlOut = false;
                        //textOut = false;
                    break;
                     case 'n':
                        int normChoice = Integer.valueOf(setting.substring(1));
                        if(normChoice == 1){
                            unitVar = true;
                            System.out.println(">Normalize Input to Unit Variance");
                        }else if(normChoice == 0){
                           logNorm = true;
                            System.out.println(">Normalize Input to Logarithm Base 2");                  
                        } else{
                            scale = Integer.valueOf(setting.substring(1));
                            System.out.println(">Normalize Input to Range [0,"+scale+"]");
                            
                        }
                    break;
                    case 'N':
                           logNorm = true;
                           unitVar = true;
                           System.out.println(">Normalize Input: Log Base 2 with Unit Var");
                    break;

                   
                    case 'F':
                        if(setting.length() > 1){
                            conf_Thresh = Integer.valueOf(setting.substring(1));
                            System.out.println(">Confidence Threshold: "+conf_Thresh);
                        }else{
                             confidence = false;
                            System.out.println(">Do Not Report Cluster Confidence Values");
                        }
                    break; 
                   
                    case 'l':
                        startData = Integer.valueOf(setting.substring(1));
                        System.out.println(">Start Reading Numerical Data From Column: "+startData); 
                    break; 
                    case 'C':
                        readColumns = true;;
                        System.out.println(">Read in Column Headers from First Row of Input"); 
                    break; 
                    case 'P':
                        if(setting.length() > 1){
                            unCentered = true;
                            System.out.println(">SOM Distance Metric: Uncentered Correlation");
                        }else{
                            Pearson = true;
                            System.out.println(">SOM Distance Metric: Pearson Correlation"); 
                        }
                    break;
                    case 'Q':
                        distMatrix = true;
                        printConsMatrix = true;
                        System.out.println(">Transform Columns into Distance Matrix"); 
                        if(setting.length() > 1){
                            dmDist = Integer.valueOf(setting.substring(1));
                            System.out.println(">Distance Matrix: "+((dmDist==2) ? "Pearson's" : "Uncentered")+" Correlation");
                        }
                    break; 
                     case 'j':

                        if(setting.length() > 1){
                            int Choice = Integer.valueOf(setting.substring(1));
                            if(Choice==1){
                                medCenter=true;
                                System.out.println(">Median Center Normalization of All Rows");
                            }else if(Choice==2){
                                medCenterCol=true;
                                System.out.println(">Median Center Normalization of All Columns");
                            }else if(Choice==3){
                                medCenter=true;
                                medCenterCol=true;
                                System.out.println(">Median Center Normalization of All Rows and Columns");
                            }
                        }else {
                            medCenter=true;
                            System.out.println(">Median Center Normalization of All Rows");
                        }
                        
                    break;
                    case 'u':

                        if(setting.length() > 1){
                            int Choice = Integer.valueOf(setting.substring(1));
                            if(Choice==1){
                                medCenter=true;
                                System.out.println(">Sum of Squares=1 Normalization of All Rows");
                            }else if(Choice==2){
                                medCenterCol=true;
                                System.out.println(">Sum of Squares=1 Normalization of All Columns");
                            }else if(Choice==3){
                                medCenter=true;
                                medCenterCol=true;
                                System.out.println(">Sum of Squares=1 Normalization of All Rows and Columns");
                            }
                        }else {
                            medCenter=true;
                            System.out.println(">Median Center Normalization of All Rows");
                        }

                    break;
                    case 'h':

                        if(setting.length() > 1){
                            int Choice = Integer.valueOf(setting.substring(1));
                            if(Choice==1){
                                mvMedian=false;
                                mvCol=false;
                                System.out.println(">Fill Missing Values with Means of Rows");
                            }else if(Choice==2){
                                mvMedian=true;
                                mvCol=false;
                                System.out.println(">Fill Missing Values with Medians of Rows");
                            }else if(Choice==3){
                                mvMedian=false;
                                mvCol=true;
                                System.out.println(">Fill Missing Values with Means of Columns");
                            }
                            else if(Choice==4){
                                mvMedian=true;
                                mvCol=true;
                                System.out.println(">Fill Missing Values with Medians of Columns");
                            }
                        }else {
                            mvMedian=false;
                            mvCol=false;
                            System.out.println(">Fill Missing Values with Means of Rows");
                        }

                    break;
                    case '@':
                        printConsMatrix = true;
                        System.out.println(">Print Consensus Cluster Matrix"); 
                    break;
                     case '$':
                        writeTemp = true;
                        System.out.println(">Write Intermediate Data to Temp Folder"); 
                    break;
                     case 'o':
                        printDefaultSettings();
                        System.exit(1);
                    break;
                    case '#':
                        unitVarAfterDM = true;
                        System.out.println(">Apply Unit Variance Normalization to Distance Matrix");
                    break;
                    case 'w':
                        PCLformat = true;
                        System.out.println(">Parse PCL formatted input file.");
                    break;
                    case 'W':
                        GEOformat = true;
                        System.out.println(">Parse Gene Expression Omnibus Series Matrix formatted input file.");
                    break;
                    
                }
            }else inputFile = args[i];
            
        }
        if(args.length > 1) System.out.println("===============================================\n");
    }
    
    public void printDefaultSettings(){
        System.out.println("\n>Usage:\njava -jar autosome.jar [Input] [Options]\n\n" +
                ">maximum JVM memory recommended, e.g. \njava -jar -Xmx1600m -Xms1600m -jar autosome.jar input.txt -p.01 -e30 -v -DC:\\out\n\n" +
      
                ">Options:\nparameter | description (default value)\n\n" +
                "-t[integer] set number of threads (available CPUs)\n" +
                "-e[integer] set number of runs to merge into ensemble (50)\n" +
                "-p[0-1] set p-value threshold for minimum spanning tree clustering (0.1)\n" +
                "-D[directory] set output directory (same directory as input file)\n" +
                "-C read in column headers from first row of input file (auto-detect otherwise)\n" +
                "-v launch cluster viewer (false)\n" +
                "-v2 open previous clustering results: input=clustering output text file (false)\n" +
                "-n[integer] normalize input by unit variance '-n1', to log2 '-n0' or into range [0,X] '-nX' (false)\n" +
                "-j[1,2,3] 1=perform median center normalization on all rows;2=columns;3=rows and columns\n"+
                "-u[1,2,3] 1=perform sum of squares=1 normalization on all rows;2=columns;3=rows and columns\n"+
                "-N normalize input to log2 with unit variance (false)\n" +
                "-# apply unit variance normalization to distance matrix (false)\n" +
                "-w read in PCL-formatted input file (false)\n" +
                "-W read in Gene Expression Omnibus Series Matrix-formatted input file (false)\n" +
                "-l[integer] start reading numerical data from this column, lowest column = 0 (1)\n"+
                "-Q transform columns from input into Euclidean distance matrix (false)\n"+
                "-Q[2,3] distance matrix metric; 2 = Pearson's, 3 = Uncentered Correlation (Euclidean)\n"+
                "-h[1,2,3,4] fill missing values; 1=means of rows;2=medians of rows;\n\t\t3=means of columns;4=medians of columns  (means of rows)\n"+
                "-c[integer] set number of Monte Carlo simulations for MST clustering (10)\n" +
                "-g[integer] set x of SOM grid xy, where y=x (square root of input size*2)\n" +
                "-M[integer] set maximum x/y grid of SOM (30)\n" +
                "-m[integer] set minimum x/y grid of SOM (5)\n" +
                "-P set SOM distance metric to Pearson Correlation (Euclidean)\n" +
                "-P2 set SOM distance metric to Uncentered Correlation (Euclidean)\n" +
                "-s set SOM topology to square (circle)\n" +
                "-i[integer] set number of SOM iterations (500)\n" +
                "-x[integer] set SOM error surface exponent (3)\n" +
                "-r[power of 2] set density-equalizing cartogram resolution (32)\n" +
                "-E disable Density-Equalizing Cartogram (false)\n" +
                "-R disable SOM; use only if clustering with K-Means or Agglomerative (false)\n" +
                "-S invoke Sammon Mapping instead of SOM (false)\n" +
                "-S[integer] set number of Sammon Mapping iterations (100)\n" +
                "-k[integer] specify number of clusters in dataset (false)\n" +
                "-K invoke K-Means Clustering; requires option -k (false)\n" +
                "-A invoke Agglomerative Clustering; requires option -k (false)\n" +
                "-A[method] 1=Single, 2=Complete, 3=Average, 4=Ward's (4)\n" +
                "-V print verbose output (false)\n");/* +
                "-T use amino acid tandem repeat mode (false)\n"+
                "-T1 read in TR database IDs as first column (false)\n"+
                "-X Do TR Hierarchical Clustering (false)\n" +
                "-X[integer] Set TR Hierarchical Clustering Iterations (1)\n"+
                "-I[0-1] set TR Consensus Error threshold for hierarchical clustering (0.4)\n" +
                "-U[file] set amino acid substitution alphabet for TR clustering\n");*/
        }
    
    public String getSettings(){

        StringBuffer sb = new StringBuffer();
        sb.append("CPU threads\t"+threads+"\n");  //how many threads?
        sb.append("Input File\t"+inputFile+"\n"); //input file
        sb.append("Input Size\t"+inputSize+"\n"); //input file size
        sb.append("Attributes\t"+input[0].getValues().length+"\n"); //input file size
        if(batch) sb.append("Trials per Dataset:\t"+trials+"\n"); //number of trials
        sb.append("Ensemble Runs\t"+ensemble_runs+"\n");//number of cluster runs to merge into ensemble clustering
        if(noMapping) sb.append("No Dimensional Reduction / Mapping"); //do dimensional reduction on input data?    
        if(!doSM && !noMapping) {
            sb.append("SOM iterations\t"+som_iters+"\n"); //number of SOM iterations
            sb.append("SOM minimum grid\t"+som_minGrid+"\n"); //minimum single dimension grid size of SOM (x or y)
            sb.append("SOM maximum grid\t"+som_maxGrid+"\n"); //maximum single dimension grid size of SOM (x or y)
            sb.append("SOM actual grid\t"+((som_gridSize == 0) ? ((int)Math.min(som_maxGrid, Math.max(som_minGrid, Math.sqrt(inputSize*2)))) : som_gridSize)+"\n"); //If 0, automatically set x/y to sqrt(number of data points * 2), o.w. use user value
            sb.append("SOM topology\t"+((som_circle) ? "circle" : "square")+"\n"); //use SOM circular topology if true, square if false
            if(doCart)sb.append("SOM error dampener (theta)\t"+som_theta+"\n"); //error dampening factor; error is divided by theta
            if(doCart)sb.append("SOM error surface exponent\t"+som_DEScale+"\n"); //set error to this number as an exponent
        }
        if(!noMapping){
            if(doCart)sb.append("Cartogram resolution\t"+de_resolution+"x"+de_resolution+"\n"); //diffusion resolution: must be a power of 2
            if(doSM) sb.append("Sammon Mapping iterations\t"+sm_iters+"\n"); //number of sammon mapping iterations
        }
        if(!doKmeans && !doHierarchical){
            sb.append("MST Monte Carlo Simulations\t"+mst_MC+"\n"); //number of monte carlo simulations for minumum spanning tree
            sb.append("MST P-value cutoff\t"+mst_pval+"\n");//p value cutoff for minumum spanning tree
        }                
        if(benchmark) sb.append("No. Known Clusters\t"+known_clusters+"\n");//set number of known clusters a priori       
        if(doKmeans) sb.append("Kmeans");
        if(doHierarchical) sb.append("Agglomerative Clustering Metric\t"+((hierarchical_choice==1) ? "single" : (hierarchical_choice==2) ? "complete" : (hierarchical_choice==3) ? "average" : "Ward's"+"\n")); //1=single,2=complete,3=average,4=wards linkage distance metric
        if(known_clusters>0) sb.append("User Specified Cluster Number\t"+known_clusters+"\n");
        sb.append("Running Time\t"+runTime+"\n");
        return sb.toString();
    }
    
    //find and store maximum and minimum values in 'input' dataItem array
    public void setInputMinMax(){
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for(int i = 0; i < input.length; i++){
            for(int j = 0; j < input[i].getValues().length; j++){
                if(min > input[i].getValues()[j]) min = input[i].getValues()[j];
                if(max < input[i].getValues()[j]) max = input[i].getValues()[j];
            }
        }
        inputMin = min;
        inputMax = max;
    }

    public String getName(){
        StringTokenizer st = new StringTokenizer(inputFile,"\\");
        String token = new String();
        while(st.hasMoreTokens()) token = st.nextToken();
        String[] tokens = token.split("/");
        token = tokens[tokens.length-1];
        st = new StringTokenizer(token,".");
        token = "";
        for(int i = 0; i < st.countTokens(); i++) {
            token = token.concat(((i>0)?".":"")+st.nextToken());
        }
        return token;
    }
    
    //calculate mean of every column and save in 'center'
    public void setCenter(){
        center = new float[input[0].getValues().length];
        for(int i = 0; i < input.length; i++){
            for(int j = 0; j < input[i].getValues().length; j++){
                if(input[i].getValues()[j] == -999999999f) continue;
                center[j] += input[i].getValues()[j];
            }
        }
        for(int k = 0; k < center.length; k++) center[k] /= input.length;
    }
    
    
    public void convertDMLabels(){
        Map<String,Integer> h = new HashMap<String,Integer>();
        for(int i = 0; i < input.length; i++){
            if(!h.containsKey(input[i].getIdentity())){
                h.put(input[i].getIdentity(),h.size()+1);
                input[i].setIdentity(String.valueOf(h.size()));
            }else{
                input[i].setIdentity(h.get(input[i].getIdentity()).toString());
            }
        }
    }

    public String getFolderDivider(){

            StringTokenizer st = new StringTokenizer(System.getProperty("os.name"));
            String os = st.nextToken();
            if(os.equals("Mac")){
               return "/";
            }else {
               return "\\";
            }
    }
    
}



# This is git repository for the Cytoscape 3 clustering app: clusterMaker

TODO:
1) Fix BiClusterView
2) Add support for BiClustering routines: 
 * Cheng & Church
 * BiMine
 * BicFinder
3) New potential cluster algorithms
 * CIDR (Lin, Troup, Ho, Genome Biology 2017)
 * SPICi (Speed and Performance In Clustering)





7 JUNE - 23 AUGUST 2021 Google Summer of Code by Maija Utriainen

Implementing remote dimensionality reduction techniques using similar approach to last years remote network clusterers (see below).
  - Dimensionality reduction techniques added: Isomap, Local Linear Embedding, MDS, Spectral, tSNE, UMAP
  - Changes to ClusterJobExecutionService and RemoteServer: abstracting and replacing code to another class
  - Subclasses to ClusterJobHandler specific to network clusterers and dimensionality reduction techniques
  - The new algorithms registered in CyActivator

The added and edited code can be found in clusterMaker2/src/main/java/edu/ucsf/rbvi/clusterMaker2/internal/
  - utils/remoteUtils
    - RemoteServer 
    - ClusterJobExecutionService
    - NetworkClusterJobHandler
    - DimensionalityReductionJobHandler
  - algorithms/dimensionalityRedcution
    - isomap
    - linearEmbedding
    - mds
    - spectral
    - tSNERemote
    - umap
  - CyActivator 




1 JUNE - 29 AUGUST 2020 changes done as a Google Summer of Code Project by Maija Utriainen

Implementing new algorithms with a new approach that runs the algorithm remotely on a server instead of in clusterMaker/Cytoscape utilizing interfaces in cytoscape.jobs package. 
  - The algorithms: Leiden, Infomap, Fast Greedy, Leading Eigenvector, Label Propagation, Multilevel
  - The classes used to carry out the remote clustering job are found in remoteUtils. Most of these classes are extended from cytoscape.jobs interfaces and classes and are:
    ClusterJob, ClusterJobData, ClusterJobDataService, ClusterJobExecutionService, ClusterJobHandler and RemoteServer.
  - The algorithms and ClusterJobExecutionService registered in CyActivator.

My code can be found in several packages in clusterMaker2/src/main/java/edu/ucsf/rbvi/clusterMaker2/internal/
 - utils/remoteUtils
 - algorithms/networkClusterers
    - Leiden
    - Infomap
    - FastGreedy
    - LeadingEigenVector
    - LabelPropagation
    - Multilevel
 - CyActivator

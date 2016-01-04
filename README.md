# This is git repository for the Cytoscape 3 clustering app: clusterMaker

## General TODO's
- Find the shortname of every algorithm that has been used to cluster the
    current network
    - [DONE] Find the shortname of every algorithm
- [DONE] Find out when the SimpleClusterContext object in the SimpleRankTaskFactory is initialized
-  Find the secretome table!!! [IMPORTANT]
    - Example's are to be found within attribute cluster algorithms
        - KMeansCluster/ModelUtil gets the attributes out! start off with node attribute first!
        - [Done] Selecting single attribute
        - Selecting multiple attributes (not for SimpleCluster)
- Checking if ranking of clusters should be able to run ( SimpleCluster.isReady() )
    1. Go through the algorithms in the clusterManager
    2. Check if there exists results from the clustering
        - If not, return false (DOOOH!)

## Refactor TODO's
- Move the getAlgorithms from the SimpleClusterContext? SimpleCluster or corresponding TaskFactory's responsibility?
- Change GetNetworkClusterTask.getResults to return Map<String, List<CyNode>> and convert the string part of the method
    to a separate function (maybe getResultString())

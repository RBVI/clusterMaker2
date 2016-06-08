# This is git repository for the Cytoscape 3 clustering app: clusterMaker

## Design decisions:
- Coloring scheme (including color blind)
    - light grey -> blue -> orange (low - mid - high score)
- Now handles both integer and double values

## Current TODO's:
- Reset colorscheme after closing ranking panel?
- Implement Neo4J support
- Prompt user for manually uploading local network or connecting to Neo4J server
  and import the data.

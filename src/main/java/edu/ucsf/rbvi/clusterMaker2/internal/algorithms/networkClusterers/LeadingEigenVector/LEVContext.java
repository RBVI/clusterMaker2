package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.LeadingEigenVector;

	import org.cytoscape.model.CyNetwork;
	import org.cytoscape.work.ContainsTunables;
	import org.cytoscape.work.Tunable;
	import org.cytoscape.work.swing.TunableUIHelper;
	import org.cytoscape.work.util.ListSingleSelection;

	import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
	import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
	import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
	import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkVizProperties;

	public class LEVContext implements ClusterAlgorithmContext {
		CyNetwork network;
		TunableUIHelper helper;
		
		//Tunables
		
		private ListSingleSelection<String> attribute ;
		@Tunable(description = "Attribute", groups={"Source for array data"}, params="displayState=uncollapsed", 
		         longDescription = "The column containing the data to be used for the clustering. "+
		                           "If no weight column is used, select ```--NONE---```",
		         exampleStringValue = "weight",
		         gravity = 1.0)
		public ListSingleSelection<String> getattribute(){
			attribute = ModelUtils.updateEdgeAttributeList(network, attribute);
			return attribute;
		}
		public void setattribute(ListSingleSelection<String> attr) { }
		
		@Tunable(description = "Synchronous",
			   longDescription = "If ```false``` the algorithm will run in the background after specified wait time",
				 exampleStringValue = "true",
			   tooltip = "<html>If ```false``` the algorithm will run in the background after specified wait time</html>",
				 groups = {"Leading Eigenvector Advanced Settings"}, gravity = 6.0)
		public boolean isSynchronous = false;
		
		@ContainsTunables
		public AdvancedProperties advancedAttributes;

		@ContainsTunables
		public NetworkVizProperties vizProperties = new NetworkVizProperties();

		public LEVContext() {
			advancedAttributes = new AdvancedProperties("__leadingEigenvectorCluster", false); //this is the name of the column Integer that is created when click LOAD
		}

		public LEVContext(LEVContext origin) {
			if (origin.advancedAttributes != null)
				advancedAttributes = new AdvancedProperties(origin.advancedAttributes);
			else
				advancedAttributes = new AdvancedProperties("LeadingEigenvectorCluster", false);

			attribute = origin.attribute;
			isSynchronous = origin.isSynchronous;
		}

		public void setNetwork(CyNetwork network) {
			if (this.network != null && this.network.equals(network))
				return; // Nothing to see here....

			this.network = network;
		}

		public CyNetwork getNetwork() { return network; }

		public String getClusterAttribute() { return advancedAttributes.clusterAttribute;}

		public void setUIHelper(TunableUIHelper helper) {
			this.helper = helper;
			
		}
	
}


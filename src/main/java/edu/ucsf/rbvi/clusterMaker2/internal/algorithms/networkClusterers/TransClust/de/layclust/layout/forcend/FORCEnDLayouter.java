package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend;

import java.util.logging.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayoutInitialiser;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayouter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training.ParameterTraining_SE;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

public class FORCEnDLayouter implements ILayouter {
	
	private static Logger log = Logger.getLogger(ParameterTraining_SE.class.getName());

	private ConnectedComponent cc = null;

	private int dim = -1;

	private FORCEnDParameters parameters = null;

	public FORCEnDLayouter() {
	}

	/**
	 * Constructor for initialising FORCEnDLayouter with an initial layouter
	 * (first layouter). The layout initaliser is then run in the constructor.
	 * 
	 * @param cc
	 *            The connected Component.
	 * @param li
	 *            The layout initialiser.
	 * @param parameters
	 *            The parameters for FORCEnD.
	 */
	public void initLayouter(ConnectedComponent cc, ILayoutInitialiser li,
			IParameters parameters) {
		this.dim = TaskConfig.dimension;
		this.cc = cc;
		this.parameters = (FORCEnDParameters) parameters;
		li.run();
	}

	/**
	 * The constructor for initalising FORCEnDLayouter if a previous
	 * {@link ILayouter} has already been run. Just in case a different
	 * {@link ConnectedComponent} is used here than for the previous layouter,
	 * the positions of the previous layouter are set to this cc.
	 * 
	 * @param cc
	 *            The connected Component.
	 * @param layouter
	 *            The previously run layouter.
	 * @param parameters
	 *            The parameters for FORCEnD.
	 */
	public void initLayouter(ConnectedComponent cc, ILayouter layouter,
			IParameters parameters) {
		this.dim = TaskConfig.dimension;
		this.cc = cc;
		this.parameters = (FORCEnDParameters) parameters;

		/* sets node positions to those of the previous layouter */
		this.cc.setCCPositions(layouter.getNodePositions());
	}

	/**
	 * This Constructor is for the parameter training. Here the positions should
	 * have already been initialised. If this is not so, then the positions are
	 * initialised with the correct {@link ILayoutInitialiser} implementation.
	 * 
	 * @param cc
	 *            The connected Component.
	 * @param parameters
	 *            The parameters for FORCEnD.
	 */
	public void initLayouter(ConnectedComponent cc, IParameters parameters) {
		this.dim = TaskConfig.dimension;
		this.cc = cc;
		this.parameters = (FORCEnDParameters) parameters;

		if (cc.getCCPositions() == null) {
			log.warning("Positions have not been initialised, perhaps"
					+ "wrong use of this constructor!");
			ILayoutInitialiser li;
			li = LayoutFactory.EnumLayouterClass.FORCEND
					.createLayoutInitialiser();
			li.initLayoutInitialiser(cc);
			li.run();
		}
	}

	/**
	 * Runs the FORCEnD algorithm to layout the objects for one
	 * {@link ConnectedComponent}.
	 */
	public void run() {
		this.cc.getCCEdges().normalise();
		int node_no = this.cc.getNodeNumber();
		double[][] node_pos = this.cc.getCCPositions();

		double[][] allDisplacements = new double[node_no][this.dim];
		/*
		 * for each iteration calculate the displacement vectors and move all
		 * nodes by this after calculation in one go
		 */
		
		
		for (int it = 0; it < this.parameters.getIterations(); it++) {
//			for (int it = 0; it < 3; it++) {
			/* the cooling temperature factor for this iteration */
		
			double temperature = FORCEnDLayoutUtility.calculateTemperature(it,node_no, this.parameters);
			
			FORCEnDLayoutUtility.calculateDisplacementVectors(allDisplacements,this.cc, this.dim, this.parameters,temperature);
			FORCEnDLayoutUtility.moveAllNodesByDisplacement(allDisplacements,
					node_pos, node_no, this.dim, temperature);

		}
		this.cc.getCCEdges().denormalise();
	}

	/**
	 * This method is needed for passing on the positions of a previous
	 * ILayouter to the next one.
	 * 
	 * @return The node positions of the object's ConnectedComponent instance.
	 */
	public double[][] getNodePositions() {
		return this.cc.getCCPositions();
	}

}

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CommonOps;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;

public class RunPCoA {

	
	private boolean canceled = false;
	protected int clusterCount = 0;
	
	private final TaskMonitor monitor;
	private final PCoAContext context;
	private final CyMatrix distanceMatrix;
	private final CyNetwork network;
	private final CyNetworkView networkView; 
	private final ClusterManager manager; 
	private CyMatrix result = null;
	
	
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;
	private int neg;
	
	public RunPCoA(final ClusterManager manager, final CyMatrix dMat, final CyNetwork network, 
	               final CyNetworkView networkView,
	               final PCoAContext context, int neg, TaskMonitor monitor )
	{
			
		this.distanceMatrix = dMat;
		this.manager = manager;
		this.monitor = monitor;
		this.neg = neg;
		this.context=context;
		this.network=network;
		this.networkView=networkView;
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Threads = "+nThreads);
		//monitor.showMessage(TaskMonitor.Level.INFO,"Matrix info: = "+distanceMatrix.printMatrixInfo());
	}
	
	public void cancel () { canceled = true; }

	
	public void run(){
		long startTime = System.currentTimeMillis();
		long time = startTime;

		// System.out.println("Calculating values");
		// double data[][]=matrix.toArray();
		// System.out.println("Length "+ distanceMatrix.nRows());

		Matrix mean = distanceMatrix.like(distanceMatrix.nColumns(), 1);

		// TODO: center the matrix?
		for (int j = 0; j < mean.nRows(); j++) {
			mean.setValue(j, 0, CommonOps.columnMean(distanceMatrix, j));
		}

		for (int i = 0; i < distanceMatrix.nRows(); i++) {
			for (int j = 0; j < distanceMatrix.nColumns(); j++) {
				distanceMatrix.setValue(i, j, distanceMatrix.doubleValue(i, j)-mean.doubleValue(j, 0));
			}
		}
		
		//System.out.println("Checking CyMatrix symmetrical "+distanceMatrix.isSymmetrical());

		CalculationMatrix calc = new CalculationMatrix();

		// Get the Gower's Matrix
		Matrix G = GowersMatrix.getGowersMatrix(distanceMatrix);
		long delta = System.currentTimeMillis()-time; time = System.currentTimeMillis();
		monitor.showMessage(TaskMonitor.Level.INFO, "Constructed Gower's Matrix in "+delta+"ms");
		// System.out.println("Got GowersMatrix in "+delta+"ms");
		Matrix V_t = CommonOps.transpose(G.ops().svdV());

		V_t = reshape(V_t, 2, mean.nRows());

		double [][] trafoed = new double[distanceMatrix.nRows()][2];
		result = CyMatrixFactory.makeLargeMatrix(distanceMatrix.getNetwork(), distanceMatrix.nRows(), 2);
		result.setRowNodes(distanceMatrix.getRowNodes());
		result.setRowLabels(Arrays.asList(distanceMatrix.getRowLabels()));
		result.setColumnLabel(0, "X");
		result.setColumnLabel(1, "Y");
		for (int i = 0; i < distanceMatrix.nRows(); i++) {
			trafoed[i] = sampleToEigenSpace(V_t, distanceMatrix, mean, i);
			for (int j = 0; j < trafoed[i].length; j++) {
				result.setValue(i, j, trafoed[i][j] *= -1);
			}
		}

		delta = System.currentTimeMillis()-time; time = System.currentTimeMillis();
		monitor.showMessage(TaskMonitor.Level.INFO, "Completed SVD Analysis in "+delta+"ms");

		if(context.pcoaPlot) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// System.out.println("Scatter plot dialog call");
					ScatterPlotDialog dialog = new ScatterPlotDialog(manager, "PCoA", monitor, result);
				}
			});
		}
	}

	private Matrix reshape(Matrix src, int numRows, int numCols) {
    Matrix result  = src.like(numRows, numCols);
    long index = 0;
    long size = src.nRows()*src.nColumns();
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        double value = 0.0;
        if (index < size) {
          int srcRow = (int)(index/src.nColumns());
          int srcCol = (int)(index%src.nColumns());
          value = src.doubleValue(srcRow, srcCol);
          index++;
        }
        result.setValue(row, col, value);
      }
    }

    return result;
  }

	public double[] sampleToEigenSpace( Matrix V_t, Matrix sampleData, Matrix mean, int row ) {
		Matrix s = distanceMatrix.like(distanceMatrix.nColumns(), 1);
		for (int col = 0; col < distanceMatrix.nColumns(); col++)
			s.setValue(col, 0, sampleData.doubleValue(row, col));

		CommonOps.subtractElement(s, mean);

		Matrix r = CommonOps.multiplyMatrix(V_t.copy(),s);

		return r.getColumn(0);
	}

	CyMatrix getResult() { return result; }

}


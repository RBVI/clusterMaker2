package org.cytoscape.myapp.internal.ui;

public interface HistoChangeListener {
	

    /**
     * This method will be called when the user sets a new
     * bounds value in the histogram
     *
     * @param bounds the value the user set
     */
    void histoValueChanged(double bounds);

}

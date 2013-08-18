package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCODE;

import java.util.HashMap;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 ** User: Gary Bader
 ** Date: Jun 25, 2004
 ** Time: 2:15:10 PM
 ** Description: Singleton class to store the current parameters
 **/

/**
 * Stores the current parameters for MCODE.  Parameters are entered in the MCODEMainPanel and
 * stored in a hash map for the particular network being analyzed by the MCODEScoreAndFindAction
 * if the analysis produced a result.
 */
public class MCODECurrentParameters {
    private static MCODECurrentParameters ourInstance = new MCODECurrentParameters();
    private static HashMap currentParams = new HashMap();
    private static HashMap resultParams = new HashMap();

    /**
     * Get the one instance of this singleton class that stores the current parameters internally.
     * 
     * @return ourInstance
     */
    public static MCODECurrentParameters getInstance() {
        return ourInstance;
    }

    /**
     * Get a copy of the current parameters for a particular network. Only a copy of the current param object is
     * returned to avoid side effects.  The user should use the following code to get their
     * own copy of the current parameters:
     * MCODECurrentParameters.getInstance().getParamsCopy();
     * <p/>
     * Note: parameters can be changed by the user after you have your own copy,
     * so if you always need the latest, you should get the updated parameters again.                                                    
     *
     * @param networkID Id of the network
     * @return A copy of the parameters
     */
    public MCODEParameterSet getParamsCopy(String networkID) {
        if (networkID != null) {
            return ((MCODEParameterSet) currentParams.get(networkID)).copy();
        } else {
            MCODEParameterSet newParams = new MCODEParameterSet();
            return newParams.copy();
        }
    }
    
    /**
     * Current parameters can only be updated using this method.
     * This method is called by MCODEScoreAndFindAction after comparisons have been conducted
     * between the last saved version of the parameters and the current user's version.
     *
     * @param newParams The new current parameters to set
     * @param resultSet Id of the result set
     * @param networkID Id of the network
     */
    public void setParams(MCODEParameterSet newParams, String resultSet, String networkID) {
        //cannot simply equate the params and newParams classes since that creates a permanent reference
        //and prevents us from keeping 2 sets of the class such that the saved version is not altered
        //until this method is called
        MCODEParameterSet currentParamSet = new MCODEParameterSet(
                newParams.getScope(),
                newParams.getSelectedNodes(),
                newParams.isIncludeLoops(),
                newParams.getDegreeCutoff(),
                newParams.getKCore(),
                newParams.isOptimize(),
                newParams.getMaxDepthFromStart(),
                newParams.getNodeScoreCutoff(),
                newParams.isFluff(),
                newParams.isHaircut(),
                newParams.getFluffNodeDensityCutoff()
        );

        currentParams.put(networkID, currentParamSet);

        MCODEParameterSet resultParamSet = new MCODEParameterSet(
                newParams.getScope(),
                newParams.getSelectedNodes(),
                newParams.isIncludeLoops(),
                newParams.getDegreeCutoff(),
                newParams.getKCore(),
                newParams.isOptimize(),
                newParams.getMaxDepthFromStart(),
                newParams.getNodeScoreCutoff(),
                newParams.isFluff(),
                newParams.isHaircut(),
                newParams.getFluffNodeDensityCutoff()
        );

        resultParams.put(resultSet, resultParamSet);
    }

    public static MCODEParameterSet getResultParams(String resultSet) {
        return ((MCODEParameterSet) resultParams.get(resultSet)).copy();
    }

    public static void removeResultParams(String resultSet) {
        resultParams.remove(resultSet);
    }
}

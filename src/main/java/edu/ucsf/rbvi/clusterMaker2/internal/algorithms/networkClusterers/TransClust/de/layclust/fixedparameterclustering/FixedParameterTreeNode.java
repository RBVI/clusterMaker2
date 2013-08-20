package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.fixedparameterclustering;

import java.util.ArrayList;
import java.util.Arrays;

public class FixedParameterTreeNode {

    public float[][] edgeCosts;
    public boolean[][] clusters;
//    public float[][] mergeCosts;
//    public float[][] deleteCosts;
//    public boolean[][] edge;
    public float costs;
    public int size;

    public FixedParameterTreeNode(int size, float costs, int elementNumber) {

        this.size = size;
        this.edgeCosts = new float[size][size];
        this.clusters = new boolean[size][elementNumber];
//        this.mergeCosts = new float[size][size];
//        this.deleteCosts = new float[size][size];
//        this.edge = new boolean[size][size];
        this.costs = costs;
    }

    public FixedParameterTreeNode copy() {
        FixedParameterTreeNode fptn = new FixedParameterTreeNode(this.size, this.costs, this.clusters[0].length);

        for (int i = 0; i < this.size; i++) {
            fptn.edgeCosts[i] = Arrays.copyOf(this.edgeCosts[i], this.size);
            fptn.clusters[i] = Arrays.copyOf(this.clusters[i], this.clusters[i].length);
        }
        return fptn;
    }

//    public void calculateCosts() {
//
//        for (int i = 0; i < this.size; i++) {
//            for (int j = i + 1; j < this.size; j++) {
//                if (edge[i][j]) {
//                    calculateCostForMergingandForbidden(i, j);
//                }
//            }
//
//        }
//
//
//    }

//    public void calculateCostsAfterSettingForbidden(int node_i, int node_j) {
//        for (int i = 0; i < node_i; i++) {
//            if(edge[node_i][i]){
//                calculateCostForMergingandForbidden(i, node_i);
//            }
//            if(edge[node_j][i]){
//                calculateCostForMergingandForbidden(i, node_j);
//            }
//        }
//        for (int i = node_i+1; i < node_j; i++) {
//            if(edge[node_i][i]){
//                calculateCostForMergingandForbidden(node_i, i);
//            }
//            if(edge[node_j][i]){
//                calculateCostForMergingandForbidden(i, node_j);
//            }
//        }
//        for (int i = node_j+1; i < this.size; i++) {
//            if(edge[node_i][i]){
//                calculateCostForMergingandForbidden(node_i, i);
//            }
//            if(edge[node_j][i]){
//                calculateCostForMergingandForbidden(node_j, i);
//            }
//        }
//
//
//    }

//    public void calculateCostForMergingandForbidden(int node_i, int node_j) {
//        mergeCosts[node_i][node_j] = 0;
//        deleteCosts[node_i][node_j] = 0;
//
//        for (int i = 0; i < node_i; i++) {
//            if (!edge[i][node_i]) {
//                if (edge[i][node_j]) {
//                    if(-edgeCosts[i][node_i]>edgeCosts[i][node_j]){
//                        mergeCosts[node_i][node_j] += edgeCosts[i][node_j];
//                    }else{
//                        mergeCosts[node_i][node_j] -= edgeCosts[i][node_i];
//                    }
//                }
//            } else {
//                if (!edge[i][node_j]) {
//                    if(edgeCosts[i][node_i]>-edgeCosts[i][node_j]){
//                        mergeCosts[node_i][node_j] -= edgeCosts[i][node_j];
//                    }else{
//                        mergeCosts[node_i][node_j] += edgeCosts[i][node_i];
//                    }
//                }else{
//                    if(edgeCosts[i][node_i]>edgeCosts[i][node_j]){
//                        deleteCosts[node_i][node_j] += edgeCosts[i][node_j];
//                    }else{
//                        deleteCosts[node_i][node_j] += edgeCosts[i][node_i];
//                    }
//                }
//            }
//        }
//        for (int i = node_i+1; i < node_j; i++) {
//            if (!edge[i][node_i]) {
//                if (edge[i][node_j]) {
//                    if(-edgeCosts[i][node_i]>edgeCosts[i][node_j]){
//                        mergeCosts[node_i][node_j] += edgeCosts[i][node_j];
//                    }else{
//                        mergeCosts[node_i][node_j] -= edgeCosts[i][node_i];
//                    }
//                }
//            } else {
//                if (!edge[i][node_j]) {
//                    if(edgeCosts[i][node_i]>-edgeCosts[i][node_j]){
//                        mergeCosts[node_i][node_j] -= edgeCosts[i][node_j];
//                    }else{
//                        mergeCosts[node_i][node_j] += edgeCosts[i][node_i];
//                    }
//                }else{
//                    if(edgeCosts[i][node_i]>edgeCosts[i][node_j]){
//                        deleteCosts[node_i][node_j] += edgeCosts[i][node_j];
//                    }else{
//                        deleteCosts[node_i][node_j] += edgeCosts[i][node_i];
//                    }
//                }
//            }
//        }
//        for (int i = node_j+1; i < size; i++) {
//            if (!edge[i][node_i]) {
//                if (edge[i][node_j]) {
//                    if(-edgeCosts[i][node_i]>edgeCosts[i][node_j]){
//                        mergeCosts[node_i][node_j] += edgeCosts[i][node_j];
//                    }else{
//                        mergeCosts[node_i][node_j] -= edgeCosts[i][node_i];
//                    }
//                }
//            } else {
//                if (!edge[i][node_j]) {
//                    if(edgeCosts[i][node_i]>-edgeCosts[i][node_j]){
//                        mergeCosts[node_i][node_j] -= edgeCosts[i][node_j];
//                    }else{
//                        mergeCosts[node_i][node_j] += edgeCosts[i][node_i];
//                    }
//                }else{
//                    if(edgeCosts[i][node_i]>edgeCosts[i][node_j]){
//                        deleteCosts[node_i][node_j] += edgeCosts[i][node_j];
//                    }else{
//                        deleteCosts[node_i][node_j] += edgeCosts[i][node_i];
//                    }
//                }
//            }
//        }
//        deleteCosts[node_i][node_j] += edgeCosts[node_i][node_j];
//        mergeCosts[node_j][node_i] = mergeCosts[node_i][node_j];
//        deleteCosts[node_j][node_i] = deleteCosts[node_i][node_j];
//    }

//    public float calculateCostsForMerging(
//            int node_i, int node_j) {
//      float costsForMerging = 0;
//        for (int i = 0; i < node_i; i++) {
//            if (!edge[i][node_i]) {
//                if (edge[i][node_j]) {
//                    if (-edgeCosts[i][node_i] > edgeCosts[i][node_j]) {
//                        costsForMerging += edgeCosts[i][node_j];
//                    } else {
//                        costsForMerging -= edgeCosts[i][node_i];
//                    }
//                }
//            } else {
//                if (!edge[i][node_j]) {
//                    if (edgeCosts[i][node_i] > -edgeCosts[i][node_j]) {
//                        costsForMerging -= edgeCosts[i][node_j];
//                    } else {
//                        costsForMerging += edgeCosts[i][node_i];
//                    }
//                }
//            }
//        }
//        for (int i = node_i + 1; i < node_j; i++) {
//            if (!edge[i][node_i]) {
//                if (edge[i][node_j]) {
//                    if (-edgeCosts[i][node_i] > edgeCosts[i][node_j]) {
//                        costsForMerging += edgeCosts[i][node_j];
//                    } else {
//                        costsForMerging -= edgeCosts[i][node_i];
//                    }
//                }
//            } else {
//                if (!edge[i][node_j]) {
//                    if (edgeCosts[i][node_i] > -edgeCosts[i][node_j]) {
//                        costsForMerging -= edgeCosts[i][node_j];
//                    } else {
//                        costsForMerging += edgeCosts[i][node_i];
//                    }
//                }
//            }
//        }
//        for (int i = node_j; i < size; i++) {
//            if (!edge[i][node_i]) {
//                if (edge[i][node_j]) {
//                    if (-edgeCosts[i][node_i] > edgeCosts[i][node_j]) {
//                        costsForMerging += edgeCosts[i][node_j];
//                    } else {
//                        costsForMerging -= edgeCosts[i][node_i];
//                    }
//                }
//            } else {
//                if (!edge[i][node_j]) {
//                    if (edgeCosts[i][node_i] > -edgeCosts[i][node_j]) {
//                        costsForMerging -= edgeCosts[i][node_j];
//                    } else {
//                        costsForMerging += edgeCosts[i][node_i];
//                    }
//                }
//            }
//        }
//        return costsForMerging;
//    }

    public float calculateCostsForSetForbidden(
            int node_i, int node_j) {
        float costsForSetForbidden = 0;
        for (int i = 0; i < size; i++) {
            if (edgeCosts[node_i][i] > 0 && edgeCosts[node_j][i] > 0) {
                costsForSetForbidden += Math.min(edgeCosts[node_i][i], edgeCosts[node_j][i]);
            }
        }

        costsForSetForbidden += edgeCosts[node_i][node_j];
        return costsForSetForbidden;
    }
}

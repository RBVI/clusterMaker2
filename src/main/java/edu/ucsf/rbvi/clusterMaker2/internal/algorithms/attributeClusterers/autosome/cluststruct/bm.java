/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.cluststruct;

/**
 * benchmarking data
 * @author Aaron
 */
public class bm{
        private double Fmeasure;
        private double Precision;
        private double Recall;
        private double NMI;
        private String settings;
        private String file = new String();
        
        public bm(double Fmeasure, double Prec, double Rec, double NMI,String settings, String file){
            this.Fmeasure = Fmeasure;
            Precision = Prec;
            Recall = Rec;
            this.NMI = NMI;
            this.file = file;
            this.settings = settings;
        }
        public double getF() {return Fmeasure;}
        public double getP() {return Precision;}
        public double getR() {return Recall;}
        public double getNMI() {return NMI;}
        public String getSettings() {return settings;}
        public String getFileName() {return file;}
    }
        

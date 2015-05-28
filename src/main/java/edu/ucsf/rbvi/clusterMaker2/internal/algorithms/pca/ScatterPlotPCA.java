/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

/**
 *
 * @author root
 */
@SuppressWarnings("serial")
public class ScatterPlotPCA extends JPanel {
   private static int MAX_SCORE = 2;
   private static int MIN_SCORE = -2;
   private static final int PREF_W = 600;
   private static final int PREF_H = 600;
   private static final int BORDER_GAP = 30;
   private static final Color GRAPH_COLOR = Color.green;
   private static final Color GRAPH_POINT_COLOR = new Color(150, 50, 50, 180);
   private static final Stroke GRAPH_STROKE = new BasicStroke(3f);
   private static final int GRAPH_POINT_WIDTH = 12;
   private static final int Y_HATCH_CNT = 10;
   private double[][] scoresX;
   private double[][] scoresY;
   
   public ScatterPlotPCA(double[][] scoresX, double[][] scoresY){
       this.scoresX = scoresX;
       this.scoresY = scoresY;
       
       double max = getMax(scoresX);
       double min = getMin(scoresX);
       if(max > MAX_SCORE || min < MIN_SCORE){
           if(max > Math.abs(min)){
               MAX_SCORE = (int) max;
               MIN_SCORE = (int) ((int) -1 * max);
           }else{
               MAX_SCORE = (int) Math.abs(min);
               MIN_SCORE = (int) ((int) -1 * Math.abs(min));
           }
       }
   }
   
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      double xScale = ((double) getWidth() - 2 * BORDER_GAP) / (MAX_SCORE - MIN_SCORE);
      double yScale = ((double) getHeight() - 2 * BORDER_GAP) / (MAX_SCORE - MIN_SCORE);

      List<Point> graphPoints = new ArrayList<Point>();
      for(int i=0; i<scoresX.length;i++){
          for(int j=0;j<scoresX[0].length;j++){
              int x1 = (int) (scoresX[i][j] * xScale + BORDER_GAP);
              int y1 = (int) ((scoresY[i][j]) * yScale + BORDER_GAP);
              graphPoints.add(new Point(x1, y1));
          }
      }

      // create x and y axes
      g2.drawLine(BORDER_GAP, getHeight()/2, getWidth() - BORDER_GAP, getHeight()/2);
      g2.drawLine(getWidth()/2, BORDER_GAP, getWidth()/2, getHeight() - BORDER_GAP);
      
      // create hatch marks for y axis. 
      for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i++) {
         int x0 = getWidth()/2;
         int x1 = GRAPH_POINT_WIDTH + getWidth()/2;
         int y0 = (int) (BORDER_GAP + i * yScale);
         int y1 = y0;
         g2.drawLine(x0, y0, x1, y1);
         
         String number = "" + ( MAX_SCORE - i);
         int xl = -1 * GRAPH_POINT_WIDTH + getWidth()/2;
         int yl = (int) (BORDER_GAP + i * yScale);
         g2.drawString(number, xl, yl);
      }

      // and for x axis
      for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i++) {
         int x0 = (int) (BORDER_GAP + i * xScale);
         int x1 = x0;
         int y0 = getHeight()/2;
         int y1 = y0 + GRAPH_POINT_WIDTH;
         g2.drawLine(x0, y0, x1, y1);
         
         String number = "" + -1 * ( MAX_SCORE - i);
         if(!number.equals("0"))
            g2.drawString(number, x1 - GRAPH_POINT_WIDTH/2, y1 + GRAPH_POINT_WIDTH);
      }

      Stroke oldStroke = g2.getStroke();
      g2.setColor(GRAPH_COLOR);
      g2.setStroke(GRAPH_STROKE);
//      for (int i = 0; i < graphPoints.size() - 1; i++) {
//         int x1 = graphPoints.get(i).x;
//         int y1 = graphPoints.get(i).y;
//         int x2 = graphPoints.get(i + 1).x;
//         int y2 = graphPoints.get(i + 1).y;
//         g2.drawLine(x1, y1, x2, y2);         
//      }

      g2.setStroke(oldStroke);      
      g2.setColor(GRAPH_POINT_COLOR);
      for (int i = 0; i < graphPoints.size(); i++) {
         int x = graphPoints.get(i).x - GRAPH_POINT_WIDTH / 2;
         int y = graphPoints.get(i).y - GRAPH_POINT_WIDTH / 2;;
         int ovalW = GRAPH_POINT_WIDTH;
         int ovalH = GRAPH_POINT_WIDTH;
         g2.fillOval(x, y, ovalW, ovalH);
      }
   }
   
   @Override
   public Dimension getPreferredSize() {
      return new Dimension(PREF_W, PREF_H);
   }
   
   public static void createAndShowGui(double[][] scoresX, double[][] scoresY) {
      
      ScatterPlotPCA mainPanel = new ScatterPlotPCA(scoresX, scoresY);

      JFrame frame = new JFrame("DrawGraph");
      frame.getContentPane().add(mainPanel);
      frame.pack();
      frame.setLocationByPlatform(true);
      frame.setVisible(true);
   }

   
   protected double getMax(double[][] mat){
       int row = mat.length;
       int col = mat[0].length;
       double max = Double.NEGATIVE_INFINITY;
       for(int i=0;i<row;i++){
           for(int j=0;j<col;j++){
               if(max < mat[i][j])
                   max = mat[i][j];
           }
       }
       
       return Math.ceil(max);
   }
   
   protected double getMin(double[][] mat){
       int row = mat.length;
       int col = mat[0].length;
       double min = Double.POSITIVE_INFINITY;
       for(int i=0;i<row;i++){
           for(int j=0;j<col;j++){
               if(min > mat[i][j])
                   min = mat[i][j];
           }
       }
       
       return Math.floor(min);
   }
}

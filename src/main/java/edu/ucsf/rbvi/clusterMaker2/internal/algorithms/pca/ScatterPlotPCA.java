/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author root
 */
@SuppressWarnings("serial")
public class ScatterPlotPCA extends JPanel {
    private static int MAX_SCORE = 1;
    private static int MIN_SCORE = -1;
    private static final int PREF_W = 500;
    private static final int PREF_H = 500;
    private static final int BORDER_GAP = 30;
    private static final Color GRAPH_COLOR = Color.green;
    private static final Color GRAPH_POINT_COLOR = new Color(150, 50, 50, 180);
    private static final Stroke GRAPH_STROKE = new BasicStroke(3f);
    private static final int GRAPH_POINT_WIDTH = 6;
    private static final int GRAPH_HATCH_WIDTH = 10;
    private final double[][] scoresX;
    private final double[][] scoresY;
    private final String lableX;
    private final String lableY;
    private static ComputationMatrix[] allComponents;
    private static String[] PCs;
    
    private static final JPanel container = new JPanel();
    private static final JLabel labelXAxis = new JLabel("X - Axis: ");
    private static final JLabel labelYAxis = new JLabel("Y - Axis: ");
    private static JComboBox<String> xAxis;
    private static JComboBox<String> yAxis;
    private static final JButton plotButton = new JButton("Plot");
   
   public ScatterPlotPCA(double[][] scoresX, double[][] scoresY, String lableX, String lableY){
       this.scoresX = scoresX;
       this.scoresY = scoresY;
       this.lableX = lableX;
       this.lableY = lableY;
       
       double max = ComputationMatrix.getMax(scoresX);
       double min = ComputationMatrix.getMin(scoresX);
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

      // create x and y axes
      g2.drawLine(BORDER_GAP, getHeight()/2, getWidth() - BORDER_GAP, getHeight()/2);
      g2.drawLine(getWidth()/2, BORDER_GAP, getWidth()/2, getHeight() - BORDER_GAP);
      
      // create hatch marks for y axis. 
      for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i++) {
         int x0 = getWidth()/2;
         int x1 = GRAPH_HATCH_WIDTH + getWidth()/2;
         int y0 = (int) (BORDER_GAP + i * yScale);
         int y1 = y0;
         g2.drawLine(x0, y0, x1, y1);
         
         String number = "" + ( MAX_SCORE - i);
         g2.drawString(number, x1 - 2*GRAPH_HATCH_WIDTH, y1 + GRAPH_HATCH_WIDTH/2);
      }
      g2.setFont(new Font("default", Font.BOLD, g2.getFont().getSize()));
      g2.drawString(lableY, getWidth()/2 - (lableY.length()/2)*5, getHeight() - BORDER_GAP/2);
      g2.setFont(new Font("default", Font.PLAIN, g2.getFont().getSize()));

      // and for x axis
      for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i++) {
         int x0 = (int) (BORDER_GAP + i * xScale);
         int x1 = x0;
         int y0 = getHeight()/2;
         int y1 = y0 + GRAPH_HATCH_WIDTH;
         g2.drawLine(x0, y0, x1, y1);
         
         String number = "" + -1 * ( MAX_SCORE - i);
         if(!number.equals("0"))
            g2.drawString(number, x1, y1 - 2*GRAPH_HATCH_WIDTH);
      }
      g2.setFont(new Font("default", Font.BOLD, g2.getFont().getSize()));
      g2.drawString(lableX, getWidth() - BORDER_GAP - (lableX.length()/2)*5, getHeight()/2 + BORDER_GAP);
      g2.setFont(new Font("default", Font.PLAIN, g2.getFont().getSize()));
      
      int newX = getWidth()/2;
      int newY = getHeight()/2;
      
      List<Point> graphPoints = new ArrayList<Point>();
      for(int i=0; i<scoresX.length;i++){
          for(int j=0;j<scoresX[0].length;j++){
              System.out.println("original " + scoresX[i][j] + " " + scoresY[i][j]);
              int x1 = (int) (scoresX[i][j] * xScale + newX);
              int y1 = (int) ((int) -1 * (scoresY[i][j] * yScale - newY));
              System.out.println("final " + x1 + " " + y1);
              graphPoints.add(new Point(x1, y1));
          }
      }

      Stroke oldStroke = g2.getStroke();
      g2.setColor(GRAPH_COLOR);
      g2.setStroke(GRAPH_STROKE);

      g2.setStroke(oldStroke);      
      g2.setColor(GRAPH_POINT_COLOR);
       for (Point graphPoint : graphPoints) {
           int x = graphPoint.x - GRAPH_POINT_WIDTH / 2;
           int y = graphPoint.y - GRAPH_POINT_WIDTH / 2;
           int ovalW = GRAPH_POINT_WIDTH;
           int ovalH = GRAPH_POINT_WIDTH;
           g2.fillOval(x, y, ovalW, ovalH);
       }
   }
   
   @Override
   public Dimension getPreferredSize() {
      return new Dimension(PREF_W, PREF_H);
   }
   
   public static JPanel createControlJPanel(ComputationMatrix[] components){
        allComponents = components;
        JPanel control = new JPanel(new GridBagLayout());
        
        PCs = new String[components.length];
        for(int i=0;i<PCs.length;i++)
            PCs[i] = "PC " + (i+1);
        
        xAxis = new JComboBox(PCs);
        yAxis = new JComboBox(PCs);
        yAxis.setSelectedIndex(1);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);
        
        // add components to the panel
        constraints.gridx = 0;
        constraints.gridy = 0;     
        control.add(labelXAxis, constraints);
 
        constraints.gridx = 1;
        control.add(xAxis, constraints);
         
        constraints.gridx = 0;
        constraints.gridy = 1;     
        control.add(labelYAxis, constraints);
         
        constraints.gridx = 1;
        control.add(yAxis, constraints);
         
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        control.add(plotButton, constraints);
        
        plotButton.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                container.remove(0);
                ScatterPlotPCA scatterPlot = new ScatterPlotPCA(allComponents[xAxis.getSelectedIndex()].toArray(), 
                        allComponents[yAxis.getSelectedIndex()].toArray(), PCs[xAxis.getSelectedIndex()], PCs[yAxis.getSelectedIndex()]);
                container.add(scatterPlot, 0);
                container.updateUI();
            }

        }); 
         
        // set border for the panel
        control.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), ""));
        
        return control;
   }
   
   public static void createAndShowGui(ComputationMatrix[] components) {
      
        ScatterPlotPCA scatterPlot = new ScatterPlotPCA(components[0].toArray(), components[1].toArray(), "PC 1", "PC 2");

        JFrame frame = new JFrame("Scatter Plot");
        
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(scatterPlot);
        container.add(createControlJPanel(components));
        
        frame.getContentPane().add(container);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
   }
}
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
import org.jdesktop.swingx.JXCollapsiblePane;

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
    private static final int GRAPH_HATCH_WIDTH = 10;
    private final double[][] scoresX;
    private final double[][] scoresY;
    private final String lableX;
    private final String lableY;
    private static ComputationMatrix[] allComponents;
    private static double[] variances;
    private static String[] PCs;
    private static final Color[] colors = {Color.black, Color.blue, Color.cyan, Color.darkGray, Color.gray, Color.green,
            Color.yellow, Color.lightGray, Color.magenta, Color.orange, Color.pink, Color.red, Color.white };
    private static final String[] colorNames = { "Black", "Blue", "Cyan", "Dark Gray", "Gray", "Green", "Yellow", 
            "Light Gray", "Magneta", "Orange", "Pink", "Red", "White" };
    
    private static final JPanel container = new JPanel();
    private static final JPanel panelXAxis = new JPanel();
    private static final JPanel panelYAxis = new JPanel();
    private static final JPanel panelButtons = new JPanel();
    private static final JLabel labelXAxis = new JLabel("X - Axis: ");
    private static final JLabel labelYAxis = new JLabel("Y - Axis: ");
    private static final JLabel labelColor = new JLabel("Color of points: ");
    private static final JLabel labelPointSize = new JLabel("Size of points: ");
    private static final JTextField textFieldPointSize = new JTextField(6);
    private static final JXCollapsiblePane collapsiblePaneOptions = new JXCollapsiblePane();
    private static JLabel labelXVariance;
    private static JLabel labelYVariance;
    private static JComboBox<String> comboXAxis;
    private static JComboBox<String> comboYAxis;
    private static JComboBox<String> comboColors;
    private static final JButton buttonPlot = new JButton("Plot");
    private static final JButton buttonOptions = new JButton("Advance Options");
   
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
              int x1 = (int) (scoresX[i][j] * xScale + newX);
              int y1 = (int) ((int) -1 * (scoresY[i][j] * yScale - newY));
              graphPoints.add(new Point(x1, y1));
          }
      }
      g2.setColor(colors[comboColors.getSelectedIndex()]);
      int graph_point_width = Integer.parseInt(textFieldPointSize.getText());
       for (Point graphPoint : graphPoints) {
           int x = graphPoint.x - graph_point_width / 2;
           int y = graphPoint.y - graph_point_width / 2;
           int ovalW = graph_point_width;
           int ovalH = graph_point_width;
           g2.fillOval(x, y, ovalW, ovalH);
       }
   }
   
   @Override
   public Dimension getPreferredSize() {
      return new Dimension(PREF_W, PREF_H);
   }
   
   public static JXCollapsiblePane createAdvanceOptionPane(){
        JPanel control = new JPanel();
              
        comboColors = new JComboBox(colorNames);
        comboColors.setSelectedIndex(1);
       
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);
        
        // add components to the panel
        constraints.gridx = 0;
        constraints.gridy = 0;     
        control.add(labelPointSize, constraints);
 
        constraints.gridx = 1;
        control.add(textFieldPointSize, constraints);
         
        constraints.gridx = 0;
        constraints.gridy = 1;     
        control.add(labelColor, constraints);
         
        constraints.gridx = 1;
        control.add(comboColors, constraints);
        
        // set border for the panel
        control.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Advanced Options"));
        
        collapsiblePaneOptions.removeAll();
        collapsiblePaneOptions.add("Center", control);
        collapsiblePaneOptions.setCollapsed(!collapsiblePaneOptions.isCollapsed());
        
       return collapsiblePaneOptions;
   }
   
   public static JPanel createControlJPanel(ComputationMatrix[] components){
        allComponents = components;
        JPanel control = new JPanel(new GridBagLayout());
        
        PCs = new String[components.length];
        for(int i=0;i<PCs.length;i++)
            PCs[i] = "PC " + (i+1);
        
        comboXAxis = new JComboBox(PCs);
        comboYAxis = new JComboBox(PCs);
        comboYAxis.setSelectedIndex(1);
        textFieldPointSize.setText("6");
        labelXVariance = new JLabel(String.valueOf(variances[0]) + "% variance");
        labelYVariance = new JLabel(String.valueOf(variances[1]) + "% variance");
                
        panelXAxis.setLayout(new BoxLayout(panelXAxis, BoxLayout.X_AXIS));
        panelXAxis.removeAll();
        panelXAxis.add(comboXAxis);
        panelXAxis.add(Box.createRigidArea(new Dimension(5,0)));
        panelXAxis.add(labelXVariance);    
        
        panelYAxis.setLayout(new BoxLayout(panelYAxis, BoxLayout.X_AXIS));
        panelYAxis.removeAll();
        panelYAxis.add(comboYAxis);
        panelYAxis.add(Box.createRigidArea(new Dimension(5,0)));
        panelYAxis.add(labelYVariance);
        
        if(buttonOptions.getActionListeners().length == 0){
            buttonOptions.addActionListener(collapsiblePaneOptions.getActionMap().get("toggle"));   
        }
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
        panelButtons.add(buttonOptions);
        panelButtons.add(buttonPlot);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);
        
        // add components to the panel
        constraints.gridx = 0;
        constraints.gridy = 0;     
        control.add(labelXAxis, constraints);
 
        constraints.gridx = 1;
        control.add(panelXAxis, constraints);
         
        constraints.gridx = 0;
        constraints.gridy = 1;     
        control.add(labelYAxis, constraints);
         
        constraints.gridx = 1;
        control.add(panelYAxis, constraints);
         
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        control.add(createAdvanceOptionPane(), constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        control.add(panelButtons, constraints);
        
        comboXAxis.addActionListener (new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                labelXVariance.setText(variances[comboXAxis.getSelectedIndex()] + "% variance");
            }
        });
        
        comboYAxis.addActionListener (new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                labelYVariance.setText(variances[comboYAxis.getSelectedIndex()] + "% variance");
            }
        });
        
        buttonPlot.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                try{
                    Integer.parseInt(textFieldPointSize.getText());
                }catch (NumberFormatException er) {
                      JOptionPane.showMessageDialog(null,textFieldPointSize.getText() + " is not a number","Error: Size of point",JOptionPane.ERROR_MESSAGE);
                      return;
                }                
                //Execute when button is pressed
                container.remove(0);
                ScatterPlotPCA scatterPlot = new ScatterPlotPCA(allComponents[comboXAxis.getSelectedIndex()].toArray(), 
                        allComponents[comboYAxis.getSelectedIndex()].toArray(), PCs[comboXAxis.getSelectedIndex()], PCs[comboYAxis.getSelectedIndex()]);
                container.add(scatterPlot, 0);
                container.updateUI();
            }

        }); 
         
        // set border for the panel
        control.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), ""));
        
        return control;
   }
   
   public static void createAndShowGui(ComputationMatrix[] components, double[] varianceArray) {
       
       if(components == null){
           return;
       }else if(components.length < 2){
           return;
       }
        variances = varianceArray;
      
        ScatterPlotPCA scatterPlot = new ScatterPlotPCA(components[0].toArray(), components[1].toArray(), "PC 1", "PC 2");

        JFrame frame = new JFrame("Scatter Plot");
        
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.removeAll();
        container.add(scatterPlot);
        container.add(createControlJPanel(components));
        
        frame.getContentPane().add(container);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
   }
}
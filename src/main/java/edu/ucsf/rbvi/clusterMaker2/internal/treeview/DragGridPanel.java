/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: DragGridPanel.java,v $
 * $Revision: 1.11 $
 * $Date: 2006/08/18 06:50:17 $
 * $Name:  $
 *
 * This file is part of Java TreeView
 * Copyright (C) 2001-2003 Alok Saldanha, All Rights Reserved. Modified by Alex Segal 2004/08/13. Modifications Copyright (C) Lawrence Berkeley Lab.
 *
 * This software is provided under the GNU GPL Version 2. In particular, 
 *
 * 1) If you modify a source file, make a comment in it containing your name and the date.
 * 2) If you distribute a modified version, you must do it under the GPL 2.
 * 3) Developers are encouraged but not required to notify the Java TreeView maintainers at alok@genome.stanford.edu when they make a useful addition. It would be nice if significant contributions could be merged into the main distribution.
 *
 * A full copy of the license can be found in gpl.txt or online at
 * http://www.gnu.org/licenses/gpl.txt
 *
 * END_HEADER 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview;


import java.awt.*;
import java.awt.event.*;

import javax.swing.JComponent;
import javax.swing.JPanel;

/** A Panel which is divided into a rectangular grid which the user can change.
 * Components may be added to the grid, and may take up any rectangular
 * set of cells within the grid. The user may drag the borders between 
 * the components to change the relative sizes of the cells, and thus
 * the sizes of the components.
 */
public class DragGridPanel extends JPanel implements MouseListener, 
    MouseMotionListener, ComponentListener,FocusListener, ContainerListener {
    //****************************
    // Constructors
    //****************************
    /** Constructor.
     * @param x the number of columns.
     * @param y the number of rows.
     */
	public DragGridPanel(int x, int y) {
	    super((LayoutManager) null);
	    setBackground(Color.white);

	    components = new Component[x][y];
	    xpos = new int[x + 1];
	    ypos = new int[y + 1];
	    xsizes = new float[x];
	    ysizes = new float[y];
	    // Space everything evenly
	    for(int i = 0; i < x; i++)
		xsizes[i] = 1.0f / x;
	    for(int i = 0; i < y; i++)
		ysizes[i] = 1.0f / y;
	    addMouseListener(this);
	    addMouseMotionListener(this);
	    addComponentListener(this);
	}

    //****************************
    // JavaBean Property getters/setters
    //****************************

    /** Adjustable - true if use is allowed to drag borders to change cell sizes. */
	    public boolean isAdjustable() {
		return adjustable;
	    }

    /** Adjustable - true if use is allowed to drag borders to change cell sizes. */
    public void setAdjustable(boolean on) {
	adjustable = on;
    }

    /** BorderWidth - width of the draggable borders between Components. */
    public int getBorderWidth() {
	return bwidth;
    }

    /** BorderWidth - width of the draggable borders between Components. */
    public void setBorderWidth(int width) {
	bwidth = width;
    }

    /** BorderHeight - height of the draggable borders between Components. */
    public int getBorderHeight() {
	return bheight;
    }

    /** BorderHeight - height of the draggable borders between Components. */
    public void setBorderHeight(int height) {
	bheight = height;
    }

    /** MinimumWidth - minimum width of a component. */
    public int getMinimumWidth() {
	return minwidth;
    }

    /** MinimumWidth - minimum width of a component. */
    public void setMinimumWidth(int width) {
	minwidth = width;
    }

    /** MinimumHeight - minimum height of a component. */
    public int getMinimumHeight() {
	return minheight;
    }

    /** MinimumHeight - minimum height of a component. */
    public void setMinimumHeight(int height) {
	minheight = height;
    }

    /** FocusWidth - width of the focus rectangle round each control. */
    public int getFocusWidth() {
	return focuswidth;
    }

    /** FocusWidth - width of the focus rectangle round each control. */
    public void setFocusWidth(int width) {
	focuswidth = width;
    }

    /** FocusHeight - height of the focus rectangle round each control. */
    public int getFocusHeight() {
	return focusheight;
    }

    /** FocusHeight - height of the focus rectangle round each control. */
    public void setFocusHeight(int height) {
	focusheight = height;
    }


    /**
     * Set the proportional widths of each column.
     * @param widths the widths.
     * @exception ArrayIndexOutOfBoundsException if not enough widths are supplied.
     */
    public void setWidths(float widths[]) {
	float max = 0;
	int i;

	// Copy provided array into class variable
	// This will give the documented exception if the parameter is too small
	System.arraycopy(widths, 0, xsizes, 0, xsizes.length);
	// Calculate total of all passed sizes
	for(i = 0; i < xsizes.length; i++)
	    max += xsizes[i];
	if(max == 0)		// Silly value - space evenly instead
	    for(i = 0; i < xsizes.length; i++)
		xsizes[i] = 1.0f / xsizes.length;
	else if(max != 1)	// Normalise so they add up to 1
	    for(i = 0; i < xsizes.length; i++)
		xsizes[i] /= max;
	// Resize all the cells accordingly
	doLayout();
	repaint();
    }

    /**
     * Get the proportional widths of each column.
     * @return a float array containing the proportions (adds up to 1).
     */
    public float[] getWidths() {
	float [] rwidths = new float[xsizes.length];

	System.arraycopy(xsizes, 0, rwidths, 0, xsizes.length);
	return rwidths;
    }

    /**
     * Set the proportional heights of each row.
     * @param heights array indicating proportional heights.
     * @exception ArrayIndexOutOfBoundsException if not enough heights are supplied.
     */
    public void setHeights(float heights[]) {
	float max = 0;
	int i;

	// Copy provided array into class variable
	// This will give the documented exception if the parameter is too small
	System.arraycopy(heights, 0, ysizes, 0, ysizes.length);
	// Calculate total of all passed sizes
	for(i = 0; i < ysizes.length; i++)
	    max += ysizes[i];
	if(max == 0)		// Silly value - space evenly instead
	    for(i = 0; i < ysizes.length; i++)
		ysizes[i] = 1.0f / ysizes.length;
	else if(max != 1)	// Normalise so they add up to 1
	    for(i = 0; i < ysizes.length; i++)
		ysizes[i] /= max;
	// Resize all the cells accordingly
	doLayout();
	repaint();
    }

    /**
     * Get the proportional heights of each row.
     * @return a float array containing the proportions (adds up to 1).
     */
    public float[] getHeights() {
	float [] rheights = new float[ysizes.length];

	System.arraycopy(ysizes, 0, rheights, 0, ysizes.length);
	return rheights;
    }

    //****************************
    // Other public methods
    //****************************

	/**
	 *  Adds a component to the DragGridPanel
	 *
	 * @param  component  The component to be added
	 * @param  rectangle  The location to add it in
	 */
	public void addComponent(Component component, Rectangle rectangle) {
		if (component != null) {
			addComponent(component, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
		}
	}

    /** Add a component to occupy a rectangle of cells.
     * @param x Top left cell.
     * @param y Bottom right cell.
     * @param width Number of cells across.
     * @param height Number of cells down.
     * @exception ArrayIndexOutofBoundsException
     *	if any of the parameters extend outside the array.
     * @exception IllegalArgumentException
     *	if any of the space is already occupied.
     */
	public void addComponent(Component comp, int x, int y, int width, int height) {
	    int i, j;

	    // See if cells required are all free
	    for(i = x; i < x + width; i++) {
		for(j = y; j < y + height; j++) {
		    if(components[i][j] != null)
			throw new IllegalArgumentException("Cells already occupied");
		}
	    }
	    // Add component to all cells
	    for(i = x; i < x + width; i++) {
		for(j = y; j < y + height; j++) {
		    components[i][j] = comp;
		}
	    }
	    // Add component to parent Container
	    add(comp);
	    // add self as focus listener, for focus display...
	    addFocusListenerRecursively(comp);
	    repaint();
	}

    /** Remove a component. Other components will use up the 
     * vacated cell(s) if possible.
     */
    public void removeComponent(Component comp) {
	int x, y;

	for(y = 0; y < ysizes.length; y++) {
	    for(x = 0; x < ysizes.length; x++) {
		if(components[x][y] == comp) {
		    components[x][y] = null;
		}
	    }
	}
	removeFocusListenerRecursively(comp);
	remove(comp);
	repaint();
    }
    
    /** added by alok 10/9/2001 */
    public Dimension getPreferredSize() {
	int maxw = 0;	
	for(int y = 0; y < ysizes.length; y++) {
	    int roww = components[0][y].getPreferredSize().width;
	    for(int x = 1; x < xsizes.length; x++) {
		// skip if same as one on left
		if(components[x][y] == components[x-1][y]) continue;
		if (components[x][y] != null) 
		    roww += components[x][y].getPreferredSize().width;
	    }
	    if (roww > maxw)
		maxw = roww;
	}

	int maxh = 0;	
	for(int x = 0; x < xsizes.length; x++) {
	    int colh = components[x][0].getPreferredSize().height;
	    for(int y = 1; y < ysizes.length; y++) {
		// skip if same as one above
		if(components[x][y] == components[x][y-1]) continue;
		if (components[x][y] != null) 
		    colh += components[x][y].getPreferredSize().height;
	    }
	    if (colh > maxh)
		maxh = colh;
	}
	return new Dimension(maxw, maxh);
    }

    /** added by alok 9/12/2001 */
    private void addFocusListenerRecursively(Component c) {
	//Add Focus Listener to the Component passed as an argument
	c.addFocusListener(this);	
	//Check if the Component is a Container
	if(c instanceof Container) {	    
	    //Component c is a Container. The following cast is safe.
	    Container cont = (Container)c;	    
	    //Add ContainerListener to the Container.
	    cont.addContainerListener(this);	    
	    //Get the Container's array of children Components.
	    Component[] children = cont.getComponents();	    
	    //For every child repeat the above operation.
	    for(int i = 0; i < children.length; i++){
		addFocusListenerRecursively(children[i]);
	    }
	}
    }

    /** added by alok 9/12/2001 */
    private void removeFocusListenerRecursively(Component c) {
	//Add Focus Listener to the Component passed as an argument
	c.removeFocusListener(this);	
	//Check if the Component is a Container
	if(c instanceof Container) {	    
	    //Component c is a Container. The following cast is safe.
	    Container cont = (Container)c;	    
	    //Add ContainerListener to the Container.
	    cont.removeContainerListener(this);	    
	    //Get the Container's array of children Components.
	    Component[] children = cont.getComponents();	    
	    //For every child repeat the above operation.
	    for(int i = 0; i < children.length; i++){
		removeFocusListenerRecursively(children[i]);
	    }
	}
    }
    /** added by alok 9/12/2001 */
     public void componentAdded(ContainerEvent e)
    {
	addFocusListenerRecursively(e.getChild());
    }
    /** added by alok 9/12/2001 */
    public void componentRemoved(ContainerEvent e)
    {
	removeFocusListenerRecursively(e.getChild());
    }

    /** Recompute cell sizes from proportional sizes, and resize components. */
    public void doLayout() {
	int x, y;
	Dimension s = getSize();

	// Do columns - start at left edge
	xpos[0] = 0;
	for(x = 0; x < xsizes.length; x++) {
	    xpos[x + 1] = xpos[x] + (int)(s.width * xsizes[x]);
	}
	// Fudge right edge in case of rounding errors
	xpos[xsizes.length] = s.width;
	// Do rows - start at top
	ypos[0] = 0;
	for(y = 0; y < ysizes.length; y++) {
	    ypos[y + 1] = ypos[y] + (int)(s.height * ysizes[y]);
	}
	// Fudge bottom edge in case of rounding errors
	ypos[ysizes.length] = s.height;
	resizeComponents();
    }

    //****************************
    // Overridden public methods
    //****************************

    /** Standard paint routine. */
    public void paintComponent(Graphics g) {
		
//		System.out.println("entering DragGridPanel paint " +getName() + " clip " + clip);
		Dimension newsize = getSize();
		
	g.setColor(Color.white);
	g.fillRect(0,0,newsize.width, newsize.height);
	g.setColor(Color.black);

	int x, y;
	
	// Do horizontal lines above each component not in top row
	for(y = 1; y < ysizes.length; y++) {
	    for(x = 0; x < xsizes.length; x++) {
		Component c = components[x][y];
		
		if(components[x][y - 1] == c)
		    continue;		// Same component above
		
		// Draw bar above
		g.fillRect(xpos[x], ypos[y] - bheight, xpos[x + 1] - xpos[x], bheight);
	    }
	}
	// Do vertical lines to left of each component not in left column
	for(x = 1; x < xsizes.length; x++) {
	    for(y = 0; y < ysizes.length; y++) {
		Component c = components[x][y];
		
		if(components[x - 1][y] == c)
		    continue;		// Same component to left
		
		// Draw bar to left
		g.fillRect(xpos[x] - bwidth, ypos[y], bwidth, ypos[y + 1] - ypos[y]);
	    }
	}
//			System.out.println("exiting DragGridPanel paint");

    }
    
    /** draws focus rectangle, added by alok 9/12/2001*/
    public void focusGained(FocusEvent e) {
	// draw the focus rectangle
	for(int x = 0; x < xsizes.length; x++) {
	    for(int y = 0; y < ysizes.length; y++) {
		Component c = components[x][y];
		if (c == e.getComponent()) {
		    drawFocus(getGraphics(), x, y);
		    return;
		}

		if(c instanceof Container) {	    
		    //Component c is a Container. The following cast is safe.
		    Container cont = (Container)c;	    
		    if (cont.isAncestorOf(e.getComponent())) {
			drawFocus(getGraphics(), x, y);
			return;
		    }
		}
	    }
	}
    }

    public void focusLost(FocusEvent e) {
	// undraw the focus rectangle
	for(int x = 0; x < xsizes.length; x++) {
	    for(int y = 0; y < ysizes.length; y++) {
		Component c = components[x][y];
		if (c == e.getComponent()) {
		    drawFocus(getGraphics(), x, y);
		    return;
		}

		if(c instanceof Container) {	    
		    //Component c is a Container. The following cast is safe.
		    Container cont = (Container)c;	    
		    if (cont.isAncestorOf(e.getComponent())) {
			Graphics g = getGraphics();
			if (g != null) { 
			    // sometimes this is called after the 
			    // graphics context is gone.
			    Color o = g.getColor();
			    g.setColor(cont.getBackground());
			    drawFocus(g, x, y);
			    g.setColor(o);
			}
			return;
		    }
		}
	    }
	}
    }
    private void drawFocus(Graphics g, int x, int y) {
			if (g == null) return;
			Component c = components[x][y];
	
	if(c == null)
	    return;		// No component here!
	if(x > 0 && components[x - 1][y] == c)
	    return;		// Already processed in last column
	if(y > 0 && components[x][y - 1] == c)
	    return;		// Already processed in last row

	Dimension size = c.getSize();
	int w = size.width;
	int h = size.height;
	
	// need to draw four rectangles
	g.fillRect // left side
	    (xpos[x],ypos[y], 
	     focuswidth, h + 2*focusheight);

	g.fillRect // right side
	    (xpos[x] + w + focuswidth, ypos[y],
	     focuswidth, h + 2*focusheight);
	g.fillRect // top
	    (xpos[x] + focuswidth, ypos[y], 
	     w, focusheight);
	g.fillRect // bottom
	    (xpos[x] + focuswidth, ypos[y] + h + focuswidth, 
	     w, focusheight);
    }

    /** Standard toString returns "DragGridPanel[x][y]" and Panel info. */
    public String toString() {
	Point p = getLocation();
	return "DragGridPanel(" + p.x + "," + p.y + ")[" + xsizes.length + "][" + ysizes.length + "] " + super.toString();
    }

    //****************************
    // Event handling
    //****************************

    /** Change the mouse cursor if over a draggable border. */
	public void mouseMoved(MouseEvent e) {
	    if(!dragging()) {
		onDragBorder(e.getX(), e.getY());
	    }
	}

    /** Select a border to drag. */
    public void mousePressed(MouseEvent e) {
	if (adjustable && onDragBorder(e.getX(), e.getY())) {
	    // Save dragging borders
	    coldrag = curscol;
	    rowdrag = cursrow;
	    setCursor(handCursor);
	    dragBar = new DragBar();
	    dragBar.setBounds(0,0, getWidth(), getHeight());
	    dragBar.setMouse(e.getX(), e.getY());
	    //	    add(dragBar, 0);
	}
    }

    /** Stop dragging a border. */
    public void mouseReleased(MouseEvent e) {
	if(dragging()) {
	    // Reset dragging borders
	    coldrag = rowdrag = 0;
	    // Set mouse cursor to correct shape
	    onDragBorder(e.getX(), e.getY());
	    remove(dragBar);
	}
	if(resize) {
	    resizeComponents();
	    repaint();
	}
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
	if(currentCursor != 0 && !dragging())
	    setCursor(0);
    }
	
    boolean resize = false;
    /** If a border is selected, change it's position. */
    public void mouseDragged(MouseEvent e) {
	if (dragging()) {
	    int x = e.getX(), y = e.getY();
	    resize = false;

	    if(coldrag > 0) {		// Dragging a column
		resize = true;
		// Ensure column is at least minwidth wide
		if(x < xpos[coldrag - 1] + minwidth)
		    x = xpos[coldrag - 1] + minwidth;
		else if(x > xpos[coldrag + 1] - minwidth)
		    x = xpos[coldrag + 1] - minwidth;
		if(xpos[coldrag] != x) {
		    // Move column border
		    xpos[coldrag] = x;
		    // Recalculate relative sizes
		    xsizes[coldrag - 1] = (xpos[coldrag] - xpos[coldrag - 1]) /
			(float)getSize().width;
		    xsizes[coldrag] = (xpos[coldrag + 1] - xpos[coldrag]) /
			(float)getSize().width;
		    // We must redraw
		}
	    }
	    if(rowdrag > 0) {		// Dragging a row
		resize = true;
		// Ensure row is at least minheight high
		if(y < ypos[rowdrag - 1] + minheight)
		    y = ypos[rowdrag - 1] + minheight;
		else if(y > ypos[rowdrag + 1] - minheight)
		    y = ypos[rowdrag + 1] - minheight;
		if(ypos[rowdrag] != y) {
		    // Move row border
		    ypos[rowdrag] = y;
		    // Recalculate relative sizes
		    ysizes[rowdrag - 1] = (ypos[rowdrag] - ypos[rowdrag - 1]) /
			(float)getSize().height;
		    ysizes[rowdrag] = (ypos[rowdrag + 1] - ypos[rowdrag]) /
			(float)getSize().height;
		    // We must redraw
		}
	    }
	}
	dragBar.setMouse(e.getX(), e.getY());
	repaint();
    }

    /** Called when this container gets resized. */
    public void componentResized(ComponentEvent e) {
	doLayout();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    //****************************
    // Package and private methods
    //****************************

    /** Resize components into new grid. */
	void resizeComponents() {
	    int x, y;

	    // Reshape the components to fit
	    // For each row
	    for(y = 0; y < ysizes.length; y++) {
		// For each column
		for(x = 0; x < xsizes.length; x++) {
		    Component c = components[x][y];

		    if(c == null)
			continue;		// No component here!
		    if(x > 0 && components[x - 1][y] == c)
			continue;		// Already processed in last column
		    if(y > 0 && components[x][y - 1] == c)
			continue;		// Already processed in last row

		    int mx, my;

		    // Find all cells for this component to the right
		    for(mx = x + 1; mx < xsizes.length && components[mx][y] == c; mx++)
			;
		    // Component occupies cells x through mx inclusive
		    // Find all the cells for this component below
		    for(my = y + 1; my < ysizes.length && components[x][my] == c; my++)
			;
		    // Component occupies cells y through my inclusive
		    // Compute the width and height of the component
		    int w = xpos[mx] - xpos[x] - 2 * focuswidth;
		    int h = ypos[my] - ypos[y] - 2 * focusheight;
		    // Allow for the draggable bar if needed
		    if(mx < xsizes.length)
			w -= bwidth;
		    if(my < ysizes.length)
			h -= bheight;
		    // Finally, resize the component
		    c.setBounds(xpos[x] + focuswidth,
				ypos[y] + focusheight, w, h);
		    c.validate(); // added by alok, 7/18/2001
		}
	    }
	}

    /**
     * Work out if the mouse cursor is over a draggable border.
     * Only called if DragGridPanel is adjustable.
     * Sets cursor shape, curscol and cursrow appropriately.
     * @param x mouse position.
     * @param y mouse position.
     * @return true if cursor on a draggable border.
     */
    private boolean onDragBorder(int x, int y) {
	int col, row;

	curscol = cursrow = 0;		// Which border we are in (i.e. none)
	// Find out which column we are in
	for(col = 1; col < xsizes.length; col++) {
	    if(x <= xpos[col]) {	// In column col - 1, or its border
		if(x >= xpos[col] - bwidth)
		    curscol = col;	// In border, so set curscol
		break;
	    }
	}
	// Find out which row we are in
	for(row = 1; row < ysizes.length; row++) {
	    if(y <= ypos[row]) {	// In row row - 1, or its border
		if(y >= ypos[row] - bheight)
		    cursrow = row;	// In border, so set cursrow
		break;
	    }
	}
	// Col is set to the column number to the right of the cursor
	// Row is set to the row number immediately below the cursor
	// i.e. if cursor in top left cell, or its border, row and col will both be 1

	int newcursor = 0;			// New mouse cursor shape

	if(curscol > 0 || cursrow > 0) {	// We may be in a border
	    if(curscol > 0) {		// May be in a column border
		// Check if components above left and right are different
		if(components[curscol][row - 1] != components[col - 1][row - 1]
		   // Also check components below left and right
		   || (row < ysizes.length && components[curscol][row] != components[col - 1][row]))
		    newcursor = leftRightCursor;
		else
		    curscol = 0;	// In the middle of a component that takes more than 1 cell
	    }
	    if(cursrow > 0) {		// May be in a row border
		// Check if components left above and below are different
		if(components[col - 1][cursrow] != components[col - 1][row - 1]
		   // Also check components right above and below
		   || (col < xsizes.length && components[col][cursrow] != components[col][row - 1])) {
		    newcursor |= upDownCursor;
		} else
		    cursrow = 0;	// In the middle of a component that takes more than 1 cell
	    }
	}
	if(newcursor != currentCursor)
	    setCursor(newcursor);
	return (newcursor != 0);
    }

    /**
     * Switch cursor to indicate user can drag a border.
     * Only called if new cursor is different to current one.
     * @param newcursor newcursor to use, -1 to change it back.
     */
    private void setCursor(int newcursor) {
	if(newcursor > 0) {
	    // Want to set cursor to new shape
	    if(originalCursor == null)	// Save original shape
		originalCursor = getCursor();
	    switch(newcursor) {
	    case upDownCursor:
		setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		break;
	    case leftRightCursor:
		setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		break;
	    case crossHairCursor:
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		break;
	    case handCursor:
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		break;
	    }
	} else {
	    // Want to retsore original cursor
	    setCursor(originalCursor);
	    originalCursor = null;
	}
	currentCursor = newcursor;
    }

    private boolean dragging() {
	return (adjustable && (coldrag > 0 || rowdrag > 0));
    }

    //****************************
    // Variables
    //****************************

    /** Set to true if user is allowed to adjust sizes. */
	boolean adjustable = true;
    /** Width of the draggable borders between Components. */
    int bwidth = 6;
    /** Height of the draggable borders between Components. */
    int bheight = 6;
    /** Minimum width of a component. */
    int minwidth = 20;
    /** Minimum height of a component. */
    int minheight = 20;
    /** Width of the focus rectangle round each control. */
    int focuswidth = 0;
    /** Height of the focus rectangle round each control. */
    int focusheight = 0;

    /** The components, organised in a grid.
     * If a component takes up more than one cell, it is placed
     * in all the array positions it occupies.
     */
    Component components[][];
    /** The x positions of the dividing lines between cells.
     * xpos[0] is always 0, and xpos[no of columns] is the width of the Panel.
     */
    int xpos[];
    /** The y positions of the dividing lines between cells.
     * ypos[0] is always 0, and ypos[no of rows] is the height of the Panel.
     */
    int ypos[];
    /** The relative sizes of the cell columns. Add up to 1. */
    float xsizes[];
    /** The relative sizes of the cell rows. Add up to 1. */
    float ysizes[];
    /** column divider being dragged by mouse, or 0. */
    int coldrag;
    /** row divider being dragged by mouse, or 0. */
    int rowdrag;
    /** column divider which cursor is over. */
    int curscol;
    /** row divider which cursor is over. */
    int cursrow;
    /** original cursor type. */
    Cursor originalCursor;
    /** current cursor type. */
    int currentCursor = -1;
    /* ContainerFocusTracker to pass focus on to appropriate sub-component. 
	removed by alok 9/12/2001, as doesn't seem necessary.

	ContainerFocusTracker tracker = new ContainerFocusTracker(this);
     */
    /** Left-right cursor */
    static final int leftRightCursor = 1;
    /** Up-down cursor */
    static final int upDownCursor = 2;
    /** Cross-hair cursor */
    static final int crossHairCursor = 3;
    /** Hand cursor */
    static final int handCursor = 4;

    static final boolean trace = false;
    DragBar dragBar = new DragBar();

}

class DragBar extends JComponent {
    DragBar() {
	setOpaque(false);
    }
    Point mouse = new Point();
    public void setMouse(int x, int y) {
	mouse.x = x;
	mouse.y = y;
    }
    public void paintComponent(Graphics g) {
	g.setColor(Color.green);
	g.fillRect(mouse.x, 0, 2, getHeight());
	g.fillRect(0, mouse.y, getWidth(), 2);
    }

}

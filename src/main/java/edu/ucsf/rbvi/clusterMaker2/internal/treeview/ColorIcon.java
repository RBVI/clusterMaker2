/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: ColorIcon.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/12/21 03:28:14 $
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

import javax.swing.Icon;

/**
 *  A little icon with a changeable color.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.5 $ $Date: 2004/12/21 03:28:14 $
 */
public class ColorIcon implements Icon {
	private int width, height;
	private Color color;


	/**
	 * @param  x  width of icon
	 * @param  y  height of icon
	 * @param  c  Initial color of icon.
	 */
	public ColorIcon(int x, int y, Color c) {
		width = x;
		height = y;
		color = c;
	}


	/**
	 *  Sets the color, but doesn't redraw or anything.
	 *
	 * @param  c  The new color
	 */
	public void setColor(Color c) {
		color = c;
	}

	/* inherit description */
	public int getIconHeight() {
		return height;
	}


	/* inherit description */
	public int getIconWidth() {
		return width;
	}


	/* inherit description */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Color old  = g.getColor();
		g.setColor(color);
		g.fillRect(x, y, width, height);
		g.setColor(Color.black);
		g.drawRect(x, y, width, height);
		g.setColor(old);
	}
}


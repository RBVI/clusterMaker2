/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: NatField.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/12/21 03:28:13 $
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


// NatField: custom Java component: text field that constrains input
// to be numeric.
// Copyright (C) Lemma 1 Ltd. 1997
// Author: Rob Arthan; rda@lemma-one.com

// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version.

// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
// The GNU General Public License can be obtained at URL:
// http://www.lemma-one.com/scrapbook/gpl.html
// or by writing to the Free Software Foundation, Inc., 59 Temple Place,
// Suite 330, Boston, MA 02111-1307 USA.

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

public class NatField extends JTextField implements KeyListener {

	private int val;

	private int max = -1;

	public NatField(int num, int cols) {
		super(Integer.toString(num < 0 ? 0 : num), cols);
		val = num < 0 ? 0 : num;
		addKeyListener(this);
	}

	public NatField(int num, int cols, int maximum) {
		super(Integer.toString(num < 0 ? 0 : num), cols);
		val = num < 0 ? 0 : num;
		max = maximum;
		addKeyListener(this);
	}
	public void keyPressed (KeyEvent evt) {
	}
	public void keyReleased (KeyEvent evt) {
	}

	public void keyTyped(KeyEvent evt) {
		boolean revert;
		int new_val = 10;
		try {
			new_val = Integer.parseInt(getText());
			revert = false;

			if(new_val < 0) { // revert if negative
			  revert = true;
			}
			if(max > 0 && new_val > max) { // revert to max if too big
			  val = max;
			  revert = true;
			}
		} catch (NumberFormatException e) {
		  int len = getText().length();
		  if (len != 0)
			revert = true; // revert if can't convert;
		  else {
			revert = false;
			val = 0;
		  }
		  }
		if(revert) {
//		  System.out.println("Reverting value..");
			setText(Integer.toString(val));
		} else {
//		  System.out.println("keeping value.." + new_val);
			val = new_val;
		}
	}

	public int getNat() {
	  keyTyped(null);
	  return val;
	}
}

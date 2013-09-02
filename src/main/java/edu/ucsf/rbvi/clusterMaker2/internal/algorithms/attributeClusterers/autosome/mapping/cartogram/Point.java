package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.mapping.cartogram;

/*
***********************************************************************************************
SOFTWARE USE AGREEMENT

Conditions of Use:
AutoSOME is freely available to the academic/non-profit community for non-commercial research
purposes.

All downloads are subject to the following terms: Software and source code Copyright (C) 2009
Aaron M. Newman. Permission to use this software and its documentation is hereby granted to
all academic and not-for-profit institutions for non-profit/non-commercial applications
without fee. The right to use this software for profit, by private companies or other organizations, or in
conjunction with for profit activities, are NOT granted except by prior arrangement and written
 consent of the copyright holder.

For these purposes, downloads of the software constitutes "use" and downloads of this software
 by for profit organizations and/or distribution to for profit institutions is explicitly
prohibited without the prior consent of the copyright holder.

The software is provided "AS-IS" and without warranty of any kind, express, implied or
otherwise. In no event shall the copyright holder be liable for any damages of any kind
arising out of or in connection with the use or performance of this software. This code was
written using Java and may be subject to certain additional restrictions as a result.
***********************************************************************************************
 */

;

  public class Point{
	public Point(){
		x = y = z = 0;
	}
	public Point(float x, float y, float z){
		this.x = x;
		this.y = y;
                this.z = z;
	}
	public float x,y,z;

	
}

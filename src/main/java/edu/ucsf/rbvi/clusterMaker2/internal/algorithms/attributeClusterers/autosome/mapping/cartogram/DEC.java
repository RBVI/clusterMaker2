package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.mapping.cartogram;

/*

 */



import java.awt.geom.GeneralPath;
import java.io.*;
import java.util.*;




public class DEC {


	private String genFileName = ""; // Cartogram generate file. - output
	private String dataFileName = ""; // Input - open file for input
        private String polygonFileName = ""; // Input coordinates. open file for input
        private int maxNSquareLog = 14; // The maximum number of squares is 2^MAXNSQLOG.
        private double blurWidth = 0.39; // Initial width of Gaussian blur. Originally named SIGMA
	private double blurWidthFactor = 1.2; // Must be > 1. it is the factor by which sigma increased upon an unsuccessful pass. Originally named SIGMAFAC
        private int arrayLength;
        
        private ArrayList[] polygons;
        private float[] census;

        public static final int DEFAULT_MAXNSQUARELOG = 8;
        public static final double DEFAULT_BLURWIDTH = 0.1;
        public static final double DEFAULT_BLURWIDTHFACTOR= 1.2;

	private static final double CONVERGENCE = 1e-100; // Convergence criterion for integrator.
	private static final double HINITIAL = 1e-4; // Initial time step size in nonlinvoltra.
	private static final int IMAX = 50; // Maximum number of iterations in Newton-Raphson routine.
	private static final int MAXINTSTEPS = 3000; // Maximum number of time steps in nonlinvoltra.

	private static final double MINH = 1e-5; // Smallest permitted time step in the integrator.
	private static final int NSUBDIV = 1; // Number of linear subdivisions for digitizing the density.
	private static final double PADDING = 2; // Determines space between map and boundary.
	private static final double PI = 3.141592653589793;

	//#define SWAP = (a,b) tempr=(a);(a)=(b);(b)=tempr;
	//SWAP cannot be used as a macro here, but instead it will be coded whenever necessary
	private static final double TIMELIMIT = 1e8 ;// Maximum time allowed in integrator.
	private static final double TOLF = 1e-3; // Sensitivity w. r. t. function value in newt2.
	private static final double TOLINT = 1e-3; // Sensitivity of the integrator.
	private static final double TOLX = 1e-3; // Sensitivity w. r. t. independent variables in newt2.


    public static final String DISPLFILE = null; //or just string

	//Globals
        GeneralPath[] inputShapes;
        GeneralPath[] outputShapes;
	ArrayFloat 	gridvx[], gridvy[], gridvz[];
	float maxx,maxy,maxz,
		  minpop,
		  minx,miny,minz,
		  polymaxx,polymaxy,polymaxz,
		  polyminx,polyminy,polyminz;

	ArrayFloat 	rho[],rho_0[],
		  		vx[],vy[],vz[],
				x[],y[],z[],
				xappr[],yappr[],zappr[];

	float xstepsize, ystepsize, zstepsize;

	int lx,ly,lz,
	    maxid,nblurs=0,npoly;

	int ncorn[], polygonid[];
	ArrayPoint corn[];
        float[] errors;
        float[][] output;
        StringBuffer ps = new StringBuffer();
        float[] zcensus;
        ArrayList[] zpolygons;
        boolean zaxis = false;
        float[][] xy;
        float[][][] densities;
        int[][] origCoors;
        boolean twoD = false;
        boolean indep = true;
        int xyres, zres;
        int zSpace = 0;
        int zAxisSize = 0;


//http://www.cs.princeton.edu/introcs/26function/MyMath.java.html
//can try using: http://home.online.no/~pjacklam/notes/invnorm/impl/karimov/StatUtil.java
// fractional error less than 1.2 * 10 ^ -7.
    private static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
                                            t * ( 1.00002368 +
                                            t * ( 0.37409196 +
                                            t * ( 0.09678418 +
                                            t * (-0.18628806 +
                                            t * ( 0.27886807 +
                                            t * (-1.13520398 +
                                            t * ( 1.48851587 +
                                            t * (-0.82215223 +
                                            t * ( 0.17087277))))))))));
        if (z >= 0) return  ans;
        else        return -ans;
    }



	// Function to count polygon corners. Also determines minimum/maximum x-/y-
	// coordinate.
	private void countcorn()
	{
		String line;
		//BufferedReader inFile = new BufferedReader(new StringReader(polygons));
		float x,y,z;
		int polyctr=0,ratiolog;

		npoly = polygons.length;
                
		ncorn = new int[npoly];
		corn = new ArrayPoint[npoly];
                if(!zaxis) xy = new float[npoly*4][2];
	
              
		polyminx = polyminy = polyminz = Float.MAX_VALUE; 
                polymaxx = polymaxy = polymaxz = Float.MIN_VALUE;
		ncorn[0] = 1;
                
                for(int h = 0; h < polygons.length; h++){
        
                float[][] poly = (float[][]) polygons[h].get(0);

		for(int k = 0; k < poly.length; k++)
		{
			
				x = poly[k][0];
				y = poly[k][1];
                                z = poly[k][2];
                                //System.out.println(line);
				if (x < polyminx) polyminx = x;
				if (x > polymaxx) polymaxx = x;
				if (y < polyminy) polyminy = y;
				if (y > polymaxy) polymaxy = y;
                                if (z < polyminz) polyminz = z;
				if (z > polymaxz) polymaxz = z;
				
			
		
				
		}
                
                corn[h] = new ArrayPoint(poly.length);

            
                }
                densities = new float[(zaxis) ? (zSpace+1) * (1+(int)polymaxx) : (1+(int)polymaxx)][1+(int)polymaxy][1+(int)polymaxz];
                origCoors = new int[npoly][3];
                
                polymaxz++;
                
                //System.out.println(polyminx+" "+polymaxx+" "+polyminy+" "+polymaxy+" "+Math.log((polymaxx-polyminx)/(polymaxy-polyminy))+" "+Math.log(2));
                //System.out.println(Math.ceil(Math.log((polymaxx-polyminx)/(polymaxy-polyminy))/Math.log(2))+" "+Math.floor(Math.log((polymaxx-polyminx)/(polymaxy-polyminy))/Math.log(2))+" "+2*Math.log((polymaxx-polyminx)/(polymaxy-polyminy))/Math.log(2));
                //System.out.println((int)Math.floor(Math.log((polymaxx-polyminx)/(polymaxy-polyminy))/Math.log(2))+" "+(int)Math.ceil(Math.log((polymaxx-polyminx)/(polymaxy-polyminy))/Math.log(2)));
 
                
		lx = (int)Math.pow(2,(int)(0.5*(maxNSquareLog)));
		ly = (int)Math.pow(2,(int)(0.5*(maxNSquareLog)));
                
                if (Math.ceil(Math.log((polymaxx-polyminx)/(polymaxz-polyminz))/Math.log(2))+
				Math.floor(Math.log((polymaxx-polyminx)/(polymaxz-polyminz))/Math.log(2))>
				2*Math.log((polymaxx-polyminx)/(polymaxz-polyminz))/Math.log(2))
			ratiolog = (int)Math.floor(Math.log((polymaxx-polyminx)/((polymaxz-polyminz)))/Math.log(2));
		else
			ratiolog = (int)Math.ceil(Math.log((polymaxx-polyminx)/((polymaxz-polyminz)))/Math.log(2));
                
                lz = (int)Math.pow(2,(int)Math.ceil(0.5*(maxNSquareLog-ratiolog)))/2;
                //lz*=2;
                if(zaxis) {lx = zAxisSize; lz = ly;}
                else zAxisSize = Math.max(lz,16);
               // lz = 0;
                if(twoD) lz = 2;
                
                //System.out.println(lx+" "+ly+" "+lz+" "+ratiolog);
               
                //lz = (int)polymaxz-1;
                
		if ((polymaxx-polyminx)/lx > (polymaxy-polyminy)/ly)
		{
			maxx = (float) (0.5*((1+PADDING)*polymaxx+(1-PADDING)*polyminx));
			minx = (float) (0.5*((1-PADDING)*polymaxx+(1+PADDING)*polyminx));
			maxy = (float) (0.5*(polymaxy+polyminy+(maxx-minx)*ly/lx));
			miny = (float) (0.5*(polymaxy+polyminy-(maxx-minx)*ly/lx));
                        maxz = polymaxz;// : (float) (0.5*((1+PADDING)*polymaxz+(1-PADDING)*polyminz));
			minz = 0;// :(float) (0.5*((1-PADDING)*polymaxz+(1+PADDING)*polyminz));

		}
		else
		{
			maxy = (float) (0.5*((1+PADDING)*polymaxy+(1-PADDING)*polyminy));
			miny = (float) (0.5*((1-PADDING)*polymaxy+(1+PADDING)*polyminy));
			maxx = (float) (0.5*(polymaxx+polyminx+(maxy-miny)*lx/ly));
			minx = (float) (0.5*(polymaxx+polyminx-(maxy-miny)*lx/ly));
                        maxz = polymaxz;// : (float) (0.5*((1+PADDING)*polymaxz+(1-PADDING)*polyminz));
			minz = 0;// :(float) (0.5*((1-PADDING)*polymaxz+(1+PADDING)*polyminz));

		}
           
                //System.out.println(polymaxx+" "+polyminx+" "+polymaxy+" "+polyminy+" "+polymaxz+" "+polyminz);
                //System.out.println(maxx+" "+minx+" "+maxy+" "+miny+" "+maxz+" "+minz);
              
		//FileTools.closeFile(inFile);
	}


// Function to read polygon corners.
		private void readcorn()
	{
		String line;
		//BufferedReader inFile = new BufferedReader(new StringReader(polygons));
		float xcoord,ycoord,zcoord;
		int i,id,polyctr=0;

		countcorn();
               // System.out.println("Number of polygons: "+npoly);
		polygonid = new int[npoly];
		xstepsize = (maxx-minx)/lx;
		ystepsize = (maxy-miny)/ly;
                zstepsize = (maxz-minz)/lz;
		if (Math.abs((xstepsize/ystepsize)-1)>1e-3)
			System.err.println("WARNING: Area elements are not square: "+xstepsize+" : "+ystepsize+"\n");
		int itor = 0;
                i = 0;
                boolean store = true;
                for(int h = 0; h < polygons.length; h++){
                       polygonid[h] = h;
                       float[][] poly = (float[][]) polygons[h].get(0);

                        for(int k = 0; k < poly.length; k++)
                        {			
				xcoord = poly[k][0];
				ycoord = poly[k][1];
                                zcoord = poly[k][2];
                                
                                 if(store){
                                    origCoors[itor][0] = (int)xcoord;
                                    origCoors[itor][1] = (int)ycoord;
                                    origCoors[itor++][2] = (int)zcoord;
                                    store = false;
                                }
                                
				corn[h].array[i].x = (xcoord-minx)/xstepsize;
				corn[h].array[i].y = (ycoord-miny)/ystepsize;
                                corn[h].array[i++].z = (zcoord-minz)/zstepsize;
                   
                        }
       
                        i = 0;
                        store = true;
                }
                
                
        
		polyminx = (polyminx-minx)/xstepsize;
		polyminy = (polyminy-miny)/ystepsize;
		polymaxx = (polymaxx-minx)/xstepsize;
		polymaxy = (polymaxy-miny)/ystepsize;
                polymaxz = (polymaxz-minz)/zstepsize;
		polyminz = (polyminz-minz)/zstepsize;
                //System.out.println(polymaxx+" "+polyminx+" "+polymaxy+" "+polyminy+" "+polymaxz+" "+polyminz);
                //System.out.println(maxx+" "+minx+" "+maxy+" "+miny+" "+maxz+" "+minz);
	
		//FileTools.closeFile(inFile);
	}




// Function to digitize density.
	private void digdens()
	{
		String line;
		double volume[],totVol=0.0,totpop=0.0;
		//BufferedReader inFile = new BufferedReader(new StringReader(census));
		float ncases, avgdens, dens[];
		int i,id,ii,j,jj,polyctr,k;
                float[] cases;
		// Read CENSUSFILE.

		cases = new float[census.length];
                
                for(int f = 0; f < census.length; f++){
                    double pop =  (cases[f] = census[f]);
                    if(pop != Double.MAX_VALUE && !Double.isInfinite(pop)) totpop += pop;
                    else totVol--;
                }

		// Calculate for each polygon the area of the political unit it belongs to.
		// This will in general not be the area of the polygon, e. g. if it consists
		// of several islands. Here we assume that the polygon identifiers in
		// BOUNDARYFILE are the same for all polygons belonging to one political
		// unit.
                
                volume = new double[npoly];
		for (polyctr=0; polyctr<npoly; polyctr++)
		{
			totVol += 1;//polygonVolume(ncorn[polyctr],corn[polyctr].array);
			volume[polyctr] = 0;
			//for (i=0; i<npoly; i++) if (polygonid[i]==polygonid[polyctr])
			volume[polyctr] += 1;//polygonVolume(ncorn[polyctr],corn[polyctr].array);
                        //System.out.println(volume[polyctr]+" "+polyctr);
		}

		// Calculate the correct density for each polygon.

		dens = new float[npoly];
                errors = new float[npoly];
		for (polyctr=0; polyctr<npoly; polyctr++){
                    dens[polyctr] = (float)(cases[polygonid[polyctr]]/(double)volume[polyctr]);
                    //System.out.println(dens[polyctr]+" "+cases[polygonid[polyctr]]+" "+volume[polyctr]);
                    //  System.out.println(polyctr+" "+origCoors[polyctr][0]+" "+origCoors[polyctr][1]+" "+origCoors[polyctr][2]);
                    
                    densities[origCoors[polyctr][0]][origCoors[polyctr][1]][origCoors[polyctr][2]] = dens[polyctr];
                  
                    if(zaxis){
                        for(int h = origCoors[polyctr][0]; h < origCoors[polyctr][0]+zSpace; h++)
                            densities[h][origCoors[polyctr][1]][origCoors[polyctr][2]] = dens[polyctr];
                    }
                    
                    errors[polyctr] = dens[polyctr];
                    //System.out.println(cases[polygonid[polyctr]]+" "+volume[polyctr]+" "+dens[polyctr]);
                }
		//System.out.println(totpop+" "+totVol);

		// Calculate the average density.

		avgdens = (float) (totpop/totVol);

                for(k = 0; k <= lz; k++)                
    		{                          
                    for (i=0; i<lx; i++)
                    {
                            for (j=0; j<ly; j++)
                            {
                                
					if (i < polyminx ||
							i > polymaxx ||
							j < polyminy ||
							j > polymaxy )
					{ 
						rho_0[k].array[i][j] += avgdens;
						continue;
					}
                                        rho_0[k].array[i][j] += within2(i,j,k,avgdens); 
                                        if(rho_0[k].array[i][j] == 100 || rho_0[k].array[i][j] == Double.MAX_VALUE || Double.isInfinite(rho_0[k].array[i][j])) rho_0[k].array[i][j] = avgdens;
                                     //   System.out.println(rho_0[k].array[i][j]+" "+avgdens);
				}
                        }
                       // System.out.println(k);
		}
                
              
  
              //  System.out.println(lz+" "+lx+" "+ly+" "+rho_0.length+" "+rho_0[0].array.length+" "+rho_0[0].array[0].length);
		// Fill the edges correctly.
            for(k = 0; k <= lz; k++){
		rho_0[k].array[0][0] += rho_0[k].array[0][ly] + rho_0[k].array[lx][0] + rho_0[k].array[lx][ly];
    		for (i=1; i<lx; i++) rho_0[k].array[i][0] += rho_0[k].array[i][ly];
		for (j=1; j<ly; j++) rho_0[k].array[0][j] += rho_0[k].array[lx][j];
		for (i=0; i<lx; i++) rho_0[k].array[i][ly] = rho_0[k].array[i][0];
		for (j=0; j<=ly; j++) rho_0[k].array[lx][j] = rho_0[k].array[0][j];
            }
		// Replace rho_0 by Fourier transform
     //       if(zaxis){
       /* for(int a = 0; a <= lz; a++){
          for(int b = 0; b <= ly; b++){
                        for(int c = 0; c <= lx; c++){
                            System.out.print(rho_0[a].array[c][b]+" ");
                        }
                        System.out.print("\n");
                    }
                    System.out.println("*********************************");
        }
                    //System.exit(1);
                //}
              */ 
                
		coscosft(rho_0,1,1);

		//area = null; //free(area);
		cases = null; //free(cases);
		for (i=0; i<npoly; i++){
			corn[i].array = null;
			corn[i] = null; //free(corn[i]);
		}
		corn = null; //free(corn);
		dens = null; //free(dens);
		ncorn = null; //free(ncorn);
		polygonid = null; //free(polygonid);
	}

        
  
        
              
      
        
        private float within2(float x, float y, float z, float avedens){
            //System.out.println(x+" "+y+" "+z+" "+xstepsize+" "+ystepsize+" "+zstepsize+" "+minx+" "+miny+" "+minz);
   
            float X = x;
            float Y = y;
            
            x *= xstepsize;
            y *= ystepsize;
            z *= zstepsize;
            x += minx;
            y += miny;
            z += minz;
            
    
            //System.out.println(x+" "+y);
            
              
            if(Math.floor(x)- x == 0 && Math.floor(y) < y){
                //y = (float)Math.floor(y);
                x--;
                y = (float)Math.ceil(y);
            }
            else if(Math.floor(y) - y == 0 && Math.floor(x) < x){
               // x = (float)Math.floor(x);
                y--;
                x = (float)Math.floor(x);
            }else{
                x = (float)Math.floor(x);
                y = (float)Math.ceil(y);
            }

            if(Math.abs(X-polyminx)<1) x = 0;
            if(Math.abs(Y-polyminy)<1) y = 0;
            if(Math.abs(X-polymaxx)<1) x = (densities.length / ((zaxis) ? (zSpace+1) : 1))-2;
            if(Math.abs(Y-polymaxy)<1) y = densities[0].length-2;
            

      
           // System.out.println(x+" "+y);
            
            
          //  if(x>=0 && y>=0 && z>=0 && x < densities.length && y < densities[0].length){
                int zPos = (int)Math.floor(z);
                if(zPos >= densities[0][0].length)  zPos = densities[0][0].length-1;
                return densities[(int)x][(int)y][zPos];
          //  }
          //  else return avedens;
        }
        
        
	// Function to replace data[1...2*nn] by its discrete Fourier transform, if
	// isign is input as 1; or replaces data[1...2*nn] by nn times its inverse
	// discrete Fourier transform, if isign is input as -1. data is a complex array
	// of length nn or, equivalently, a real array of length 2*nn. nn MUST be an
	// integer power of 2 (this is not checked for!).
	// From "Numerical Recipes in C".
	private void four1(float data[], long nn,int isign)
	//unsigned long (32bit) changed to long (64 bit)
	{
		double theta,wi,wpi,wpr,wr,wtemp;
		float tempi,tempr;
		long i,istep,j,m,mmax,n;
		float tmpf;

		n=nn<<1;
		j=1;
  
		for (i=1; i<n; i+=2)
		{
			if (j>i)
			{
				// This is the bit-reversal section of the routine.
				//SWAP(data[j],data[i]);
				tmpf = data[(int) j];
				data[(int) j] = data[(int) i];
				data[(int) i] = tmpf;

				//SWAP(data[j+1],data[i+1]); // Exchange the two complex numbers.
				tmpf = data[(int)j+1];
				data[(int)j+1] = data[(int)i+1];
				data[(int)i+1] = tmpf;
			}
			m=n>>1;
			while (m>=2 && j>m)
			{
				j -= m;
				m>>=1;
			}
			j += m;
		}
		// Here begins the Danielson-Lanczos section of the routine.
		mmax=2;
		while (n>mmax) // Outer loop executed log_2 nn times.
		{
			istep = mmax<<1;
			// Initialize the trigonometric recurrence.
			theta = isign*(6.28318530717959/mmax);
			wtemp = Math.sin(0.5*theta);
			wpr = -2.0*wtemp*wtemp;
			wpi = Math.sin(theta);
			wr = 1.0;
			wi = 0.0;
			for (m=1; m<mmax; m+=2) // Here are the two nested inner loops.
			{
				for (i=m; i<=n; i+=istep)
				{
					j=i+mmax; // This is the Danielson-Lanczos formula
					tempr=(float) (wr*data[(int)j]-wi*data[(int)j+1]);
					tempi=(float) (wr*data[(int)j+1]+wi*data[(int)j]);
					data[(int)j]=data[(int)i]-tempr;
					data[(int)j+1]=data[(int)i+1]-tempi;
					data[(int)i] += tempr;
					data[(int)i+1] += tempi;
				}
				wr = (wtemp=wr)*wpr-wi*wpi+wr; // Trigonometric recurrence.
				wi = wi*wpr+wtemp*wpi+wi;
			}
			mmax=istep;
		}
	}


// Function to calculate the Fourier Transform of a set of n real-valued data
// points. It replaces this data (which is stored in array data[1...n]) by the
// positive frequency half of its complex Fourier Transform. The real-valued
// first and last components of the complex transform are returned as elements
// data[1] and data[2] respectively. n must be a power of 2. This routine also
// calculates the inverse transform of a complex data array if it is the
// transform of real data. (Result in this case must be multiplied by 2/n).
// From "Numerical Recipes in C".
	private void realft(float data[], long n,int isign)
	//changed unsigned int to long (64 bits)
	{
		double theta,wi,wpi,wpr,wr,wtemp;
		float c1=(float)0.5,c2,h1i,h1r,h2i,h2r;
		long i,i1,i2,i3,i4,np3;

		theta = 3.141592653589793/(double) (n>>1); // Initialize the recurrence
		if (isign == 1)
		{
			c2 = (float)-0.5;
			four1(data,n>>1,1); // The forward transform is here.
		}
		else // Otherwise set up for an inverse transform.
		{
			c2 = (float) 0.5;
			theta = -theta;
		}
		wtemp = Math.sin(0.5*theta);
		wpr = -2.0*wtemp*wtemp;
		wpi = Math.sin(theta);
		wr = 1.0+wpr;
		wi = wpi;
		np3 = n+3;
		for (i=2; i<=(n>>2); i++) // Case i=1 done separately below.
		{
			i4 = 1+(i3=np3-(i2=1+(i1=i+i-1)));
			// The two separate transforms are separated out of data.
			h1r = c1*(data[(int)i1]+data[(int)i3]);
			h1i = c1*(data[(int)i2]-data[(int)i4]);
			h2r = -c2*(data[(int)i2]+data[(int)i4]);
			h2i = c2*(data[(int)i1]-data[(int)i3]);
			// Here they are recombined to form the true transform of the original
			// data.
			data[(int)i1] = (float) (h1r+wr*h2r-wi*h2i);
			data[(int)i2] = (float) (h1i+wr*h2i+wi*h2r);
			data[(int)i3] = (float) (h1r-wr*h2r+wi*h2i);
			data[(int)i4] = (float) (-h1i+wr*h2i+wi*h2r);
			wr = (wtemp=wr)*wpr-wi*wpi+wr; // The recurrence.
			wi = wi*wpr+wtemp*wpi+wi;
		}
		if (isign == 1)
		{
			data[1] = (h1r=data[1])+data[2]; // Squeeze the first and last data
			// together to get them all within the original array.
			data[2] = h1r-data[2];
		}
		else
		{
			data[1] = c1*((h1r=data[1])+data[2]);
			data[2] = c1*(h1r-data[2]);
			// This is the inverse transform for the case isign = -1.
			four1(data,n>>1,-1);
		}
	}


// Function to calculate the cosine transform of a set z[0...n] of real-valued
// data points. The transformed data replace the original data in array z. n
// must be a power of 2. For forward transform set isign=1, for back transform
// isign = -1. (Note: The factor 2/n has been taken care of.)
// From "Numerical Recipes in C".
	private void cosft(float z[],int n,int isign)
	{
		double theta,wi=0.0,wpi,wpr,wr=1.0,wtemp;
		float a[],sum,y1,y2;
		int j,n2;

		// Numerical Recipes starts counting at 1 which is rather confusing. I will
		// count from 0.

		a = new float[n+2];
		for (j=1; j<=n+1; j++) a[j] = z[j-1];

		// Here is the Numerical Recipes code.

		theta=PI/n; //Initialize the recurrence.
		wtemp = Math.sin(0.5*theta);
		wpr = -2.0*wtemp*wtemp;
		wpi = Math.sin(theta);
		sum = (float) (0.5*(a[1]-a[n+1]));
		a[1] = (float) (0.5*(a[1]+a[n+1]));
		n2 = n+2;
		for (j=2; j<=(n>>1); j++)
		{
			wr = (wtemp=wr)*wpr-wi*wpi+wr;
			wi = wi*wpr+wtemp*wpi+wi;
			y1 = (float) (0.5*(a[j]+a[n2-j]));
			y2 = (a[j]-a[n2-j]);
			a[j] = (float) (y1-wi*y2);
			a[n2-j] = (float) (y1+wi*y2);
			sum += wr*y2;
		}
		realft(a,n,1);
		a[n+1] = a[2];
		a[2] = sum;
		for (j=4; j<=n; j+=2)
		{
			sum += a[j];
			a[j] = sum;
		}

		// Finally I revert to my counting method.

		if (isign == 1) for (j=1; j<=n+1; j++) z[j-1] = a[j];
		else if (isign == -1) for (j=1; j<=n+1; j++) z[j-1] = (float) (2.0*a[j]/n);
		a = null;
	}

// Function to calculate the sine transform of a set of n real-valued data
// points stored in array z[0..n]. The number n must be a power of 2. On exit
// z is replaced by its transform. For forward transform set isign=1, for back
// transform isign = -1.

//NO OBJECT REFERENCE
	//unsigned long n ->int n
	private void sinft(float z[], int n,int isign)
	{
		double theta,wi=0.0,wpi,wpr,wr=1.0,wtemp;
		float a[],sum,y1,y2;
		int j;
		int n2=n+2; //unsigned long

		// See my comment about Numerical Recipe's counting above. Note that the last
		// component plays a completely passive role and does not need to be stored.

		a = new float[n+1];
		for (j=1; j<=n; j++) a[j] = z[j-1];

		// Here is the Numerical Recipes code.

		theta = PI/(double)n; // Initialize the recurrence.
		wtemp = Math.sin(0.5*theta);
		wpr = -2.0*wtemp*wtemp;
		wpi = Math.sin(theta);
		a[1] = 0.0f;
		for (j=2; j<=(n>>1)+1; j++)
		{
			// Calculate the sine for the auxiliary array.

			wr = (wtemp=wr)*wpr-wi*wpi+wr;

			// The cosine is needed to continue the recurrence.

			wi = wi*wpr+wtemp*wpi+wi;

			// Construct the auxiliary array.

			y1 = (float) (wi*(a[j]+a[n2-j]));
			y2 = (float) (0.5*(a[j]-a[n2-j]));

			// Terms j and N-j are related.

			a[j] = y1+y2;
			a[n2-j] = y1-y2;
		}

		// Transform the auxiliary array.

		realft(a,n,1);

		// Initialize the sum used for odd terms below.

		a[1] *= 0.5;
		sum = a[2] = 0.0f;

		// Even terms determined directly. Odd terms determined by running sum.

		for (j=1; j<=n-1; j+=2)
		{
			sum += a[j];
			a[j] = a[j+1];
			a[j+1] = sum;
		}

		// Change the indices.

		if (isign == 1) for (j=1; j<=n; j++) z[j-1] = a[j];
		else if (isign == -1) for (j=1; j<=n; j++) z[j-1] = (float)2.0*a[j]/n;
		z[n] = 0.0f;
		a=null;
	}



// Function to calculate a two-dimensional cosine Fourier transform. Forward/
// backward transform in x: isign1 = +/-1, in y: isign2 = +/-1.

//NO OBJECT REFERENCE
	private void coscosft(ArrayFloat y[],int isign1,int isign2)
	{
		float temp[] = new float[lx+1];
		int i,j,k; //unsigned long
                
                for(k = 0; k <= lz; k++){
                    for (i=0; i<=lx; i++)
                    {
			cosft(y[k].array[i],ly,isign2);
                    }
                    for (j=0; j<=ly; j++)
                    {
			for (i=0; i<=lx; i++) temp[i]=y[k].array[i][j];
			cosft(temp,lx,isign1);
			for (i=0; i<=lx; i++) y[k].array[i][j]=temp[i];
                    }
                }
                for(i = 0; i <= lx; i++){
                    for(j = 0; j <= ly; j++){

                        temp = new float[lz+1];
			for (k =0; k<=lz; k++) temp[k]=y[k].array[i][j];
			cosft(temp,lz,isign1);
			for (k =0; k<=lz; k++)  y[k].array[i][j]=temp[k];

                    }
                }
              
	}

// Function to calculate a cosine Fourier transform in x and a sine transform
// in y. Forward/backward transform in x: isign1 = +/-1, in y: isign2 = +/-1.

//NO OBJECT REFERENCE
	private void cossinft(ArrayFloat y[],int isign1,int isign2)
	{
		float temp[] = new float[lx+1];
		int i,j,k; //unsigned long

		for(k = 0; k <= lz; k++){
                    for (i=0; i<=lx; i++)
                    {
			sinft(y[k].array[i],ly,isign2);
                    }
                    for (j=0; j<=ly; j++)
                    {
			for (i=0; i<=lx; i++) temp[i]=y[k].array[i][j];
			cosft(temp,lx,isign1);
			for (i=0; i<=lx; i++) y[k].array[i][j]=temp[i];
                    }
                }
               
                for(i = 0; i <= lx; i++){
                    for(j = 0; j <= ly; j++){

                        temp = new float[lz+1];
			for (k =0; k<=lz; k++) temp[k]=y[k].array[i][j];
			cosft(temp,lz,isign1);
			for (k =0; k<=lz; k++)  y[k].array[i][j]=temp[k];

                    }
                }
                
	}

// Function to calculate a sine Fourier transform in x and a cosine transform
// in y. Forward/backward transform in x: isign1 = +/-1, in y: isign2 = +/-1.

//NO OBJECT REFERENCE
	private void sincosft(ArrayFloat y[],int isign1,int isign2)
	{
		float temp[] =new float[lx+1];
		int i,j,k;//unsigned long

		for(k = 0; k <= lz; k++){
                    for (i=0; i<=lx; i++)
                    {
			cosft(y[k].array[i],ly,isign2);
                    }
                    for (j=0; j<=ly; j++)
                    {
			for (i=0; i<=lx; i++) temp[i]=y[k].array[i][j];
			sinft(temp,lx,isign1);
			for (i=0; i<=lx; i++) y[k].array[i][j]=temp[i];
                    }
                }
                  for(i = 0; i <= lx; i++){
                    for(j = 0; j <= ly; j++){

                        temp = new float[lz+1];
			for (k =0; k<=lz; k++) temp[k]=y[k].array[i][j];
			cosft(temp,lz,isign1);
			for (k =0; k<=lz; k++)  y[k].array[i][j]=temp[k];

                    }
                }
	}
        
        private void sinzft(ArrayFloat y[],int isign1,int isign2)
	{
		float temp[] =new float[lx+1];
		int i,j,k;//unsigned long

		for(k = 0; k <= lz; k++){
                    for (i=0; i<=lx; i++)
                    {
			cosft(y[k].array[i],ly,isign2);
                    }
                    for (j=0; j<=ly; j++)
                    {
			for (i=0; i<=lx; i++) temp[i]=y[k].array[i][j];
			cosft(temp,lx,isign1);
			for (i=0; i<=lx; i++) y[k].array[i][j]=temp[i];
                    }
                }
                  for(i = 0; i <= lx; i++){
                    for(j = 0; j <= ly; j++){

                        temp = new float[lz+1];
			for (k =0; k<=lz; k++) temp[k]=y[k].array[i][j];
			sinft(temp,lz,isign1);
			for (k =0; k<=lz; k++)  y[k].array[i][j]=temp[k];

                    }
                }
	}

// Function to replace data by its ndim-dimensional discrete Fourier transform,
// if isign is input as 1. nn[1..ndim] is an integer array containing the
// lengths of each dimension (number of complex values), which MUST be all
// powers of 2. data is a real array of length twice the product of these
// lengths, in which the data are stored as in a multidimensional complex
// array: real and imaginary parts of each element are in consecutive
// locations, and the rightmost index of the array increases most rapidly as
// one proceeds along data. For a two-dimensional array, this is equivalent to
// storing the arrays by rows. If isign is input as -1, data is replaced by its
// inverse transform times the product of the lengths of all dimensions.

//NO OBJECT REFERENCE
	//unsigned long nn[] -> changed to int nn[]
	//data[] must became a d3tensor from [1..][1..][1..] to regular
	private void fourn(D3Tensor data,int[] nn,int ndim,int isign)
	{
		int idim;
		long i1,i2,i3,i2rev,i3rev,ip1,ip2,ip3,ifp1,ifp2; //unsigned long
		long ibit,k1,k2,n,nprev,nrem,ntot; //unsigned long
		double tempi,tempr;
		float theta,wi,wpi,wpr,wr,wtemp;

		for (ntot=1, idim=1; idim<=ndim; idim++)
			ntot *= nn[idim];
		nprev = 1;
		for (idim=ndim; idim>=1; idim--)
		{
			n = nn[idim];
			nrem = ntot/(n*nprev);
			ip1=nprev << 1;
			ip2 = ip1*n;
			ip3 = ip2*nrem;
			i2rev = 1;
			for (i2=1; i2<=ip2; i2+=ip1)
			{
				if (i2 < i2rev)
				{
					for (i1=i2; i1<=i2+ip1-2; i1+=2)
					{
						for (i3=i1; i3<=ip3; i3+=ip2)
						{
							i3rev = i2rev+i3-i2;
							//SWAP(data[i3],data[i3rev]);
							data.swapElements((int)i3,(int)i3rev);

							//SWAP(data[i3+1],data[i3rev+1]);
							data.swapElements((int)i3+1,(int)i3rev+1);
						}
					}
				}
				ibit = ip2>>1;
				while (ibit>=ip1 && i2rev>ibit)
				{
					i2rev -= ibit;
					ibit >>= 1;
				}
				i2rev += ibit;
			}
			ifp1 = ip1;
			while (ifp1 < ip2)
			{
				ifp2 = ifp1 << 1;
				theta = (float) (2*isign*PI/(ifp2/ip1));
				wtemp = (float) Math.sin(0.5*theta);
				wpr = (float) (-2.0*wtemp*wtemp);
				wpi = (float) Math.sin(theta);
				wr = 1.0f;
				wi = 0.0f;
				for (i3=1; i3<=ifp1; i3+=ip1)
				{
					for (i1=i3; i1<=i3+ip1-2; i1+=2)
					{
						for (i2=i1; i2<=ip3; i2+=ifp2)
						{
							k1 = i2;
							k2 = k1+ifp1;
							tempr = (float)wr*data.getElement((int)k2)-(float)wi*data.getElement((int)k2+1);
							tempi = (float)wr*data.getElement((int)k2+1)+(float)wi*data.getElement((int)k2);
							data.setElement((int) k2,(float) (data.getElement((int)k1)-tempr));
							data.setElement((int)k2+1, (float) (data.getElement((int)k1+1)-tempi));
							data.addToElement((int)k1, (float)tempr);
							data.addToElement((int)k1+1, (float)tempi);
						}
					}
					wr = (wtemp=wr)*wpr-wi*wpi+wr;
					wi = wi*wpr+wtemp*wpi+wi;
				}
				ifp1 = ifp2;
			}
			nprev *= n;
		}
	}

// Function to calculate a three-dimensional Fourier transform of
// data[1..nn1][1..nn2][1..nn3] (where nn1=1 for the case of a logically two-
// dimensional array). This routine returns (for isign=1) the complex fast
// Fourier transform as two complex arrays: On output, data contains the zero
// and positive frequency values of the third frequency component, while
// speq[1..nn1][1..2*nn2] contains the Nyquist critical frequency values of the
// third frequency component. First (and second) frequency components are
// stored for zero, positive, and negative frequencies, in standard wrap-around
// order. See Numerical Recipes for description of how complex values are
// arranged. For isign=-1, the inverse transform (times nn1*nn2*nn3/2 as a
// constant multiplicative factor) is performed, with output data (viewed as
// real array) deriving from input data (viewed as complex) and speq. For
// inverse transforms on data not generated first by a forward transform, make
// sure the complex input data array satisfies property 12.5.2 from NR. The
// dimensions nn1, nn2, nn3 must always be integer powers of 2.

	private void rlft3(D3Tensor data,DMatrix speq,/*long*/int nn1, /*long*/int nn2,
			/*long*/int nn3,int isign)
	{
		double theta,wi,wpi,wpr,wr,wtemp;
		float c1,c2,h1r,h1i,h2r,h2i;
		int /*long*/ i1,i2,i3,j1,j2,j3,ii3;
		/*long*/ int[] nn = new int/*long*/[4];

		if (data.getElementsCount() != nn1*nn2*nn3)
		{
			System.err.println(
			"rlft3: problem with dimensions or contiguity of data array\n");
			System.exit(1);
		}
		c1 = 0.5f;
		c2 = -0.5f*isign;
		theta = 2*isign*(PI/nn3);
		wtemp = (float)Math.sin(0.5*theta);
		wpr = -2.0*wtemp*wtemp;
		wpi = (float)Math.sin(theta);
		nn[1] = nn1;
		nn[2] = nn2;
		nn[3] = nn3 >> 1;

		// Case of forward transform. Here is where most all of the compute time is
		// spent. Extend data periodically into speq.

		if (isign == 1)
		{
			data.setOffset(0,0,-1); //1,1,0 is actually 0,0,-1
			fourn(data /*&data[1][1][1]-1*/,nn,3,isign); //MUST CHANGE (CHECK)!
			data.setOffset(0,0,+1);
			for (i1=1; i1<=nn1; i1++)
				for (i2=1, j2=0; i2<=nn2; i2++)
				{
					speq.setElement((int)i1,(int)++j2, data.getElement((int)i1,(int)i2,1) );
					speq.setElement((int)i1,(int)++j2, data.getElement((int)i1,(int)i2,2));
				}
		}
		for (i1=1; i1<=nn1; i1++)
		{
			// Zero frequency is its own reflection; otherwise locate corresponding
			// negative frequency in wrap-around order.

			j1 = (i1 != 1 ? nn1-i1+2 : 1);

			// Initialize trigonometric recurrence.

			wr = 1.0;
			wi = 0.0;
			for (ii3=1, i3=1; i3<=(nn3>>2)+1; i3++,ii3+=2)
			{
				for (i2=1; i2<=nn2; i2++)
				{
					if (i3 == 1)
					{
						j2 = (i2 != 1 ? ((nn2-i2)<<1)+3 : 1);
						h1r = c1*(data.getElement((int)i1,(int)i2,1)+speq.getElement((int)j1,(int) j2));
						h1i = c1*(data.getElement((int)i1,(int)i2,2)-speq.getElement((int)j1,(int)j2+1));
						h2i = c2*(data.getElement((int)i1,(int)i2,1)-speq.getElement((int)j1,(int)j2));
						h2r = -c2*(data.getElement((int)i1,(int)i2,2)+speq.getElement((int)j1,(int)j2+1));
						data.setElement((int)i1,(int)i2,1, h1r+h2r);
						data.setElement((int)i1,(int)i2,2, h1i+h2i);
						speq.setElement((int)j1,(int)j2, h1r-h2r);
						speq.setElement((int)j1, (int)j2+1, h2i-h1i);
					}
					else
					{
						j2 = (i2 != 1 ? nn2-i2+2 : 1);
						j3 = nn3+3-(i3<<1);
						h1r = c1*(data.getElement((int)i1,(int)i2,(int)ii3)+data.getElement((int)j1,(int)j2,(int)j3));
						h1i = c1*(data.getElement((int)i1,(int)i2,(int)ii3+1)-data.getElement((int)j1,(int)j2,(int)j3+1));
						h2i = c2*(data.getElement((int)i1,(int)i2,(int)ii3)-data.getElement((int)j1,(int)j2,(int)j3));
						h2r = -c2*(data.getElement((int)i1,(int)i2,(int)ii3+1)+data.getElement((int)j1,(int)j2,(int)j3+1));
						data.setElement((int)i1,(int)i2,(int)ii3  , (float)(h1r+wr*h2r-wi*h2i));
						data.setElement((int)i1,(int)i2,(int)ii3+1, (float)(h1i+wr*h2i+wi*h2r));
						data.setElement((int)j1,(int)j2,(int)j3   , (float)(h1r-wr*h2r+wi*h2i));
						data.setElement((int)j1,(int)j2,(int)j3+1 , (float)(-h1i+wr*h2i+wi*h2r));					}
				}

				// Do the recurrence.

				wr = (wtemp=wr)*wpr-wi*wpi+wr;
				wi = wi*wpr+wtemp*wpi+wi;
			}
		}

		// Case of reverse transform.

		if (isign == -1){
			//TODO check about the "-1"
			data.setOffset(0,0,-1);
			fourn(data /*&data[1][1][1]-1*/,nn,3,isign); //MUST CHANGE (CHECK)
			data.setOffset(0,0,+1);
		}
	}

// Function to perform Gaussian blur.
private void gaussianblur()
{
  D3Tensor blur,conv,pop;
  DMatrix speqblur,speqconv,speqpop;
  int i,j,k,p,q;

  blur = new D3Tensor(1,1,1,lx,1,ly);
  conv = new D3Tensor(1,1,1,lx,1,ly);
  pop = new D3Tensor(1,1,1,lx,1,ly);
  speqblur = new DMatrix(1,1,1,2*lx);
  speqconv = new DMatrix(1,1,1,2*lx);
  speqpop = new DMatrix(1,1,1,2*lx);

  // Fill population and convolution matrix.
  for(k = 0; k<= lz; k++){
  for (i=1; i<=lx; i++) for (j=1; j<=ly; j++)
    {
      if (i > lx/2) p = i-1-lx;
      else p = i-1;
      if (j > ly/2) q = j-1-ly;
      else q = j-1;
      pop.setElement(1,i,j, rho_0[k].array[i-1][j-1] );
      conv.setElement(1,i,j, (float)(0.5*
      	(erf((p+0.5)/(Math.sqrt(2.0)*(blurWidth*Math.pow(blurWidthFactor,nblurs))))-
	 erf((p-0.5)/(Math.sqrt(2.0)*(blurWidth*Math.pow(blurWidthFactor,nblurs)))))*
	(erf((q+0.5)/(Math.sqrt(2.0)*(blurWidth*Math.pow(blurWidthFactor,nblurs))))-
	 erf((q-0.5)/(Math.sqrt(2.0)*(blurWidth*Math.pow(blurWidthFactor,nblurs)))))/(lx*ly)));
    }

    // Fourier transform.

  rlft3(pop,speqpop,1,lx,ly,1);
  rlft3(conv,speqconv,1,lx,ly,1);

  // Multiply pointwise.

  for (i=1; i<=lx; i++)
    for (j=1; j<=ly/2; j++)
      {
	blur.setElement(1,i,2*j-1 ,
	  pop.getElement(1,i,2*j-1)*conv.getElement(1,i,2*j-1)-
	  pop.getElement(1,i,2*j)*conv.getElement(1,i,2*j) );
	blur.setElement(1,i,2*j,
	  pop.getElement(1,i,2*j)*conv.getElement(1,i,2*j-1)+
	  pop.getElement(1,i,2*j-1)*conv.getElement(1,i,2*j ));
      }
  for (i=1; i<=lx; i++)
    {
      speqblur.setElement(1,2*i-1,
	speqpop.getElement(1,2*i-1)*speqconv.getElement(1,2*i-1)-
	speqpop.getElement(1,2*i)*speqconv.getElement(1,2*i));
      speqblur.setElement(1,2*i,
	speqpop.getElement(1,2*i)*speqconv.getElement(1,2*i-1)+
	speqpop.getElement(1,2*i-1)*speqconv.getElement(1,2*i));
    }

  // Backtransform.

  rlft3(blur,speqblur,1,lx,ly,-1);

  // Write to rho_0.

  for (i=1; i<=lx; i++) for (j=1; j<=ly; j++) rho_0[k].array[i-1][j-1] = blur.getElement(1,i,j);
  }
  
  blur = new D3Tensor(1,1,1,lz,1,ly);
  conv = new D3Tensor(1,1,1,lz,1,ly);
  pop = new D3Tensor(1,1,1,lz,1,ly);
  speqblur = new DMatrix(1,1,1,2*lz);
  speqconv = new DMatrix(1,1,1,2*lz);
  speqpop = new DMatrix(1,1,1,2*lz);

  // Fill population and convolution matrix.
  for(k = 0; k<= lx; k++){
  for (i=1; i<=lz; i++) for (j=1; j<=ly; j++)
    {
      if (i > lz/2) p = i-1-lx;
      else p = i-1;
      if (j > ly/2) q = j-1-ly;
      else q = j-1;
      pop.setElement(1,i,j, rho_0[i-1].array[k][j-1] );
      conv.setElement(1,i,j, (float)(0.5*
      	(erf((p+0.5)/(Math.sqrt(2.0)*(blurWidth*Math.pow(blurWidthFactor,nblurs))))-
	 erf((p-0.5)/(Math.sqrt(2.0)*(blurWidth*Math.pow(blurWidthFactor,nblurs)))))*
	(erf((q+0.5)/(Math.sqrt(2.0)*(blurWidth*Math.pow(blurWidthFactor,nblurs))))-
	 erf((q-0.5)/(Math.sqrt(2.0)*(blurWidth*Math.pow(blurWidthFactor,nblurs)))))/(lz*ly)));
    }

    // Fourier transform.

  rlft3(pop,speqpop,1,lz,ly,1);
  rlft3(conv,speqconv,1,lz,ly,1);

  // Multiply pointwise.

  for (i=1; i<=lz; i++)
    for (j=1; j<=ly/2; j++)
      {
	blur.setElement(1,i,2*j-1 ,
	  pop.getElement(1,i,2*j-1)*conv.getElement(1,i,2*j-1)-
	  pop.getElement(1,i,2*j)*conv.getElement(1,i,2*j) );
	blur.setElement(1,i,2*j,
	  pop.getElement(1,i,2*j)*conv.getElement(1,i,2*j-1)+
	  pop.getElement(1,i,2*j-1)*conv.getElement(1,i,2*j ));
      }
  for (i=1; i<=lz; i++)
    {
      speqblur.setElement(1,2*i-1,
	speqpop.getElement(1,2*i-1)*speqconv.getElement(1,2*i-1)-
	speqpop.getElement(1,2*i)*speqconv.getElement(1,2*i));
      speqblur.setElement(1,2*i,
	speqpop.getElement(1,2*i)*speqconv.getElement(1,2*i-1)+
	speqpop.getElement(1,2*i-1)*speqconv.getElement(1,2*i));
    }

  // Backtransform.

  rlft3(blur,speqblur,1,lz,ly,-1);

  // Write to rho_0.

  for (i=1; i<=lz; i++) for (j=1; j<=ly; j++) rho_0[i].array[k][j-1] = blur.getElement(1,i,j);
  }
  

}

// Function to initialize rho_0. The original density is blurred with width
// SIGMA*pow(SIGMAFAC,nblurs).
private void initcond()
{
  float maxpop;
  int i,j,k;

  // Reconstruct population density.

  coscosft(rho_0,-1,-1);

  // There must not be negative densities.

  for(k=0; k<lz; k++) for (i=0; i<lx; i++) for (j=0; j<ly; j++) if (rho_0[k].array[i][j]<-1e10)
    {
      System.err.println("ERROR: Negative density in DENSITYFILE.");
      System.exit(1);
    }

  // Perform Gaussian blur.

//  logger.finest("Gaussian blur ...\n");
  gaussianblur();

  // Find the mimimum density. If it is very small suggest an increase in
  // SIGMA.

  minpop = rho_0[0].array[0][0];
  maxpop = rho_0[0].array[0][0];
  for(k=0; k<lz; k++) for (i=0; i<lx; i++) for (j=0; j<ly; j++) if (rho_0[k].array[i][j]<minpop)
    minpop = rho_0[k].array[i][j];
  for(k=0; k<lz; k++) for (i=0; i<lx; i++) for (j=0; j<ly; j++) if (rho_0[k].array[i][j]>maxpop)
    maxpop = rho_0[k].array[i][j];
  if (0<minpop && minpop<1e-8*maxpop)
    {
      //System.err.println("Minimimum population very small ("+minpop+"). Integrator");
      //System.err.println(
	//      "will probably converge very slowly. You can speed up the");
      //System.err.println("process by increasing SIGMA to a value > "+
	//		blurWidth*Math.pow(blurWidthFactor,nblurs));
    }

  
  
  // Replace rho_0 by cosine Fourier transform in both variables.

  coscosft(rho_0,1,1);
}

// Function to calculate the velocity field

//NO OBJECT REFERENCE
private void calcv(float t)
{
  int j,k,i;

  // Fill rho with Fourier coefficients.

  for (i=0; i<=lz; i++)for (j=0; j<=lx; j++) for (k=0; k<=ly; k++)
    rho[i].array[j][k] = (float)Math.exp(-((PI*j/lx)*(PI*j/lx)+(PI*k/ly)*(PI*k/ly)+(PI*i/lz)*(PI*i/lz))*t)*rho_0[i].array[j][k];

  // Calculate the Fourier coefficients for the partial derivative of rho.
  // Store temporary results in arrays gridvx, gridvy.

   for (i=0; i<=lz; i++) for (j=0; j<=lx; j++) for (k=0; k<=ly; k++)
    {
      gridvx[i].array[j][k] = (float)-(PI*j/lx)*rho[i].array[j][k];
      gridvy[i].array[j][k] = (float)-(PI*k/ly)*rho[i].array[j][k];
      gridvz[i].array[j][k] = (float)-(PI*i/lz)*rho[i].array[j][k];
    }

  // Replace rho by cosine Fourier backtransform in both variables.

  coscosft(rho,-1,-1);

  // Replace vx by sine Fourier backtransform in the first and cosine Fourier
  // backtransform in the second variable.

  sincosft(gridvx,-1,-1);

  // Replace vy by cosine Fourier backtransform in the first and sine Fourier
  // backtransform in the second variable.

  cossinft(gridvy,-1,-1);
  
  
  
  sinzft(gridvz,-1,-1);

  // Calculate the velocity field.

  for (i=0; i<=lz; i++)for (j=0; j<=lx; j++) for (k=0; k<=ly; k++)
    {
      gridvx[i].array[j][k] = -gridvx[i].array[j][k]/rho[i].array[j][k];
      gridvy[i].array[j][k] = (!zaxis) ? -gridvy[i].array[j][k]/rho[i].array[j][k] : 0;
      gridvz[i].array[j][k] = 0;//(zaxis) ? -gridvz[i].array[j][k]/rho[i].array[j][k] : 0;
    }
}


// Function to bilinearly interpolate a two-dimensional array. For higher
// accuracy one could consider higher order interpolation schemes, but that
// will make the program slower.

//NO OBJECT REFERENCE
private float intpol(ArrayFloat[] arr,float x,float y, float z)
{
  int gaussx,gaussy,gaussz;
  float deltax,deltay,deltaz;

  // Decompose x and y into an integer part and a decimal.
  //System.out.println(x+" "+y);
  gaussx = (int)x;
  gaussy = (int)y;
  gaussz = (int)z;
  deltax = x-gaussx;
  deltay = y-gaussy;
  deltaz = z-gaussz;
  // Interpolate.

  if (gaussx==lx && gaussy==ly && gaussz==lz)
    return arr[gaussz].array[gaussx][gaussy];
  if (gaussx==lx && gaussz == lz)
    return (1-deltay)*arr[gaussz].array[gaussx][gaussy]+deltay*arr[gaussz].array[gaussx][gaussy+1];
  if (gaussy==ly && gaussz == lz)
    return (1-deltax)*arr[gaussz].array[gaussx][gaussy]+deltax*arr[gaussz].array[gaussx+1][gaussy];
  if (gaussx==lx && gaussy == ly)
    return (1-deltaz)*arr[gaussz].array[gaussx][gaussy]+deltaz*arr[gaussz+1].array[gaussx][gaussy];
  if (gaussx==lx)
    return (1-deltay)*(1-deltaz)*arr[gaussz].array[gaussx][gaussy]+deltay*(1-deltaz)*arr[gaussz].array[gaussx][gaussy+1]+
            (deltaz)*(1-deltay)*arr[gaussz+1].array[gaussx][gaussy]+deltaz*deltay*arr[gaussz+1].array[gaussx][gaussy+1];
  if (gaussy==ly)
    return (1-deltax)*(1-deltaz)*arr[gaussz].array[gaussx][gaussy]+deltax*(1-deltaz)*arr[gaussz].array[gaussx+1][gaussy]+
            (deltaz)*(1-deltax)*arr[gaussz+1].array[gaussx][gaussy]+deltaz*deltax*arr[gaussz+1].array[gaussx+1][gaussy];
  if (gaussz==lz)
    return (1-deltax)*(1-deltay)*arr[gaussz].array[gaussx][gaussy]+deltax*(1-deltay)*arr[gaussz].array[gaussx+1][gaussy]+
            (deltay)*(1-deltax)*arr[gaussz].array[gaussx][gaussy+1]+deltay*deltax*arr[gaussz].array[gaussx+1][gaussy+1];
  return (1-deltax)*(1-deltay)*(1-deltaz)*arr[gaussz].array[gaussx][gaussy]+
    (1-deltax)*deltay*(1-deltaz)*arr[gaussz].array[gaussx][gaussy+1]+
    deltax*(1-deltay)*(1-deltaz)*arr[gaussz].array[gaussx+1][gaussy]+
    deltax*deltay*(1-deltaz)*arr[gaussz].array[gaussx+1][gaussy+1]+
    (1-deltax)*(1-deltay)*(deltaz)*arr[gaussz+1].array[gaussx][gaussy]+
    (1-deltax)*deltay*(deltaz)*arr[gaussz+1].array[gaussx][gaussy+1]+
    deltax*(1-deltay)*(deltaz)*arr[gaussz+1].array[gaussx+1][gaussy]+
    deltax*deltay*(deltaz)*arr[gaussz+1].array[gaussx+1][gaussy+1];
}

// Function to find the root of the system of equations
// xappr-0.5*h*v_x(t+h,xappr,yappr)-x[j][k]-0.5*h*vx[j][k]=0,
// yappr-0.5*h*v_y(t+h,xappr,yappr)-y[j][k]-0.5*h*vy[j][k]=0
// with Newton-Raphson. Returns TRUE after sufficient convergence.


//MUST CHANGE - object reference - xappr and yappr
private boolean newt2(float h,float[] retxyAppr,float xguess,float yguess,float zguess,
	      int j,int k, int w)
{
  float deltax,deltay,deltaz=0, dfxdx,dfxdy,dfxdz,dfydx,dfydy,dfydz,dfzdx,dfzdy,dfzdz,fx,fy,fz;
  int gaussx,gaussxplus,gaussy,gaussyplus,gaussz, gausszplus, i;

  float xappr, yappr, zappr;
  // Initial guess.

  xappr = xguess;
  yappr = yguess;
  zappr = zguess;


  for (i=1; i<=IMAX; i++)
    {
      // fx, fy are the left-hand sides of the two equations. Find
      // v_x(t+h,xappr,yappr), v_y(t+h,xappr,yappr) by interpolation.
      if(xappr > lx+1 || yappr > ly+1 || zappr > lz+1) return false;
  
      fx = (float) (xappr-0.5*h*intpol(gridvx,xappr,yappr,zappr)-x[w].array[j][k]-0.5*h*vx[w].array[j][k]);
      fy = (float) (yappr-0.5*h*intpol(gridvy,xappr,yappr,zappr)-y[w].array[j][k]-0.5*h*vy[w].array[j][k]);
      fz = (float) (zappr-0.5*h*intpol(gridvz,xappr,yappr,zappr)-z[w].array[j][k]-0.5*h*vz[w].array[j][k]);
      // Linearly approximate the partial derivatives of fx, fy with a finite
      // difference method. More elaborate techniques are possible, but this
      // quick and dirty method appears to work reasonably for our purpose.

      gaussx = (int)(xappr);
      gaussy = (int)(yappr);
      gaussz = (int)(zappr);
      if (gaussx == lx) gaussxplus = 0;
      else gaussxplus = gaussx+1;
      if (gaussy == ly) gaussyplus = 0;
      else gaussyplus = gaussy+1;
      if(gaussz == lz) gausszplus = 0;
      else gausszplus = gaussz+1;
      
      deltax = x[w].array[j][k] - gaussx;
      deltay = y[w].array[j][k] - gaussy;
      deltaz = z[w].array[j][k] - gaussz;
      
      dfxdx = (float) (1 - 0.5*h*
	((1-deltay)*(gridvx[gaussz].array[gaussxplus][gaussy]-gridvx[gaussz].array[gaussx][gaussy])+
	 deltay*(gridvx[gaussz].array[gaussxplus][gaussyplus]-gridvx[gaussz].array[gaussx][gaussyplus])+
	 (1-deltaz)*(gridvx[gaussz].array[gaussxplus][gaussy]-gridvx[gaussz].array[gaussx][gaussy])+
	 deltaz*(gridvx[gausszplus].array[gaussxplus][gaussy]-gridvx[gausszplus].array[gaussx][gaussy])));
      dfxdy = (float) (-0.5*h*
	((1-deltax)*(gridvx[gaussz].array[gaussx][gaussyplus]-gridvx[gaussz].array[gaussx][gaussy])+
	 deltax*(gridvx[gaussz].array[gaussxplus][gaussyplus]-gridvx[gaussz].array[gaussxplus][gaussy])+
	 (1-deltaz)*(gridvx[gaussz].array[gaussx][gaussyplus]-gridvx[gaussz].array[gaussx][gaussy])+
	 deltaz*(gridvx[gausszplus].array[gaussx][gaussyplus]-gridvx[gausszplus].array[gaussxplus][gaussy])));
      dfxdz = (float) (-0.5*h*
	((1-deltax)*(gridvx[gausszplus].array[gaussx][gaussy]-gridvx[gaussz].array[gaussx][gaussy])+
	 deltax*(gridvx[gausszplus].array[gaussxplus][gaussyplus]-gridvx[gausszplus].array[gaussxplus][gaussy])+
	 (1-deltay)*(gridvx[gausszplus].array[gaussx][gaussy]-gridvx[gaussz].array[gaussx][gaussy])+
	 deltay*(gridvx[gaussz].array[gaussx][gaussyplus]-gridvx[gausszplus].array[gaussx][gaussyplus])));
      dfydx = (float) (-0.5*h*
	((1-deltay)*(gridvy[gaussz].array[gaussxplus][gaussy]-gridvy[gaussz].array[gaussx][gaussy])+
	 deltay*(gridvy[gaussz].array[gaussxplus][gaussyplus]-gridvy[gaussz].array[gaussx][gaussyplus])+
	 (1-deltaz)*(gridvy[gaussz].array[gaussxplus][gaussy]-gridvy[gaussz].array[gaussx][gaussy])+
	 deltaz*(gridvy[gausszplus].array[gaussxplus][gaussy]-gridvy[gausszplus].array[gaussx][gaussy])));
      dfydy = (float) (1 - 0.5*h*
	((1-deltax)*(gridvy[gaussz].array[gaussx][gaussyplus]-gridvy[gaussz].array[gaussx][gaussy])+
	 deltax*(gridvy[gaussz].array[gaussxplus][gaussyplus]-gridvy[gaussz].array[gaussxplus][gaussy])+
	 (1-deltaz)*(gridvy[gaussz].array[gaussx][gaussyplus]-gridvy[gaussz].array[gaussx][gaussy])+
	 deltaz*(gridvy[gausszplus].array[gaussx][gaussyplus]-gridvy[gausszplus].array[gaussxplus][gaussy])));
      dfydz = (float) (-0.5*h*
	((1-deltax)*(gridvy[gausszplus].array[gaussx][gaussy]-gridvy[gaussz].array[gaussx][gaussy])+
	 deltax*(gridvy[gausszplus].array[gaussxplus][gaussyplus]-gridvy[gausszplus].array[gaussxplus][gaussy])+
	 (1-deltay)*(gridvy[gausszplus].array[gaussx][gaussy]-gridvy[gaussz].array[gaussx][gaussy])+
	 deltay*(gridvy[gaussz].array[gaussx][gaussyplus]-gridvy[gausszplus].array[gaussx][gaussyplus])));
      dfzdz = (float) (1 - 0.5*h*
	((1-deltax)*(gridvz[gausszplus].array[gaussx][gaussy]-gridvz[gaussz].array[gaussx][gaussy])+
	 deltax*(gridvz[gausszplus].array[gaussxplus][gaussy]-gridvz[gaussz].array[gaussxplus][gaussy])+
	 (1-deltay)*(gridvz[gausszplus].array[gaussx][gaussy]-gridvz[gaussz].array[gaussx][gaussy])+
	 deltay*(gridvz[gausszplus].array[gaussx][gaussyplus]-gridvz[gaussz].array[gaussx][gaussyplus])));
      dfzdy = (float) (-0.5*h*
	((1-deltax)*(gridvz[gaussz].array[gaussx][gaussyplus]-gridvz[gaussz].array[gaussx][gaussy])+
	 deltax*(gridvz[gaussz].array[gaussxplus][gaussyplus]-gridvz[gaussz].array[gaussxplus][gaussy])+
	 (1-deltaz)*(gridvz[gaussz].array[gaussx][gaussyplus]-gridvz[gaussz].array[gaussx][gaussy])+
	 deltaz*(gridvz[gausszplus].array[gaussx][gaussyplus]-gridvz[gausszplus].array[gaussx][gaussy])));
      dfzdx = (float) (-0.5*h*
	((1-deltay)*(gridvz[gaussz].array[gaussxplus][gaussy]-gridvz[gaussz].array[gaussx][gaussy])+
	 deltax*(gridvz[gaussz].array[gaussxplus][gaussyplus]-gridvz[gaussz].array[gaussx][gaussyplus])+
	 (1-deltaz)*(gridvz[gaussz].array[gaussxplus][gaussy]-gridvz[gaussz].array[gaussx][gaussy])+
	 deltaz*(gridvz[gausszplus].array[gaussxplus][gaussy]-gridvz[gausszplus].array[gaussx][gaussy])));

      // If the current approximation is (xappr,yappr) for the zero of
      // (fx(x,y),fy(x,y)) and J is the Jacobian, then we can approximate (in
      // vector notation) for |delta|<<1:
      // f((xappr,yappr)+delta) = f(xappr,yappr)+J*delta.
      // Setting f((xappr,yappr)+delta)=0 we obtain a set of linear equations
      // for the correction delta which moves f closer to zero, namely
      // J*delta = -f.
      // The improved approximation is then x = xappr+delta.
      // The process will be iterated until convergence is reached.

      if ((fx*fx + fy*fy + fz*fz) < TOLF) {retxyAppr[0] = xappr; retxyAppr[1] = yappr; retxyAppr[2] = zappr; return true; }
      //deltax = (fy*dfxdy - fx*dfydy)/(dfxdx*dfydy - dfxdy*dfydx);
      //deltay = (fx*dfydx - fy*dfxdx)/(dfxdx*dfydy - dfxdy*dfydx);
      float jDeter = dfxdx*dfydy*dfzdz + dfxdy*dfydz*dfzdx + dfxdz*dfydx*dfzdy
                   - dfxdx*dfydz*dfzdy - dfxdy*dfydx*dfzdz - dfxdz*dfydy*dfzdx;
      
      deltax = -(fx*(dfydy*dfzdz - dfydz*dfzdy) + fy*(dfxdz*dfzdy - dfxdy*dfzdz) + fz*(dfxdy*dfydz - dfxdz*dfydy)) / jDeter;
      deltay = -(fx*(dfydz*dfzdx - dfydx*dfzdz) + fy*(dfxdx*dfzdz - dfxdz*dfzdx) + fz*(dfxdz*dfydx - dfxdx*dfydz)) / jDeter;
      deltaz = -(fx*(dfydx*dfzdy - dfydy*dfzdx) + fy*(dfxdy*dfzdx - dfxdx*dfzdy) + fz*(dfxdx*dfydy - dfxdy*dfydx)) / jDeter;      
       //     System.out.println(deltax+" "+deltay+" "+deltaz+" "+xappr+" "+yappr+" "+zappr);
      if ((deltax*deltax + deltay*deltay + deltaz*deltaz) < TOLX) {retxyAppr[0] = xappr; retxyAppr[1] = yappr; retxyAppr[2] = zappr; return true; }
      xappr += deltax;
      yappr += deltay;
      zappr += deltaz;

      //printf("deltax %f, deltay %f\n",deltax,deltay);
    }
  //System.err.println("newt2 failed, increasing sigma to "+
//	  blurWidth*Math.pow(blurWidthFactor,nblurs));
  retxyAppr[0] = xappr;
  retxyAppr[1] = yappr;
  retxyAppr[2] = zappr;
  return false;
}

// Function to integrate the nonlinear Volterra equation. Returns TRUE after
// the displacement field converged, after MAXINTSTEPS integration steps, or
// if the time exceeds TIMELIMIT.

//NO OBJECT REFERENCE
private boolean nonlinvoltra()
{
  boolean stepsize_ok;
  BufferedWriter displfile = null;
	if ( DISPLFILE != null ){
	  try {
		displfile = new BufferedWriter( new FileWriter(DISPLFILE) );
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
  float h,maxchange=0f,t,vxplus,vyplus, vzplus, xguess,yguess,zguess;
  int i,j,w,k;

  maxchange = Float.MAX_VALUE;
  
  do
    {
      initcond();
      nblurs++;
      /*if (minpop<0.0)
	System.out.println(
		"Minimum population negative, will increase sigma to "+
		blurWidth*Math.pow(blurWidthFactor,nblurs));*/
    }
  while (minpop<0.0);
  h = (float) HINITIAL;
  t = 0; // Start at time t=0.

  // (x[j][k],y[j][k]) is the position for the element that was at position
  // (j,k) at time t=0.

  for (w=0; w<=lz; w++)for (j=0; j<=lx; j++) for (k=0; k<=ly; k++)
    {
      x[w].array[j][k] = j;
      y[w].array[j][k] = k;
      z[w].array[j][k] = w;
    }
  calcv( 0.0f );

  
          
                /* for(int b = 0; b <= ly; b++){
                        for(int c = 0; c <= lx; c++){
                            System.out.print(rho_0[0].array[c][b]+" ");
                        }
                        System.out.print("\n");
                    }
                    System.out.println("*********************************");
                    System.exit(1);*/
  
  // (gridvx[j][k],gridvy[j][k]) is the velocity at position (j,k).
  // (vx[j][k],vy[j][k]) is the velocity at position (x[j][k],y[j][k]).
  // At t=0 they are of course identical.

  for (w=0; w<=lz; w++)for (j=0; j<=lx; j++) for (k=0; k<=ly; k++)
    {
      vx[w].array[j][k] = gridvx[w].array[j][k];
      vy[w].array[j][k] = gridvy[w].array[j][k];
      vz[w].array[j][k] = gridvz[w].array[j][k];
     // System.out.println(vx[w].array[j][k]+" "+vy[w].array[j][k]+" "+vz[w].array[j][k]+" "+j+" "+k+" "+w+" "+rho_0[w].array[j][k]);
    }
  i = 1; // i counts the integration steps.

  // Here is the integrator.

  do
    {
      stepsize_ok = true;
      calcv(t+h);
     // System.out.println(t+" "+h);
       for (w=0; w<=lz; w++) for (j=0; j<=lx; j++) for (k=0; k<=ly; k++)
	{
	  // First take a naive integration step. The velocity at time t+h for
	  // the element [j][k] is approximately
	  // v(t+h,x[j][k]+h*vx[j][k],y[j][k]+h*vy[j][k]).
	  // The components, call them vxplus and vyplus, are interpolated from
	  // gridvx and gridvy.
          //System.out.println(vx[w].array[j][k]+" "+vy[w].array[j][k]+" "+vz[w].array[j][k]+" "+j+" "+k+" "+w);
	  vxplus = intpol(gridvx,x[w].array[j][k]+h*vx[w].array[j][k],y[w].array[j][k]+h*vy[w].array[j][k],z[w].array[j][k]+h*vz[w].array[j][k]);
	  vyplus = intpol(gridvy,x[w].array[j][k]+h*vx[w].array[j][k],y[w].array[j][k]+h*vy[w].array[j][k],z[w].array[j][k]+h*vz[w].array[j][k]);
          vzplus = intpol(gridvz,x[w].array[j][k]+h*vx[w].array[j][k],y[w].array[j][k]+h*vy[w].array[j][k],z[w].array[j][k]+h*vz[w].array[j][k]);
          //System.out.println(vxplus+" "+vyplus+" "+vzplus+" "+j+" "+k+" "+w);
	  // Based on (vx[j][k],vy[j][k]) and (vxplus,vyplus) we expect the
	  // new position at time t+h to be:

	  xguess = (float)(x[w].array[j][k] + 0.5*h*(vx[w].array[j][k]+vxplus));
	  yguess = (float)(y[w].array[j][k] + 0.5*h*(vy[w].array[j][k]+vyplus));
          zguess = (float)(z[w].array[j][k] + 0.5*h*(vz[w].array[j][k]+vzplus));

	  // Then we make a better approximation by solving the two nonlinear
	  // equations:
	  // xappr[j][k]-0.5*h*v_x(t+h,xappr[j][k],yappr[j][k])-
	  // x[j][k]-0.5*h*vx[j][k]=0,
	  // yappr[j][k]-0.5*h*v_y(t+h,xappr[j][k],yappr[j][k])-
	  // y[j][k]-0.5*h*vy[j][k]=0
	  // with Newton-Raphson and (xguess,yguess) as initial guess.
	  // If newt2 fails to converge, exit nonlinvoltra.

	  float[] ret = new float[]{xappr[w].array[j][k],yappr[w].array[j][k],zappr[w].array[j][k]};
	  boolean result = newt2(h,ret,xguess,yguess,zguess,j,k,w);
	   xappr[w].array[j][k] = ret[0];
           yappr[w].array[j][k] = ret[1];
           zappr[w].array[j][k] = ret[2];
	  if (!result){
            //  System.out.println("NO RESULT!");
               return false;
          }
	   

	  // If the integration step was too large reduce the step size.

	  if ((xguess-xappr[w].array[j][k])*(xguess-xappr[w].array[j][k])+
	      (yguess-yappr[w].array[j][k])*(yguess-yappr[w].array[j][k])+
	      (zguess-zappr[w].array[j][k])*(zguess-zappr[w].array[j][k]) > TOLINT)
	    {
	      if (h<MINH)
		{
		  /*System.out.println(
			  "Time step below "+h+", increasing SIGMA to "+
			  blurWidth*Math.pow(blurWidthFactor,nblurs));*/
		  nblurs++;
		  return false;
		}
	      h /= 10;
           //   System.out.println("Reduce Step Size");
	      stepsize_ok = false;
	      break;
	    }
	}
    //  System.out.println("here "+stepsize_ok+" "+h+" "+i+" "+MAXINTSTEPS+" "+t+" "+TIMELIMIT+" "+maxchange+" "+CONVERGENCE);
      if (!stepsize_ok){continue;}
      else
	{
	  t += h;
	  maxchange = 0.0f; // Monitor the maximum change in positions.
	  for (w=0; w<=lz; w++) for (j=0; j<=lx; j++) for (k=0; k<=ly; k++)
	    {
	      if ((x[w].array[j][k]-xappr[w].array[j][k])*(x[w].array[j][k]-xappr[w].array[j][k])+
		  (y[w].array[j][k]-yappr[w].array[j][k])*(y[w].array[j][k]-yappr[w].array[j][k])+
		  (z[w].array[j][k]-zappr[w].array[j][k])*(z[w].array[j][k]-zappr[w].array[j][k]) > maxchange)
		maxchange =
		  (x[w].array[j][k]-xappr[w].array[j][k])*(x[w].array[j][k]-xappr[w].array[j][k])+
		  (y[w].array[j][k]-yappr[w].array[j][k])*(y[w].array[j][k]-yappr[w].array[j][k])+
		  (z[w].array[j][k]-zappr[w].array[j][k])*(z[w].array[j][k]-zappr[w].array[j][k]);
	      x[w].array[j][k] = xappr[w].array[j][k];
	      y[w].array[j][k] = yappr[w].array[j][k];
              z[w].array[j][k] = zappr[w].array[j][k];
              //System.out.println(xappr[w].array[j][k]+" "+yappr[w].array[j][k]+" "+zappr[w].array[j][k]);
	      vx[w].array[j][k] = intpol(gridvx,xappr[w].array[j][k],yappr[w].array[j][k],zappr[w].array[j][k]);
	      vy[w].array[j][k] = intpol(gridvy,xappr[w].array[j][k],yappr[w].array[j][k],zappr[w].array[j][k]);
              vz[w].array[j][k] = intpol(gridvz,xappr[w].array[j][k],yappr[w].array[j][k],zappr[w].array[j][k]);
	    }
	}
      h *= 1.2; // Make the next integration step larger.
/*      if (logger.isLoggable(Level.FINEST)){
          if (i % 10 == 0) logger.finest("time " + t);
      }*/
      //if (i % 10 == 0) System.out.println("time "+t);
      i++;
    } while (i<MAXINTSTEPS && t<TIMELIMIT && maxchange>CONVERGENCE);
 /// System.out.println(i+" "+MAXINTSTEPS+" "+t+" "+TIMELIMIT+" "+maxchange+" "+CONVERGENCE);
 // System.out.println("BUSTED");
  if (maxchange>CONVERGENCE)
    System.err.println(
	    "WARNING: Insufficient convergence within "+MAXINTSTEPS+" steps, time "+TIMELIMIT);

  if (DISPLFILE != null){

  // Write displacement field to file.
  try {
  displfile.write("time "+t+
                  "\nminx "+minx+
                  "\nmaxx "+maxx+
                  "\nminy "+miny+
                  "\nmaxy "+maxy+"\n");
  displfile.write("sigma "+blurWidth*Math.pow(blurWidthFactor,nblurs-1)+"\n");
  displfile.write("background 0\nlx\nly\n\n"); //,0,lx,ly); --> warning, lx and ly are not being display in the originial c program
  for (j=0; j<=lx; j++) for (k=0; k<=ly; k++)
    displfile.write("j "+j+", k "+k+", x "+x[j].array[k]+", y "+y[j].array[k]+"\n");

	displfile.close();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
}

  return true;
}




// Function to transform points according to displacement field.

//NO OBJECT REFERENCE
Point transf(Point p)
{
	float deltax,deltay,deltaz,den,t,u;
	int gaussx,gaussy,gaussz;
	Point a = new Point(),
	      b = new Point(),
		  c = new Point(),
		  d = new Point(),
                  g = new Point(),
                  i = new Point(),
		  j = new Point(),
		  ptr = new Point();

	p.x = (p.x-minx)*lx/(maxx-minx);
	p.y = (p.y-miny)*ly/(maxy-miny);
        p.z = (p.z-minz)*lz/(maxz-minz);
  
	gaussx = (int)p.x;
	gaussy = (int)p.y;
        gaussz = (int)p.z;
        if(gaussx >= lx) gaussx = lx-1;
        if(gaussy >= ly) gaussy = ly-1;
        if(gaussz >= lz) gaussz = lz-1;
        
	if (gaussx<0 || gaussx>lx || gaussy<0 || gaussy>ly)
	{
                return p;
		//System.err.println("ERROR: Coordinate limits exceeded in transf.\n");
		//System.exit(1);
	}
	deltax = p.x - gaussx;
	deltay = p.y - gaussy;
        deltaz = p.z - gaussz;

	// The transformed point is the intersection of the lines:
	// (I) connecting
	//     (1-deltax)*(x,y[gaussx][gaussy])+deltax*(x,y[gaussx+1][gaussy])
	//     and
	//     (1-deltax)*(x,y[gaussx][gaussy+1])+deltax*(x,y[gaussx+1][gaussy+1])
	// (II) connecting
	//     (1-deltay)*(x,y[gaussx][gaussy])+deltay*(x,y[gaussx][gaussy+1])
	//     and
	//     (1-deltay)*(x,y[gaussx+1][gaussy])+deltay*(x,y[gaussx+1][gaussy+1]).
	// Call these four points a, b, c and d.
     //   if(zaxis) System.out.println(gaussx+" "+gaussy+" "+gaussz+" "+x[gaussz].length);
	a.x = (1-deltax)*x[gaussz].array[gaussx][gaussy] + deltax*x[gaussz].array[gaussx+1][gaussy];
	a.y = (1-deltax)*y[gaussz].array[gaussx][gaussy] + deltax*y[gaussz].array[gaussx+1][gaussy];
	b.x = (1-deltax)*x[gaussz].array[gaussx][gaussy+1] + deltax*x[gaussz].array[gaussx+1][gaussy+1];
	b.y = (1-deltax)*y[gaussz].array[gaussx][gaussy+1] + deltax*y[gaussz].array[gaussx+1][gaussy+1];
	c.x = (1-deltay)*x[gaussz].array[gaussx][gaussy] + deltay*x[gaussz].array[gaussx][gaussy+1];
	c.y = (1-deltay)*y[gaussz].array[gaussx][gaussy] + deltay*y[gaussz].array[gaussx][gaussy+1];
        c.z = (1-deltay)*z[gaussz].array[gaussx][gaussy] + deltay*z[gaussz].array[gaussx][gaussy+1];
	d.x = (1-deltay)*x[gaussz].array[gaussx+1][gaussy] + deltay*x[gaussz].array[gaussx+1][gaussy+1];
	d.y = (1-deltay)*y[gaussz].array[gaussx+1][gaussy] + deltay*y[gaussz].array[gaussx+1][gaussy+1];        
	g.y = (1-deltay)*y[gaussz+1].array[gaussx][gaussy] + deltay*y[gaussz+1].array[gaussx][gaussy+1];
        g.z = (1-deltay)*z[gaussz+1].array[gaussx][gaussy] + deltay*z[gaussz+1].array[gaussx][gaussy+1];        
	i.y = (1-deltaz)*y[gaussz].array[gaussx][gaussy] + deltaz*y[gaussz+1].array[gaussx][gaussy];
        i.z = (1-deltaz)*z[gaussz].array[gaussx][gaussy] + deltaz*z[gaussz+1].array[gaussx][gaussy];	
	j.y = (1-deltaz)*y[gaussz].array[gaussx][gaussy+1] + deltaz*y[gaussz+1].array[gaussx][gaussy+1];
        j.z = (1-deltaz)*z[gaussz].array[gaussx][gaussy+1] + deltaz*z[gaussz+1].array[gaussx][gaussy+1];

	

	// Solve the vector equation a+t(b-a) = c+u(d-c) for the scalars t, u.

	den=(b.x-a.x)*(c.y-d.y)+(a.y-b.y)*(c.x-d.x);
	t = ((c.x-a.x)*(c.y-d.y)+(a.y-c.y)*(c.x-d.x))/den;
	u = ((b.x-a.x)*(c.y-a.y)+(a.y-b.y)*(c.x-a.x))/den;

        float den2 = (j.z-i.z)*(c.y-g.y)+(i.y-j.y)*(c.z-g.z);
        float t2 = ((c.z-i.z)*(c.y-g.y)+(i.y-c.y)*(c.z-g.z))/den2;
 
	
	//if (t<-1e-3|| t>1+1e-3 || u<-1e-3 || u>1+1e-3)
		//System.err.println("WARNING: Transformed area element non-convex.\n");
	ptr.x = (1-(a.x+t*(b.x-a.x))/lx)*minx + ((a.x+t*(b.x-a.x))/lx)*maxx;
	ptr.y = (1-(a.y+t*(b.y-a.y))/ly)*miny + ((a.y+t*(b.y-a.y))/ly)*maxy;
        ptr.z = (1-(i.z+t2*(j.z-i.z))/lz)*minz + ((i.z+t2*(j.z-i.z))/lz)*maxz;
        //ptr.z = p.z;//(1-a.z/lz)*minz + (a.z/lz)*maxz;
        //System.out.println(ptr.z+" "+p.z+" "+i.z+" "+j.z+" "+t2+" "+den2);
	return ptr;
}
// Function to read spatial features from user-specified file and map to
// cartogram.
private void cartogram() 
{
   // try{
	String id,line;
	BufferedReader infile;
	//BufferedWriter outfile;
	float xcoord,ycoord,zcoord;
	Point p = new Point();
        output = new float[npoly*4][3];
        ps = new StringBuffer();
        
	//infile = new BufferedReader(new StringReader(polygons));
        int itor = 0, count = 0;
        //System.out.println(polygons);
	//outfile = FileTools.openFileWrite(genFileName);
	 for(int h = 0; h < polygons.length; h++){
                    
                float[][] poly = (float[][]) polygons[h].get(0);
                
		for(int k = 0; k < poly.length; k++, itor++)
		{
			
			xcoord = poly[k][0];
			ycoord = poly[k][1];
                        zcoord = poly[k][2];
            
                        p.x = xcoord;
			p.y = ycoord;
                        p.z = zcoord;
			p = transf(p);
                        
                        if(!zaxis){                          
                            if(twoD) {
                                output[itor][0] = p.x;
                                output[itor][1] = p.y;
                                output[itor][2] = 1;
                            }else{
                                xy[itor][0] = p.x;
                                xy[itor][1] = p.y; 
                            }
                        }else{
                            output[itor][0] = xy[itor][0];
                            output[itor][1] = xy[itor][1];
                            output[itor][2] = p.x;
                        }
  
                    } 
		
                                            

   
                }
	
        /*if(!zaxis) {
            output.append("END\n");
            polygons = output.toString();
        }*/
       // ps.append("END\n");
       // System.out.println("****\n"+output.toString()+"\n****");
	//infile.close();
	//outfile.close();
   // }catch(IOException err){};
}


// Function to prepare a map in postscript standard letter format.



 public DEC(ArrayList[] polygons, float[] census, float[] zcensus, ArrayList[] zpolygons, boolean twoD, boolean indep, int xyres, int zres){
     this.polygons = polygons;
     this.census = census;
     this.zcensus = zcensus;
     this.zpolygons = zpolygons;
     this.twoD = twoD;
     this.indep = indep;
     this.xyres = xyres;
     this.zres = zres;
    // System.out.println(xyres+" "+zres);
       //this.makeCartogram();
}

/*public DEC_2(String genFile, String datFile, String polygonFile) throws IOException{
    this.polygonFileName = polygonFile;
    this.genFileName = genFile;
    this.dataFileName = datFile;
    this.makeCartogram();
}*/

 public float[][] makeCartogram(){

    maxNSquareLog = (int)((Math.log(xyres) / Math.log(2)) / .5);

    for(int q = 0; q < ((indep) ? 1 : 2); q++){

    boolean n;

    int i;

    // Read the polygon coordinates.

    readcorn();

    // Allocate memory for arrays.
    gridvz = new ArrayFloat[lz+1];
    gridvx = new ArrayFloat[lz+1];
    gridvy = new ArrayFloat[lz+1];
    rho =    new ArrayFloat[lz+1];
    rho_0 =  new ArrayFloat[lz+1];
    vx =     new ArrayFloat[lz+1];
    vy =     new ArrayFloat[lz+1];
    vz =     new ArrayFloat[lz+1];
    x =      new ArrayFloat[lz+1];
    xappr =  new ArrayFloat[lz+1];
    y =      new ArrayFloat[lz+1];
    yappr =  new ArrayFloat[lz+1];
    z =      new ArrayFloat[lz+1];
    zappr =  new ArrayFloat[lz+1];
    arrayLength = x.length;

    for(i = 0; i<=lz; i++){
            gridvx[i] = new ArrayFloat(lx+1, ly+1);
            gridvy[i] = new ArrayFloat(lx+1, ly+1);
            gridvz[i] = new ArrayFloat(lx+1, ly+1);
            rho[i] = new ArrayFloat(lx+1, ly+1);
            rho_0[i] = new ArrayFloat(lx+1, ly+1);
            vx[i] = new ArrayFloat(lx+1, ly+1);
            vy[i] = new ArrayFloat(lx+1, ly+1);
            vz[i] = new ArrayFloat(lx+1, ly+1);
            x[i] = new ArrayFloat(lx+1, ly+1);
            xappr[i] = new ArrayFloat(lx+1, ly+1);
            y[i] = new ArrayFloat(lx+1, ly+1);
            yappr[i] = new ArrayFloat(lx+1, ly+1);
            z[i] = new ArrayFloat(lx+1, ly+1);
            zappr[i] = new ArrayFloat(lx+1, ly+1);
    }

    // Digitize the density.
    
 
    digdens();
    
   
    // Solve the diffusion equation.

    do n = nonlinvoltra(); while (!n);
   // zaxis = true;
    
  
    cartogram();
    
    if(twoD) return output;
    
  /* if(zaxis){
    try{
        //System.out.println(polygons);
        BufferedReader infile = new BufferedReader(new StringReader(ps.toString()));
        BufferedWriter outfile = FileTools.openFileWrite(MAP2PS);
        pspicture(infile, outfile);
        infile.close();
        outfile.close();
    }catch(IOException err){};
    }*/
    

    census = zcensus;
    polygons = zpolygons;
    zaxis = true;
    
    maxNSquareLog = (int)((Math.log(zres) / Math.log(2)) / .5);


    }
        
        return output;

   }
 
 


    public void setBlurWidth(double blurWidth) {
        this.blurWidth = blurWidth;
    }

    public void setBlurWidthFactor(double blurWidthFactor) {
        this.blurWidthFactor = blurWidthFactor;
    }

    public void setGenFileName(String genFileName) {
        this.genFileName = genFileName;
    }

    public void setPolygonFileName(String polygonFileName) {
        this.polygonFileName = polygonFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public void setMaxNSquareLog(int maxNSquareLog) {
        this.maxNSquareLog = maxNSquareLog;
    }



    public double getBlurWidth() {
        return blurWidth;
    }

    public double getBlurWidthFactor() {
        return blurWidthFactor;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public String getGenFileName() {
        return genFileName;
    }

    public String getPolygonFileName() {
        return polygonFileName;
    }

    public int getMaxNSquareLog() {
        return maxNSquareLog;
    }



    //we give these different names to provide our own serialization method
    public ArrayFloat[] retreiveX() {
        return x;
    }

    public ArrayFloat[] retreiveY() {
        return y;
    }
    public void putX(ArrayFloat[] x) {
        this.x = x;
    }

    public void putY(ArrayFloat[] y) {
        this.y = y;
    }
    public int getArrayLength() {
        return arrayLength;
    }

    public int getLx() {
        return lx;
    }

    public int getLy() {
        return ly;
    }

    public float getMaxx() {
        return maxx;
    }

    public float getMaxy() {
        return maxy;
    }

    public float getMinx() {
        return minx;
    }

    public float getMiny() {
        return miny;
    }

    public void setArrayLength(int arrayLength) {
        this.arrayLength = arrayLength;//note, this is just for bookkeeping in the serialized version of this class
    }

    public void setLx(int lx) {
        this.lx = lx;
    }

    public void setLy(int ly) {
        this.ly = ly;
    }

    public void setMaxx(float maxx) {
        this.maxx = maxx;
    }

    public void setMaxy(float maxy) {
        this.maxy = maxy;
    }

    public void setMinx(float minx) {
        this.minx = minx;
    }

    public void setMiny(float miny) {
        this.miny = miny;
    }
    
    public class ArrayFloat {
        public ArrayFloat(){

        }
	public ArrayFloat ( int x, int y ){
		array = new float[x][y];
	}

    public float[][] getArray() {
        return array;
    }

    public void setArray(float[][] array) {
        this.array = array;
    }

    public float array[][] = null;
    
    
    public class ArrayInt {
	public ArrayInt( int x ){
		array = new int[x];
	}
	public int array[] = null;
}
}
    
    
    public class ArrayPoint {
	/**
	 * @param i
	 */
	public ArrayPoint(int i) {
		array = new Point[i];
		for(int x = 0; x<i; x++)
			array[x] = new Point();
	}

	public Point array[] = null;
}
    

    
    
    public class D3Tensor {
		private int nrl;
		private int ncl;
		private int ndl;
		int xSize, ySize, zSize;
		int kOffset = 0;

		private float matrix[][][];
// Function to allocate a float 3tensor with range
// t[nrl..nrh][ncl..nch][ndl..ndh]. From "Numerical Recipes in C".

		//all parameters changed from long to int
		//the method used to be called: d3tensor
		public D3Tensor (int nrl,int nrh,int ncl,int nch, int ndl, int ndh)
		{
			this.nrl = nrl;
			this.ncl = ncl;
			this.ndl = ndl;
			xSize = nrh-nrl+1;
			ySize = nch-ncl+1;
			zSize = ndh-ndl+1;
			matrix = new float[xSize][ySize][zSize];
		}

		public float getElement( int x, int y , int z)
		{
			return matrix[x-nrl][y-ncl][z-ndl];
		}
		public float getElement( int k ){
			k+=kOffset;
			int zPos = k % zSize;
			int yPos = (k / zSize) % ySize;
			int xPos = (k / zSize)/ySize;

			return matrix[ xPos ][ yPos  ][ zPos ];
		}

		/**
		 * @param b
		 * @param x
		 * @return
		 */
		private float getAndSetElement(int k, float x) {
			k+=kOffset;
			int zPos = k % zSize;
			int yPos = (k / zSize) % ySize;
			int xPos = (k / zSize)/ySize;

			float f = matrix[ xPos ][ yPos  ][ zPos ];
			matrix[ xPos ][ yPos  ][ zPos ] = x;
			return f;
		}
		/**
		 * @param i
		 * @param tempr
		 */
		public void addToElement(int k, float x) {
			k+=kOffset;
			int zPos = k % zSize;
			int yPos = (k / zSize) % ySize;
			int xPos = (k / zSize)/ySize;

			matrix[ xPos ][ yPos  ][ zPos ] += x;
		}


		public void setElement( int k, float f){
			k+=kOffset;
			int zPos = k % zSize;
			int yPos = (k / zSize) % ySize;
			int xPos = (k / zSize)/ySize;

			matrix[ xPos ][ yPos  ][ zPos ] = f;
		}

		public void setElement( int x, int y, int z, float f)
		{
			matrix[x-nrl][y-ncl][z-ndl]=f;
		}

		public int getElementsCount(){
			return xSize*ySize*zSize;
		}

		/**
		 * @param a
		 * @param b
		 */
		public void swapElements(int a, int b) {
			float x = getElement(a);
			float y = getAndSetElement(b,x);
			setElement(a,y);
		}

		/**
		 * @param i
		 * @param j
		 * @param k
		 */
		public void setOffset(int i, int j, int k) {
			// TODO Auto-generated method stub
			kOffset+= k +(j+i*ySize)*zSize;
		}
}
   
    public class DMatrix {
	private int nrl;

	private int ncl;


	private float matrix[][];
	// Function to allocate a float matrix with subscript range
	// m[nrl..nrh][ncl..nch]. From "Numerical Recipes in C".

	//all parameters changed from long to int
	//the method used to be called: dmatrix
	public DMatrix (int nrl,int nrh,int ncl,int nch)
	{
		matrix = new float[nrh-nrl+1][nch-ncl+1];
		this.nrl = nrl;

		this.ncl = ncl;
	}

	public float getElement( int x, int y )
	{
		return matrix[x-nrl][y-ncl];
	}

	public void setElement( int x, int y, float f)
	{
		matrix[x-nrl][y-ncl]=f;
	}
}
    
    
    
}

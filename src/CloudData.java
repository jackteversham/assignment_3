import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CloudData {

	public windVector[][][] advection; // in-plane regular grid of windVector vectors, that evolve over time
	float [][][] convection; // vertical air movement strength, that evolves over time
	public static int [][][] classification; // cloud type per grid point, evolving over time
	int dimx, dimy, dimt; // data dimensions
    public windVector [] vectorArray;

    CloudData(){}

    CloudData(int dimx, int dimy, int dimt, float [][][] convection, int [][][] classification){
        this.dimt=dimt;
        this.dimx=dimx;
        this.dimy=dimy;
        this.convection=convection;
        this.classification=classification;
    }


	// overall number of elements in the timeline grids
	int dim(){
		return dimt*dimx*dimy;
	}
	
	// convert linear position into 3D location in simulation grid
	void locate(int pos, int [] ind)
	{
		ind[0] = (int) pos / (dimx*dimy); // t
		ind[1] = (pos % (dimx*dimy)) / dimy; // x
		ind[2] = pos % (dimy); // y
	}
	
	// read cloud simulation data from file
	void readData(String fileName){ 
		try{ 
			Scanner sc = new Scanner(new File(fileName), "UTF-8");
			
			// input grid dimensions and simulation duration in timesteps
			dimt = sc.nextInt();
			dimx = sc.nextInt(); 
			dimy = sc.nextInt();

            vectorArray = new windVector[dim()]; //initiliase array of vectors, length is equal to total elements in timeline grids
            int count=0; //keeps track of index of vectorArray, also used during boundary classification
			
			// initialize and load advection (windVector direction and strength) and convection
			advection = new windVector[dimt][dimx][dimy];
			convection = new float[dimt][dimx][dimy];
			for(int t = 0; t < dimt; t++)
				for(int x = 0; x < dimx; x++)
					for(int y = 0; y < dimy; y++){
						advection[t][x][y] = new windVector();
						advection[t][x][y].x = sc.nextFloat();
						advection[t][x][y].y = sc.nextFloat();
						convection[t][x][y] = sc.nextFloat();
						vectorArray[count] = advection[t][x][y];
						boundaryClassifier(count, advection[t][x][y]);
                        count++;
					}
			classification = new int[dimt][dimx][dimy];
			sc.close(); 
		} 
		catch (IOException e){ 
			System.out.println("Unable to open input file "+fileName);
			e.printStackTrace();
		}
		catch (java.util.InputMismatchException e){ 
			System.out.println("Malformed input file "+fileName);
			e.printStackTrace();
		}
	}

	DecimalFormat df = new DecimalFormat("#.######");
	
	// write classification output to file
	void writeData(String fileName, double x_prevailing, double y_prevailing ){
		 try{

			 FileWriter fileWriter = new FileWriter(fileName);
			 PrintWriter printWriter = new PrintWriter(fileWriter);
			 printWriter.printf("%d %d %d\n", dimt, dimx, dimy);
			 printWriter.printf("%s %s\n", df.format(x_prevailing), df.format(y_prevailing));
			 
			 for(int t = 0; t < dimt; t++){
				 for(int x = 0; x < dimx; x++){
					for(int y = 0; y < dimy; y++){
						printWriter.printf("%d ", classification[t][x][y]);
					}
				 }
				 printWriter.printf("\n");
		     }
				 
			 printWriter.close();
		 }
		 catch (IOException e){
			 System.out.println("Unable to open output file "+fileName);
				e.printStackTrace();
		 }
	}


    /**
     * This function classifies each vector as a left boundary, right boundary, top boundary or bottom boundary vector
     * respectively. It assigns an integer to the vector accordingly.
     * This will be helpful when calculating the local average of the vector later on.
     * @param i  - the index of the vector in the array
     * @param vector - the vector to be classified
     */

	public void boundaryClassifier(int i, windVector vector){
	    boolean leftBoundary = false;
	    boolean rightBoundary = false;
	    boolean topBoundary = false;
	    boolean bottomBoundary = false;


	    //CHECK IF ON LEFT BOUNDARY
        for (int j = 0; j <= dim()-dimy; j+=dimy) {
            if(i ==j)
                leftBoundary=true;

        }
        //CHECK IF ON RIGHT BOUNDARY
        for (int j = dimx-1; j <= dim(); j+=dimx) {
            if(i ==j)
                rightBoundary=true;
        }

        //CHECK IF ON TOP BOUNDARY of TIME LAYER
        for (int j = 0; j <= dim()-1; j+= (dimy*dimy)) {
            for (int k = j; k < (j+dimy) ; k++) {
                if(i==k)
                    topBoundary=true;

            }

        }
        //CHECK IF ON BOTTOM BOUNDARY of TIME LAYER
        for (int j = (dimy*dimy-dimy); j < dim()-1; j+= (dimy*dimy)) {
            for (int k = j; k < (j+dimy) ; k++) {
                if (i==k)
                    bottomBoundary=true;
            }

        }

        //BOUNDARY CLASSIFICATION
        //normal= 0
        //left boundary = 1
        //top boundary = 2
        //right boundary = 3
        //bottom boundary = 4
        //topleft = 5
        //topright = 6
        //bottomleft = 7
        //bottomright = 8

        if(topBoundary){
            if(leftBoundary)
                vector.boundaryClassification=5;
            else if(rightBoundary)
                vector.boundaryClassification=6;
            else
                vector.boundaryClassification=2;

        }
        else if (bottomBoundary){
            if(leftBoundary)
                vector.boundaryClassification=7;
            else if(rightBoundary)
                vector.boundaryClassification=8;
            else
                vector.boundaryClassification=4;
        }
        else if(rightBoundary)
            vector.boundaryClassification=3;
        else if(leftBoundary)
            vector.boundaryClassification=1;
        else
            vector.boundaryClassification=0; //normal

    }



}

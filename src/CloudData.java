import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

public class CloudData {

	public windVector[][][] advection; // in-plane regular grid of windVector vectors, that evolve over time
	float [][][] convection; // vertical air movement strength, that evolves over time
	int [][][] classification; // cloud type per grid point, evolving over time
	int dimx, dimy, dimt; // data dimensions
    public static windVector [] vectorArray;

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
            int count=0; //keeps track of index of vectorArray
			
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
	
	// write classification output to file
	void writeData(String fileName, windVector wind){
		 try{

			 FileWriter fileWriter = new FileWriter(fileName);
			 PrintWriter printWriter = new PrintWriter(fileWriter);
			 printWriter.printf("%d %d %d\n", dimt, dimx, dimy);
			 printWriter.printf("%f %f\n", wind.x, wind.y);
			 
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
}

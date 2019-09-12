import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by jackteversham on 2019/09/06.
 */
public class cloudClassifier {
    public static int dimt, dimx, dimy;
    public static float [][][] convection; // vertical air movement strength, that evolves over time
    public static int [][][] classification; // cloud type per grid point, evolving over time


    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        System.out.println("Enter input file name:\n");
        String inputfile = input.nextLine();
        System.out.println("Enter output file name:\n");
        String outputfile = input.nextLine();
        System.out.println("Enter code:\n");
        int code = Integer.parseInt(input.nextLine());

        System.out.println("Files Captured Successfully.");

        input.close();


        CloudData cloudData = new CloudData();
        resultObject result = new resultObject();

        cloudData.readData(inputfile); //read data from specified file


        dimy = cloudData.dimy;
        dimx = cloudData.dimx;
        dimt = cloudData.dimt;

        convection = cloudData.convection;
        classification = cloudData.classification;



        System.out.println("cloud data successfully read.");
        windVector [] vectorArray = cloudData.vectorArray; //array of wind vectors ready for operations (linear in time)
        System.out.println(cloudData.dimt + "  " + cloudData.dimx + "  " + cloudData.dimy); //print data dimensions

        if(code==0){ //run once with specified sequential cutoff

            long currentTime = System.currentTimeMillis(); //get system time immediately before run
            result = sum(vectorArray, 220000); //invokes forkJoinPool and all threads. Returns resultObject when finished.
            long timeAfterRun = System.currentTimeMillis(); //get system time immediately after run

            long runTime = (timeAfterRun - currentTime); //calculate difference
            System.out.println("Paralell program executed in: " + runTime + "ms");

        }else{ //run several times for different cutoffs for testing purposes.
            int [] cutoffs = {20000, 120000, 220000, 320000, 420000, 520000, 620000, 720000, 820000, 920000, 1020000, 1120000, 1220000, 1320000, 1420000, 1520000};
            for (int j = 0; j <2 ; j++) {
                double count=0;
                // System.gc();//minimize likelihood that garbage collector will run during execution
                for (int i = 0; i < 1; i++) {

                    long currentTime = System.currentTimeMillis(); //get system time immediately before run
                    result = sum(vectorArray, cutoffs[j]); //invokes forkJoinPool and all threads. Returns resultObject when finished.
                    long timeAfterRun = System.currentTimeMillis(); //get system time immediately after run

                    long runTime = (timeAfterRun - currentTime); //calculate difference
                    System.out.println("Paralell program executed in: " + runTime + "ms");

                    if(i>3){ //take average of just last 7, first 3 runs used for cache warming
                        count+=runTime;
                    }


                }
                System.out.println("\nAverage runtime = "+count/7.0+"\n"); //return this average runtime

            }

        }

            System.out.println(vectorArray.length);
            System.out.println("X sum: " + result.wind.x);
            System.out.println("Y sum: " + result.wind.y+"\n");

            double x_average = result.wind.x / (double) cloudData.dim();
            double y_average = result.wind.y / (double) cloudData.dim();

            System.out.println("Prevailing wind X: "+x_average);
            System.out.println("Prevailing wind Y: "+y_average);

            result.cloudDataObject.writeData(outputfile, x_average, y_average);  //DATA WRITTEN

/*

        int diffCount = 0; //tracks differences in file
        try{

            Scanner sc = new Scanner(new File("largesample_output.txt"), "UTF-8"); //scanner for file 1
            Scanner sc2 = new Scanner(new File("output.txt"), "UTF-8");//scanner for file 2
            for (int i = 0; i <(20*512*512+3) ; i++) {

                if(i<3){ //first three values infile are integers
                    if(sc.nextInt()!= sc2.nextInt()){
                        diffCount++;
                    }
                }else{ //the rest are floats
                    if(sc.nextFloat() != sc2.nextFloat()){
                        diffCount++; //if different, increment differences count
                    }
                }

            }
            System.out.println("\n\nDifferences in files: "+diffCount);

            sc.close();
            sc2.close();

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }


   */


    }
    static final ForkJoinPool fjPool = new ForkJoinPool();
    static resultObject sum(windVector [] vectorArray, int sequential_cutoff){
        return fjPool.invoke(new windThread(0,vectorArray.length, vectorArray, dimt, dimx, dimy, convection, classification, sequential_cutoff));
    }


}

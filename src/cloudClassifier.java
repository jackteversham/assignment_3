import java.util.concurrent.ForkJoinPool;

/**
 * Created by jackteversham on 2019/09/06.
 */
public class cloudClassifier {
    public static int dimt, dimx, dimy;
    public static float [][][] convection; // vertical air movement strength, that evolves over time
    public static int [][][] classification; // cloud type per grid point, evolving over time


    public static void main(String[] args) {
        CloudData cloudData = new CloudData();
        String outputFile = "";
        if (args.length==0){

        cloudData.readData("largesample_input.txt");
        outputFile = "output.txt";
        }
        else{
            cloudData.readData(args[0]);
            outputFile = args[1];
        }


        dimy = cloudData.dimy;
        dimx = cloudData.dimx;
        dimt = cloudData.dimt;

        convection = cloudData.convection;
        classification = cloudData.classification;

        System.out.println("cloud data successfully read.");

        windVector [] vectorArray = cloudData.vectorArray; //array of wind vectors ready for operations (linear in time)

        System.out.println(cloudData.dimt +"  "+cloudData.dimx+"  "+cloudData.dimy);
       // System.out.println("last: "+vectorArray[5242879].boundaryClassification); //should be 8


        System.gc();//minimize likelihood that garbage collector will run during execution
        long currentTime = System.currentTimeMillis();
        resultObject result = sum(vectorArray); //invokes forkJoinPool and all threads. Returns vector in form X-sum; Y-sum when finished
        long timeAfterRun = System.currentTimeMillis();

        long runTime = (timeAfterRun-currentTime);
        System.out.println("Paralell program executed in: "+runTime/1000.0+"s");


        System.out.println(vectorArray.length);
        System.out.println("X sum: "+result.wind.x);
        System.out.println("Y sum: "+result.wind.y);

        double x_average = result.wind.x/(double)cloudData.dim();
        double y_average = result.wind.y/(double)cloudData.dim();

        System.out.println(x_average);
        System.out.println(y_average);

        System.out.println("convection: "+ cloudData.convection[2][1][0]);
        int[] coords = new int[3];

        double x_av = (vectorArray[13].x + vectorArray[13+1].x + vectorArray[13-1].x+ vectorArray[13+cloudData.dimy].x
                + vectorArray[13+cloudData.dimy-1].x + vectorArray[13+cloudData.dimy+1].x)/6.0;

        double y_av = (vectorArray[13].y + vectorArray[13+1].y + vectorArray[13-1].y+ vectorArray[13+cloudData.dimy].y
                + vectorArray[13+cloudData.dimy-1].y + vectorArray[13+cloudData.dimy+1].y)/6.0;
        cloudData.locate(13,coords);

        float convection = cloudData.convection[coords[0]][coords[1]][coords[2]];
        double lenLocalAverage = Math.sqrt((x_av*x_av)+(y_av*y_av));

       // cloudData.classification[coords[0]][coords[1]][coords[2]] = 2;

        if(lenLocalAverage>0.2 && (float)lenLocalAverage>=Math.abs(convection)){
            System.out.println("classification: "+1);

        }
        else if (Math.abs(convection)>(float)lenLocalAverage){

            System.out.println(lenLocalAverage+"local average main");
            System.out.println("classification: "+0);
            System.out.println(Math.abs(convection)+" convection in thread");

        }
        System.out.println(coords[0]);
        System.out.println(coords[1]);
        System.out.println(coords[2]);
        System.out.println(classification[0][0][13]);



       // for (int i = 0; i <512 ; i++) {



            //System.out.println("X: "+coords[1]+" Y: "+coords[2]);
           // System.out.println(vectorArray[i].boundaryClassification);
      //  }


        result.cloudDataObject.writeData(outputFile,x_average, y_average);

    }
    static final ForkJoinPool fjPool = new ForkJoinPool();
    static resultObject sum(windVector [] vectorArray){

        return fjPool.invoke(new windThread(0,vectorArray.length, vectorArray));


    }
}

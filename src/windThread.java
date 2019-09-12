
import java.util.concurrent.RecursiveTask;


/**
 * Created by jackteversham on 2019/09/06.
 */
public class windThread extends RecursiveTask<resultObject> {
    static int dimx;
    static int dimy;
    static int dimt;

    static float [][][] convection; // vertical air movement strength, that evolves over time
    static int [][][] classification; // cloud type per grid point, evolving over time


    private static int SEQUENTIAL_CUTOFF; //value of 3 for testing small data
    int lo; int hi; //recursively splitting the array of vectors using these bounds
    windVector [] vectorArray; //our array of vectors to be operated on

    resultObject result = new resultObject(new CloudData(dimx, dimy, dimt, convection, classification), new windVector());

    windThread(){}
    windThread(int l, int h, windVector [] vecArray, int t, int x, int y, float [][][] connvection, int [][][] classification, int sequential_cutoff){
        this.lo = l;
        this.hi = h;
        this.vectorArray = vecArray;
        this.dimx = x;
        this.dimt = t;
        this.dimy = y;
        this.convection = connvection;
        this.classification = classification;
        this.SEQUENTIAL_CUTOFF = sequential_cutoff;
    }

    protected resultObject compute(){  //the work we want this thread to do in here.

        if(hi-lo< SEQUENTIAL_CUTOFF){ //SEQUENTIAL - actual work here
            for (int i = lo; i < hi; i++) {
                result.wind.x += vectorArray[i].x;
                result.wind.y += vectorArray[i].y;
                classify(result.cloudDataObject, result.wind, i, vectorArray);
            }
            return result; //all work done at this point on sequential ranges

        }else{ //PARALLEL - splitting problem up

            int mid = (hi+lo)/2;
            windThread left = new windThread(lo, mid, vectorArray, dimt, dimx, dimy, convection, classification, SEQUENTIAL_CUTOFF);
            windThread right = new windThread(mid, hi, vectorArray, dimt, dimx, dimy, convection, classification, SEQUENTIAL_CUTOFF);

            left.fork();

           // resultObject leftAns = left.compute(); //send thread away to do task
            resultObject rightAns = right.compute(); //execute sequentially, returns a vector
            resultObject leftAns = left.join(); //wait for thread to complete, returns a vector

            windVector prevWind = leftAns.wind.combine(rightAns.wind);


            leftAns.wind = prevWind; //left ans result object updated with prevailing wind and returned.
            return leftAns; //combines the results of the two child thread and returns prevailing wind vector

        }

    }
    //calculates local average for windVector and classifies each vector accordingly.
    /**
     * Calculates the local average of the wind vector, compares it to its convection value, classifies the cloud to be formed
     * and then updates the relevant index of the classification array accordingly.
     * @param cloudData
     * @param wind
     * @return
     */
    public void classify(CloudData cloudData, windVector wind, int index, windVector [] vectorArray){

        int [] coords = new int[3];
        cloudData.locate(index,coords); //converts a linear index into a 3D location in the grid. Use this 3D location to
                                        //access the correct position in the classification and convection arrays
        float convection = cloudData.convection[coords[0]][coords[1]][coords[2]];
        int boundaryClassification = vectorArray[index].boundaryClassification;


        windVector localVector = new windVector();

        double x_av = 0.0;
        double y_av = 0.0;



        if (boundaryClassification==5){ //topLeft
            x_av = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index+cloudData.dimy].x + vectorArray[index+cloudData.dimy +1].x)/4.0;
            y_av = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index+cloudData.dimy].y + vectorArray[index+cloudData.dimy +1].y)/4.0;

        }
        else if(boundaryClassification==6){//topRight

            x_av= (vectorArray[index].x + vectorArray[index-1].x + vectorArray[index+cloudData.dimy].x + vectorArray[index+cloudData.dimy-1].x)/4.0;
            y_av = (vectorArray[index].y + vectorArray[index-1].y + vectorArray[index+cloudData.dimy].y + vectorArray[index+cloudData.dimy-1].y)/4.0;

        }
        else if (boundaryClassification==7){ //bottomLeft
            x_av  = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index-cloudData.dimy].x + vectorArray[index-cloudData.dimy+1].x)/4.0;
            y_av = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy+1].y)/4.0;

        }else if(boundaryClassification==8){ //bottomRight

            x_av = (vectorArray[index].x + vectorArray[index-1].x + vectorArray[index-cloudData.dimy].x + vectorArray[index-cloudData.dimy-1].x)/4.0;
            y_av = (vectorArray[index].y + vectorArray[index-1].y + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy-1].y)/4.0;

        }else if(boundaryClassification==1){ //Left boundary

            x_av= (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index+cloudData.dimy].x+ vectorArray[index+cloudData.dimy+1].x
                    + vectorArray[index-cloudData.dimy].x + vectorArray[index-cloudData.dimy+1].x)/6.0;
            y_av = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index+cloudData.dimy].y+ vectorArray[index+cloudData.dimy+1].y
                    + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy+1].y)/6.0;


        }
        else if(boundaryClassification==2) { //top boundary

            x_av = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index-1].x+ vectorArray[index+cloudData.dimy].x
                    + vectorArray[index+cloudData.dimy-1].x + vectorArray[index+cloudData.dimy+1].x)/6.0;

            y_av = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index-1].y+ vectorArray[index+cloudData.dimy].y
                    + vectorArray[index+cloudData.dimy-1].y + vectorArray[index+cloudData.dimy+1].y)/6.0;

        }
        else if (boundaryClassification == 3) { //right boundary

            x_av = (vectorArray[index].x + vectorArray[index-1].x + vectorArray[index+cloudData.dimy].x+ vectorArray[index+cloudData.dimy-1].x
                    + vectorArray[index-cloudData.dimy].x + vectorArray[index-cloudData.dimy-1].x)/6.0;
            y_av = (vectorArray[index].y + vectorArray[index-1].y + vectorArray[index+cloudData.dimy].y+ vectorArray[index+cloudData.dimy-1].y
                    + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy-1].y)/6.0;


        } else if(boundaryClassification==4){//bottom boundary

            x_av = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index-1].x+ vectorArray[index-cloudData.dimy].x
                    + vectorArray[index-cloudData.dimy-1].x + vectorArray[index-cloudData.dimy+1].x)/6.0;

            y_av = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index-1].y+ vectorArray[index-cloudData.dimy].y
                    + vectorArray[index-cloudData.dimy-1].y + vectorArray[index-cloudData.dimy+1].y)/6.0;

        } else{ //normal - vector doesn't lie on any boundary.

            x_av = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index-1].x+ vectorArray[index-cloudData.dimy].x
                    + vectorArray[index-cloudData.dimy-1].x + vectorArray[index-cloudData.dimy+1].x
                    +vectorArray[index+cloudData.dimy].x+vectorArray[index+cloudData.dimy-1].x+vectorArray[index+cloudData.dimy+1].x)/9.0;
            y_av = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index-1].y+ vectorArray[index-cloudData.dimy].y
                    + vectorArray[index-cloudData.dimy-1].y + vectorArray[index-cloudData.dimy+1].y
                    +vectorArray[index+cloudData.dimy].y+vectorArray[index+cloudData.dimy-1].y+vectorArray[index+cloudData.dimy+1].y)/9.0;


        }

        double lenLocalAverage = Math.sqrt((x_av*x_av)+(y_av*y_av));

            classification[coords[0]][coords[1]][coords[2]] = 2;

        if(lenLocalAverage>0.2 && (float)lenLocalAverage>=Math.abs(convection)){
            classification[coords[0]][coords[1]][coords[2]] = 1;

        }
        else if (Math.abs(convection)>(float)lenLocalAverage){

            classification[coords[0]][coords[1]][coords[2]] = 0;

        }

    }

}


import java.util.concurrent.RecursiveTask;


/**
 * Created by jackteversham on 2019/09/06.
 */
public class windThread extends RecursiveTask<resultObject> {
    int dimx = cloudClassifier.dimx;
    int dimy = cloudClassifier.dimy;
    int dimt = cloudClassifier.dimt;

    float [][][] convection = cloudClassifier.convection; // vertical air movement strength, that evolves over time
    static int [][][] classification = cloudClassifier.classification; // cloud type per grid point, evolving over time


    private static final int SEQUENTIAL_CUTOFF = 20000; //value of 3 for testing small data
    int lo; int hi; //recursively splitting the array of vectors using these bounds
    windVector [] vectorArray; //our array of vectors to be operated on

    resultObject result = new resultObject(new CloudData(dimx, dimy, dimt, convection, classification), new windVector());


    windThread(int l, int h, windVector [] vecArray){
        this.lo = l;
        this.hi = h;
        this.vectorArray = vecArray;
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
            windThread left = new windThread(lo, mid, vectorArray);
            windThread right = new windThread(mid, hi, vectorArray);

            left.fork(); //send thread away to do task
            resultObject rightAns = right.compute(); //execute sequentially, returns a vector
            resultObject leftAns = left.join(); //wait for thread to complete, returns a vector

            windVector prevWind = leftAns.wind.combine(rightAns.wind);

          /*  int [] coords = new int[3];
            for (int i =  lo; i < hi; i++) {

                rightAns.cloudDataObject.locate(i,coords);
                leftAns.cloudDataObject.classification[coords[0]][coords[1]][coords[2]] = rightAns.cloudDataObject.classification[coords[0]][coords[1]][coords[2]];
            }*/

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

        if (boundaryClassification==5){ //topLeft
            localVector.x = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index+cloudData.dimy].x + vectorArray[index+cloudData.dimy +1].x)/4.0;
            localVector.y = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index+cloudData.dimy].y + vectorArray[index+cloudData.dimy +1].y)/4.0;

        }
        else if(boundaryClassification==6){//topRight

            localVector.x= (vectorArray[index].x + vectorArray[index-1].x + vectorArray[index+cloudData.dimy].x + vectorArray[index+cloudData.dimy-1].x)/4.0;
            localVector.y = (vectorArray[index].y + vectorArray[index-1].y + vectorArray[index+cloudData.dimy].y + vectorArray[index+cloudData.dimy-1].y)/4.0;

        }
        else if (boundaryClassification==7){ //bottomLeft
            localVector.x  = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index-cloudData.dimy].x + vectorArray[index-cloudData.dimy+1].x)/4.0;
            localVector.y = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy+1].y)/4.0;

        }else if(boundaryClassification==8){ //bottomRight

            localVector.x = (vectorArray[index].x + vectorArray[index-1].x + vectorArray[index-cloudData.dimy].x + vectorArray[index-cloudData.dimy-1].x)/4.0;
            localVector.y = (vectorArray[index].y + vectorArray[index-1].y + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy-1].y)/4.0;

        }else if(boundaryClassification==1){ //Left boundary

            localVector.x= (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index+cloudData.dimy].x+ vectorArray[index+cloudData.dimy+1].x
                    + vectorArray[index-cloudData.dimy].x + vectorArray[index-cloudData.dimy+1].x)/6.0;
            localVector.y = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index+cloudData.dimy].y+ vectorArray[index+cloudData.dimy+1].y
                    + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy+1].y)/6.0;


        }
        else if(boundaryClassification==2) { //top boundary

            localVector.x = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index-1].x+ vectorArray[index+cloudData.dimy].x
                    + vectorArray[index+cloudData.dimy-1].x + vectorArray[index+cloudData.dimy+1].x)/6.0;
            localVector.y = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index-1].y+ vectorArray[index+cloudData.dimy].y
                    + vectorArray[index+cloudData.dimy].y + vectorArray[index+cloudData.dimy+1].y)/6.0;

        }
        else if (boundaryClassification == 3) { //right boundary

            localVector.x = (vectorArray[index].x + vectorArray[index-1].x + vectorArray[index+cloudData.dimy].x+ vectorArray[index+cloudData.dimy-1].x
                    + vectorArray[index-cloudData.dimy].x + vectorArray[index-cloudData.dimy-1].x)/6.0;
            localVector.y = (vectorArray[index].y + vectorArray[index-1].y + vectorArray[index+cloudData.dimy].y+ vectorArray[index+cloudData.dimy-1].y
                    + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy-1].y)/6.0;


        } else if(boundaryClassification==4){//bottom boundary

            localVector.x = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index-1].x+ vectorArray[index-cloudData.dimy].x
                    + vectorArray[index-cloudData.dimy-1].x + vectorArray[index-cloudData.dimy+1].x)/6.0;
            localVector.y = (vectorArray[index].y + vectorArray[index].y + vectorArray[index-1].y+ vectorArray[index-cloudData.dimy].y
                    + vectorArray[index-cloudData.dimy].y + vectorArray[index-cloudData.dimy+1].y)/6.0;

        } else{ //normal - vector doesn't lie on any boundary.

            localVector.x = (vectorArray[index].x + vectorArray[index+1].x + vectorArray[index-1].x+ vectorArray[index-cloudData.dimy].x
                    + vectorArray[index-cloudData.dimy-1].x + vectorArray[index-cloudData.dimy+1].x
                    +vectorArray[index+cloudData.dimy].x+vectorArray[index+cloudData.dimy-1].x+vectorArray[index+cloudData.dimy+1].x)/9.0;
            localVector.y = (vectorArray[index].y + vectorArray[index+1].y + vectorArray[index-1].y+ vectorArray[index-cloudData.dimy].y
                    + vectorArray[index-cloudData.dimy-1].y + vectorArray[index-cloudData.dimy+1].y
                    +vectorArray[index+cloudData.dimy].y+vectorArray[index+cloudData.dimy-1].y+vectorArray[index+cloudData.dimy+1].y)/9.0;


        }

        double lenLocalAverage = Math.sqrt((localVector.x*localVector.x)+(localVector.y*localVector.y));

        cloudClassifier.classification[coords[0]][coords[1]][coords[2]] = 2;

        if(lenLocalAverage>0.2 && (float)lenLocalAverage>=Math.abs(convection)){
            cloudClassifier.classification[coords[0]][coords[1]][coords[2]] = 1;

        }
        else if (Math.abs(convection)>(float)lenLocalAverage){

            cloudClassifier.classification[coords[0]][coords[1]][coords[2]] = 0;

        }
        if (coords[0]==0&&coords[1]==0&&coords[2]==13){
            System.out.println(cloudClassifier.classification[coords[0]][coords[1]][coords[2]]+" is the classification");
            System.out.println(lenLocalAverage+" local average thread");
            System.out.println(Math.abs(convection)+" convection in thread");
        }

    }

}

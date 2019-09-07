import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;


/**
 * Created by jackteversham on 2019/09/06.
 */
public class windThread extends RecursiveAction {
    private static final int SEQUENTIAL_CUTOFF = 500; //value of 3 for testing small data
    int lo; int hi; //recursively splitting the array of vectors using these bounds
    windVector [] vectorArray; //our array of vectors to be operated on

    public static double x_total=0; //stores sum of vector x-values
    public static double y_total=0; //stores sum of vector y-values

    public windThread(){}

    windThread(int l, int h, windVector [] vecArray){
        this.lo = l;
        this.hi = h;
        this.vectorArray = vecArray;

    }
    protected void compute(){  //the work we want this thread to do in here.
        if(hi-lo< SEQUENTIAL_CUTOFF){ //SEQUENTIAL
            for (int i = lo; i < hi; i++) {
                x_total+=vectorArray[i].x;
                y_total+=vectorArray[i].y;
            }
        }else{ //PARALLEL
            int mid = (hi+lo)/2;
            windThread left = new windThread(lo, mid, vectorArray);
            windThread right = new windThread(mid, hi, vectorArray);
            left.fork();
            right.compute();
            left.join();
            //x_total=left.x_total +right.x_total;
            //y_total= left.y_total+right.y_total;

        }


    }



    public double getX_total() {
        return x_total;
    }

    public double getY_total() {
        return y_total;
    }
}

import java.util.concurrent.ForkJoinPool;

/**
 * Created by jackteversham on 2019/09/06.
 */
public class cloudClassifier {
    public static void main(String[] args) {

        CloudData cloudData = new CloudData();
        cloudData.readData("largesample_input.txt");
        System.out.println("cloud data successfully read.");

        windVector [] vectorArray = CloudData.vectorArray; //array of wind vectors ready for operations (linear in time)

        sum(vectorArray); //invokes forkJoinPool and all threads. Sums x values of all vectors and y values of all vectors
                          // these values are stored in static variables in the windThread class.
        System.out.println(vectorArray.length);

        double average_X = windThread.x_total/(double) cloudData.dim();
        System.out.println(average_X);
        double average_Y = windThread.y_total/(double) cloudData.dim();
        System.out.println(average_Y);

    }
    static final ForkJoinPool fjPool = new ForkJoinPool();
    static void sum(windVector [] vectorArray){
        fjPool.invoke(new windThread(0,vectorArray.length, vectorArray));

    }
}

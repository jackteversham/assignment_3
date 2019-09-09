import java.util.Vector;

/**
 * Created by jackteversham on 2019/09/01.
 */
public class windVector extends Vector{

   public double x;
   public double y;
   public int boundaryClassification = 0;

   public windVector(double x, double y){
      this.x = x;
      this.y = y;
   }


   public windVector(){
      x=0.0;
      y=0.0;
   }

   public windVector combine(windVector otherVector){ //element-wise addition returning new vector
      return new windVector(this.x+otherVector.x, this.y+otherVector.y);
   }


}

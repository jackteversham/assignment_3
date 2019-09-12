/**
 * Created by jackteversham on 2019/09/07.
 */
public class resultObject {
    public CloudData cloudDataObject; //contains 3D arrays for convection and classification
    public windVector wind;

    resultObject(){}
    public resultObject(CloudData cloudData, windVector wind){
        this.cloudDataObject = cloudData;
        this.wind = wind;
    }

}

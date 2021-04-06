package examScheduler;

import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONArray;
public class Classroom {
    private String name;
    private int capacity;
    private boolean hasComputers;
    private int proctors;
    private int inETF;
    public Classroom(JSONObject jo){
        name=jo.getString("naziv");
        capacity=jo.getInt("kapacitet");
        hasComputers=(jo.getInt("racunari")==0)?false:true;
        proctors=jo.getInt("dezurni");
        inETF=jo.getInt("etf");
    }

    @Override
    public String toString() {
        return "Classroom{" +
                "name='" + name + '\'' +
                ", capacity=" + capacity +
                ", hasComputers=" + hasComputers +
                ", proctors=" + proctors +
                ", inETF=" + inETF +
                ", quality="+getQuality()+
                '}';
    }

    public double getQuality(){
        return ((double)((inETF==0)?1:0))*1.2+proctors;
    }
    public int getCapacity(){
        return capacity;
    }
    public boolean hasComputers(){
        return hasComputers;
    }
    public String getName(){
        return name;
    }
}

package examScheduler;

import java.io.FileNotFoundException;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONArray;

/*
cuva listu sala za 1 termin svakog ispita

 */
public class ETF {
    class RemovedData{
        ArrayList<Classroom> removed;
        ETF forReservaton;
        RemovedData(ArrayList<Classroom> r,ETF reserv){
            removed=r;
            forReservaton=reserv;
        }
    }
    private ArrayList<Classroom> classrooms= new ArrayList<Classroom>();
    private ArrayList<RemovedData>recentlyRemoved=new ArrayList<RemovedData>();
    private int day;
    private int time;

    /*
    parsira sale
     */
    public ETF(String JSONfile) throws FileNotFoundException, JSONException {
        FileReader fd=new FileReader(JSONfile);
        JSONTokener tokener=new JSONTokener(fd);
        JSONArray ja=new JSONArray(tokener);
        for(int i=0;i<ja.length();i++){
            classrooms.add(new Classroom(ja.getJSONObject(i)));
        }
        classrooms.sort((Classroom c1, Classroom c2)->{
            double d=c1.getQuality()-c2.getQuality();
            if(d<0)return -1;
            if(d>0) return 1;
            else return c1.getCapacity()-c2.getCapacity();

        });

    }
    /*
    rezervise sale koje je neko zauzeo
     */
    public void classroomsReserved(ETF reservation){
        ArrayList<Classroom> reserv= reservation.classrooms;
        ArrayList<Classroom> removed=new ArrayList<Classroom>();
        reserv.forEach(c->{
            classrooms.remove(c);
            removed.add(c);
        });
        recentlyRemoved.add(new RemovedData(removed,reservation));
    }

    /*
    one vrednosti koje su prethodno obrisane zbog necije rezervacije sala, pvracaju se ponovo
     */
    public void returnRemoved(ETF reservation){
        List<RemovedData> removed =recentlyRemoved.stream().filter(d->{
            return d.forReservaton==reservation;
        }).collect(Collectors.toList());
        for(RemovedData d:removed){
            classrooms.addAll(d.removed);
        }
        removed.removeAll(removed);
        classrooms.sort((c1,c2)->{
            double d=c1.getQuality()-c2.getQuality();
            if(d<0)return -1;
            if(d>0) return 1;
            else return c1.getCapacity()-c2.getCapacity();
        });
    }
    /*
    rezervise ceo termin (slucaj kad je isti smer a ista ili susedna godina)
     */
    public void reserveAll(ETF reserv){
        ArrayList<Classroom> removed=new ArrayList<Classroom>();
        removed.addAll(classrooms);
        recentlyRemoved.add(new RemovedData(removed,reserv));
        classrooms.clear();
    }
    public int getDay(){
        return day;
    }
    public int getTime(){
        return time;
    }
    public void setDay(int d){
        day=d;
    }
    public void setTime(int t){
        time=t;
    }
    public ETF(ArrayList<Classroom> cr){
        classrooms=cr;
    }
    public int size(){
        return classrooms.size();
    }
    public ArrayList<Classroom> getClassRooms() {
        return classrooms;
    }
    public double getQuality(){
        double sum=0;
        for (Classroom cr:classrooms){
            sum+=cr.getQuality();
        }
        return sum;
    }
    public boolean hasClassroom(String name){
        for(Classroom c:classrooms){
            if(c.getName().equals(name)) return true;
        }
        return false;
    }
    /*
    provera za forward checking da li zbir svih sala dostupnih u tom terminu premasuje broj studenata za taj ispit
    ako ne, te sale su neupotrebljive i sam termin
     */
    public boolean forwardChecking(int students){
        int cap=0;
        for(Classroom c:classrooms){
            cap+=c.getCapacity();
        }
        if(cap>=students) return true;
        else return false;
    }

    /*
    dohvata najbolju kombinaciju sala po algoritmu
     */
    public ETF getBestCombination(int studentsCount, double minQuality){
        return new ETF(Algorithm.getBestCombination(classrooms,studentsCount,minQuality));
    }
    public int getCapacity(){
        int c=0;
        for(Classroom cs:classrooms){
            c+=cs.getCapacity();
        }
        return c;
    }
    public String getAllClassrooms(){
        StringBuilder ret=new StringBuilder();

        for(int i=0;i<classrooms.size();i++){
            if(i!=0) ret.append(",").append(classrooms.get(i).getName());
            else ret.append(classrooms.get(i).getName());
        }
        return ret.toString();
    }
    public void deleteSolution(ETF e){
        classrooms.removeAll(e.getClassRooms());
    }
    @Override
    public String toString() {
        return "ETF{" +
                "classrooms=" + classrooms +
                '}';
    }
}

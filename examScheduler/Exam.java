package examScheduler;

import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Arrays;
/*
cuva informacije o jednom ispitu
 */
public class Exam {
    private ExamPeriod examPer;
    private String courseCode;
    private int numOfRegStudents;
    private boolean onComputers;
    private boolean[] department=new boolean[7];
    private boolean marked; //da li je vec vrednost dodeljena
    private ExamDay[] dailyValues; //vrednosti koje ima za svaki dan ispita
    private double []branches;
    private int year;
    private ETF reserved;
    
    enum DEPS{
        SI,IR,OE,OG,OT,OS,OF;
    }

    /*
    parsira jedan ispit i dohvata sve informacije o njemu
     */
    public Exam(JSONObject jo,ExamPeriod ep) {
        examPer=ep;
        dailyValues=new ExamDay[ep.getDuration()];
        for(int i=0;i<ep.getDuration();i++) dailyValues[i]=new ExamDay();
        branches=new double[ep.getDuration()];
        courseCode=jo.getString("sifra");
        year= Integer.parseInt(courseCode.substring(5,6));
        numOfRegStudents=jo.getInt("prijavljeni");
        onComputers=(jo.getInt("racunari")==0)?false:true;
        JSONArray dep=jo.getJSONArray("odseci");
        for(int i=0;i<dep.length();i++){
            switch(dep.getString(i)){
                case "СИ": department[0]=true; break;
                case "ИР": department[1]=true; break;
                case "ОЕ": department[2]=true; break;
                case "ОГ": department[3]=true; break;
                case "ОТ":department[4]=true; break;
                case "ОС":department[5]=true; break;
                case "ОФ":department[6]=true; break;
            };
        }
        marked=false;


    }

    public void returnRemoved(ETF reservation){
        for(ExamDay ac:dailyValues){
            ac.returnRemoved(reservation);
        }
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void deleteSolution(ETF e){
        dailyValues[e.getDay()].deleteSolution(e);
    }
    public boolean forwardChecking(){
        for(ExamDay val:dailyValues){
            if(val.forwardChecking(numOfRegStudents)) return true;
        }
        return false;
    }
    public boolean isInClassroom(int day, int time,String classroomName){
        if(reserved==null)return false;
        if((reserved.getDay()==day)&&reserved.getTime()==time&&reserved.hasClassroom(classroomName)) return true;
        else return false;
    }
    private boolean hasSameDepartmentAs(Exam e){
        for(int i=0;i<7;i++){
            if(department[i]&&e.department[i]) return true;
        }
        return false;
    }

    public void reserveClassrooms(ETF reservation){
        reserved=reservation;
        marked=true;
        examPer.reserveClassrooms(this,reservation);

    }

    public void cancelReservation(ETF reservation){
        reserved=null;
        examPer.cancelReservation(reservation);
        marked=false;
    }

    /*
    uklanja vrednosti iz domena zbog necije rezervacije
     */
    public void classroomsReserved(Exam e,ETF reservation){
        int day=reservation.getDay();
        dailyValues[day].classroomsReserved(reservation,false);
        if((e.year==year)&&hasSameDepartmentAs(e)){
            dailyValues[day].reservedAllDay(reservation);
        }
        if(Math.abs(e.year-year)==1&&hasSameDepartmentAs(e)){
            dailyValues[day].classroomsReserved(reservation,true);
        }
    }
    public void setMarked(boolean m){
        marked=m;
    }
    public boolean isMarked(){
        return marked;
    }

    public void setValues(ArrayList<Classroom>rooms){
        //System.out.println(rooms.size());
        ArrayList<Classroom> lc=new ArrayList<>(rooms);
        lc.removeIf(c->{
            return onComputers && !c.hasComputers();
        });
        //System.out.println(lc.size());
        for (ExamDay day:dailyValues) {
            day.setValues(lc);
        }


    }

    public int getNumOfConstraints(){
        ArrayList<Exam> exams=examPer.getExams();
        int cons=0;
        for(Exam e:exams){
            if(e!=this&&!e.isMarked()&&Math.abs(year-e.year)<2){
                //za sve ispite koji nisu vec odredjeni i nisu jednaki tom ispitu i pripadaju susednim ili istim godinama
                for(int i=0;i<7;i++){
                    if(department[i] && e.department[i])cons++;
                    //broj zavisnosti je broj takvih ispita ako su ispiti za isti odsek

                }
            }
        }
        return cons;
    }
    public void resetBranches(){
        for(int i=0;i<branches.length;i++) branches[i]=0;
        for(ExamDay d:dailyValues)d.resetBranches();
    }

    /*
    dohvata najbolje kombinacije sala za taj ispit po danima i bira najbolju
    minQuality predstavlja kvalitet prethodno odabrane kombinacije, tekuca mora da bude losija
     */
    public ETF getBestCombination(double minQuality,int day,int time){
        double quality=-1;
        ETF best=null;
        int i=0;
        for(ExamDay ed:dailyValues){
            ETF bestDaily = null;
            if(i<=day) bestDaily=ed.getBestCombination(numOfRegStudents, minQuality,time);
            else bestDaily=ed.getBestCombination(numOfRegStudents, branches[i],-1);
            if (bestDaily != null) {
                double q = bestDaily.getQuality();
                int cap=bestDaily.getCapacity();
                if (q < quality || quality == -1||(q==quality&&cap<best.getCapacity())) {
                    quality = q;
                    best= bestDaily;
                    best.setDay(i);
                }
            }
            i++;

        }
        if(best!=null) branches[best.getDay()]=best.getQuality();
        return best;
    }
    public int getNumOfValues(int days){
        int sum=0;
        for(int d=0;d<days;d++){
            sum+=dailyValues[d].getNumOfValues();
        }
        return sum;
    }
    public double getReservedQuality(){
        if(reserved!=null ) return reserved.getQuality();
        return 0;
    }
    @Override
    public String toString() {
        return "Exam{" +
                "courseCode='" + courseCode + '\'' +
                ", numOfRegStudents=" + numOfRegStudents +
                ", onComputers=" + onComputers +
                ", department=" + Arrays.toString(department) +
                ", year="+year+
                ", values="+getNumOfValues(examPer.getDuration())+
                ",constraints="+getNumOfConstraints()+
                '}';
    }
}

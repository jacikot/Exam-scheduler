package examScheduler;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;
import java.util.stream.Collectors;


import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONObject;
import org.json.JSONArray;
/*
Klasa koja ucitava i cuva rok sa svim ispitima
 */
public class ExamPeriod {
    private int duration;
    private ArrayList<Exam> exams=new ArrayList<>();
    private ETF availableClassrooms;

    public ExamPeriod(String JSONfile) throws FileNotFoundException, JSONException {
        FileReader fd=new FileReader(JSONfile);
        JSONTokener tokener=new JSONTokener(fd);
        JSONObject jo=new JSONObject(tokener);
        duration=jo.getInt("trajanje_u_danima");
        JSONArray exms=jo.getJSONArray("ispiti");
        for(int i=0;i<exms.length();i++){
            exams.add(new Exam(exms.getJSONObject(i),this));
        }

    }
    public ArrayList<Exam> getExams() {
        return exams;
    }

    /*
    svakom ispitu postavlja sale
     */
    public void setAvailableClassrooms(ETF etf){
        ArrayList<Classroom> halls=etf.getClassRooms();
        exams.forEach(e->{
            e.setValues(halls);
        });
        availableClassrooms=etf;

    }

    public int getDuration(){
        return duration;
    }
    /*
    dohvata sledeci ispit kome treba dodeliti vrednost
     */
    public Exam getNextExam(){

        List<Exam> elist=exams.stream().filter(e->{ //posmatraj samo one koji nisu vec dodeljeni
            return !e.isMarked();
        }).collect(Collectors.toList());

        elist.sort((e1,e2)->{
            //njih sortiraj po broju preostalih vrednosti (prvo one sa manjim brojem vrednosti) i po broju ogranicenja sa drugima (sa vise ogranicenja)
            int valDif=e1.getNumOfValues(duration)-e2.getNumOfValues(duration);
            if(valDif!=0)return valDif;
            return e2.getNumOfConstraints()-e1.getNumOfConstraints();
        });
        return elist.get(0); //uzmi prvi takav
    }
    @Override
    public String toString() {
        return "ExamPeriod{" +
                "duration=" + duration +
                ", exams=" + exams +
                '}';
    }
    /*
    rezervise odabrane sale, tako da ih izbacuje iz domena ostalih nedodeljenih ispita
     */
    public void reserveClassrooms(Exam e,ETF request){
        exams.forEach((Exam other)->{
            if(!other.isMarked()) other.classroomsReserved(e,request);
        });
    }
    /*
    ponistava rezervaciju ako nije moguca
     */
    public void cancelReservation(ETF reservation){
        exams.forEach((Exam other)->{
            if(!other.isMarked())other.returnRemoved(reservation);
        });
    }
    /*
    pronalazi najbolju kombinaciju sala za sve ispite i ispisuje korake algoritma
     */
    public boolean schedule(int size,FileWriter fw) throws IOException {
        if(size==0) return true;

        //dohvata sledeci ispit
        Exam e=getNextExam();
        String spaces="";
        for(int i=0;i<exams.size()-size;i++)spaces+=" ";

        //dohvata najbolju kombinaciju za njega
        ETF bestAvailable=e.getBestCombination(0,-1,-1);

        //pokusava da pronadje najbolju kombinaciju takvu da moze da se dodeli ispitu
        while(bestAvailable!=null){

            e.reserveClassrooms(bestAvailable);

            //forward checking
            boolean fc=true;
            for(Exam ee:exams){
                if(!ee.isMarked()) fc=ee.forwardChecking();
                if(!fc) break;
            }
            //ako je dodeljena vrednost ispitu, ispisuje korak
            if(fc){
                StringBuilder sb=new StringBuilder();
                sb.append(spaces).append("predmet: ").append(e.getCourseCode()).append(" dodeljeno: ")
                        .append(" Dan").append(bestAvailable.getDay())
                        .append(" y ").append(getTime(bestAvailable.getTime()))
                        .append(" sale:").append(bestAvailable.getAllClassrooms()+"\n");
                fw.write(sb.toString());
            }

            //rekurzivno ponavlja postupak
            if(fc&&schedule(size-1,fw)) return true;
            else {

                //ako nije uspelo, ponistava rezervaciju i dohvata novu najbolju komb sala

                e.cancelReservation(bestAvailable);
                bestAvailable=e.getBestCombination(bestAvailable.getQuality(),bestAvailable.getDay(),bestAvailable.getTime());
            }
        }
        e.resetBranches();
        return false;
    }
    /*
    pokrece rasporedjivanje
     */
    public boolean examScheduler(ETF classrooms,String stepsFile){
        setAvailableClassrooms(classrooms);
        try {
            FileWriter fw = new FileWriter(stepsFile);

            boolean b=schedule(exams.size(),fw);
            if(!b)fw.write("*****Solution not found*****");
            fw.close();
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }
    private String getTime(int t){
        switch(t){
            case 0:return "08:00";
            case 1:return "11:30";
            case 2: return "15:00";
            default:return "18:30";
        }
    }
    private String header(){
        StringBuilder ret= new StringBuilder();
        for (Classroom c:availableClassrooms.getClassRooms()){
            ret.append(",").append(c.getName());
        }
        return ret.toString();
    }
    /*
    formira CSV fajl od dodeljenih sala ispitima
     */
    public void printCSV(String filePath) {
        try {
            try (FileWriter fw = new FileWriter(filePath)) {
                String h = header();
                for (int i = 0; i < duration; i++) {
                    fw.write("Dan" + i + h + "\n");
                    for (int j = 0; j < 4; j++) {
                        final int ii = i;
                        final int jj = j;
                        if (availableClassrooms == null) return;
                        fw.write(getTime(jj));
                        for (Classroom c : availableClassrooms.getClassRooms()) {
                            List<Exam> eTime = exams.stream().filter(e -> {
                                return e.isInClassroom(ii, jj, c.getName());
                            }).collect(Collectors.toList());
                            if (eTime.size() > 1) {
                                System.out.println("Error");
                                return;
                            }
                            if (eTime.size() == 0) fw.write(",X");
                            else fw.write("," + eTime.get(0).getCourseCode());

                        }
                        fw.write("\n");

                    }
                    fw.write("\n");


                }
                fw.close();
            }
            double k=0;
            for(Exam e:exams){
                k+=e.getReservedQuality();
            }
            System.out.println("Kvalitet rasporeda:"+k);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[]arg){
        try {
            ExamPeriod p=new ExamPeriod("D:\\3. godina\\intel\\projekat2\\javni_testovi\\rok6.json");
            System.out.println(p);
            ETF sale=new ETF("D:\\3. godina\\intel\\projekat2\\javni_testovi\\sale6.json");
            System.out.println(sale);
            if(p.examScheduler(sale,"izlaz1.txt"))  p.printCSV("izlaz2.csv");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}

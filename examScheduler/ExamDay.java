package examScheduler;

import java.util.ArrayList;
/*
cuva informacije vrednostima za jedan dan jednog ispita
 */
public class ExamDay {
    private ETF values[]=new ETF[4]; //4 termina
    private double branches[]=new double[4];


    public void setValues(ArrayList<Classroom> lc){
        for(int i=0;i<4;i++)
            values[i]=new ETF(new ArrayList<Classroom>(lc));
    }


    public int getNumOfValues(){
        int sumVal=0;
        for (ETF ac:values) {
           if(ac!=null) sumVal+=ac.size();
        }
        //System.out.println(sumVal);
        return sumVal;
     }


    public boolean forwardChecking(int students){
        for(ETF val:values){
            if(val.forwardChecking(students)) return true;
        }
        return false;
    }


    public void resetBranches(){
        for(int i=0;i<4;i++)branches[i]=0;
    }


    public void deleteSolution(ETF e){
        values[e.getDay()].deleteSolution(e);
    }

    /*
    pronalazi najbolju kombinaciju sala za svaki termin i uzima najbolju od njih
    kombinacija mora da zadovoljava kapacitet za broj studenata, kao i da ne bude bolja od prethodno odabrane
     */
    public ETF getBestCombination(int studentsCount, double minQuality,int time){
        double quality=-1;
        ETF best=null;
        int i=0;
        for (ETF e:values) {

            ETF bestt=null;
            if(i<=time)bestt= e.getBestCombination(studentsCount, minQuality);
            else bestt=e.getBestCombination(studentsCount, branches[i]);
            if (bestt.getClassRooms() != null) {
                double q = bestt.getQuality();
                int cap=bestt.getCapacity();
                if (q < quality || quality == -1||(q==quality&&cap<best.getCapacity())) {
                    quality = q;
                    best = bestt;
                    best.setTime(i);
                }
            }
            i++;
        }
        if(best!=null)branches[best.getTime()]=best.getQuality();
        return best;
    }

    public void returnRemoved(ETF reservation){
        for(ETF ac:values){
            ac.returnRemoved(reservation);
        }
    }

    public void classroomsReserved(ETF reservation, boolean allClassrooms){
        int time = reservation.getTime();
        if(!allClassrooms) values[time].classroomsReserved(reservation);
        else values[time].reserveAll(reservation);
    }

    public void reservedAllDay(ETF reservation){
        for(ETF ac:values){
            ac.reserveAll(reservation);
        }
    }
}

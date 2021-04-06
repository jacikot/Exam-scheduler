package examScheduler;
import java.util.ArrayList;
/*
algoritam izbora sala
 */
public class Algorithm {
    static class Data{ //podaci koji su potrebni algoritmu
        ArrayList<Classroom> selected; //sale jedne kombinacije
        int startIndex; //indeks sale od koje treba vrsiti proveru sa ovom kombinacijom (za otklanjanje duplikata)
        double quality; //kvalitet kombinacije
        int capacity; //kapacitet kombinacije


        public Data( ArrayList<Classroom> s, int ind){
            selected=s;
            startIndex=ind;
            quality=0;
            capacity=0;
            for(Classroom cr:s){
                quality+=cr.getQuality();
                capacity+=cr.getCapacity();
            }
        }
        public Data(int ind){
            selected=null;
            startIndex=ind;
            quality=-1;
            capacity=0;
        }

    }

    /*
    dohvata najbolju kombinaciju sala za prosledjenu listu sala, broj studenata i prethodno odabrani kvalitet
     */
    public static ArrayList<Classroom> getBestCombination(ArrayList<Classroom>clist, int studentsCount, double minQuality){
        Data cur=new Data(clist.size());
        //odrzava dve pomocne liste, jedna cuva sve kombinacije velicine k, druga k+1

        ArrayList<Data> list1=new ArrayList<Data>();
        ArrayList<Data> list2=new ArrayList<Data>();
        ArrayList<Classroom> list=new ArrayList<>();
        list1.add(new Data(list,0));

        for(int y=0;y<clist.size();y++){
            for(int i=0;i<clist.size();i++){ //prolazi kroz sve sale
                Classroom a=clist.get(i);
                boolean finished=false;
                for(int k=0;k<list1.size();k++){ //prolazi kroz sve kombinacije iz prethodne iteracije
                    Data dk=list1.get(k);
                    if(dk.startIndex>i) continue;

                    ArrayList<Classroom>newList=new ArrayList<>(dk.selected);
                    newList.add(a);
                    double newQuality=a.getQuality()+dk.quality;

                    //ako kapacitet zadovoljava, ako je kvalitet losiji od prethodno odabranog
                    //ako je kvalitet tekuceg odabrane kombinacije losiji, ili jednak, ali je kapacitet manje odgovarajuci
                    //postavlja tu kombinaciju kao tekucu odabranu i zavrsava uzimanje kombinacija preth iteracije jer su losije sigurno od tekuce

                    if((a.getCapacity()+dk.capacity>=studentsCount)&&
                            (newQuality>minQuality)&&
                            ((cur.quality==-1)||(cur.quality>newQuality)||(cur.quality==newQuality)&&(cur.capacity>a.getCapacity()+dk.capacity))){
                        cur=new Data(newList,i);
                       finished=true;
                        break;
                    }
                    else{
                        //u suprotnom ubacuje u listu 2 ako je kvalitet manji od tekuceg
                        if(newQuality<cur.quality||cur.quality==-1)list2.add(new Data(newList,i+1));
                    }


                }
                if(finished) break;
            }
            if(list2.isEmpty()) break;
            ArrayList<Data> lp=list1;
            list1=list2;
            list1.sort((d1,d2)->{
                double dif= d1.quality-d2.quality;
                if(dif<0)return -1;
                if(dif>0)return 1;
                return d1.capacity-d2.capacity;
            }); //sortira sve dobijene kombinacije koje imaju k+1 sala po kvalitetu
            lp.clear();
            list2=lp;
        }
        return cur.selected;


    }
}

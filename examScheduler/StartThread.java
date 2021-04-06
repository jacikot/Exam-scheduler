package examScheduler;

import java.io.FileNotFoundException;
import org.json.JSONException;
/*
Nit koja pokrece izvrsavanje programa nad zadatim fajlovima
 */
public class StartThread extends Thread{
    private final String rok,sale,izlaz,koraci;
    public StartThread(String rrok, String ssale, String iizlaz, String kkoraci){
        rok=rrok;
        sale=ssale;
        izlaz=iizlaz;
        koraci=kkoraci;
    }
    @Override
    public void run() {
        try {
            ExamPeriod p=new ExamPeriod(rok);
            System.out.println(p);
            ETF s=new ETF(sale);
            System.out.println(s);
            if(p.examScheduler(s,koraci))  p.printCSV(izlaz);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void main(String []arg) {
        StartThread r=new StartThread("D:\\3. godina\\intel\\projekat2\\javni_testovi\\rok5.json",
                "D:\\3. godina\\intel\\projekat2\\javni_testovi\\sale5.json",
                "izlaz2.csv", "izlaz1.txt"
                );
       // r.setDaemon(true);
        r.start();
        try {
           synchronized (r){
               r.wait(20000);
           }
        } catch (InterruptedException e) { }
        if(r.isAlive()) System.err.println("Execution more than 20s:Interrupted");
    }
}

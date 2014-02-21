/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class ReduceGraph implements Runnable{
    
    public File Inp;
    public File Out;
    
    public ReduceGraph (String Inp, String Out){
        this.Inp = new File(Inp);
        this.Out = new File(Out);
    }
    
    public ReduceGraph (File Inp, String Out){
        this.Inp = Inp;
        this.Out = new File(Out);
    }

    @Override
    public void run() {
        String[] x;
        UniqueListInt ULI = new UniqueListInt();
        String src;
        try {
            BufferedReader br = new BufferedReader(new FileReader(Inp));
            x = br.readLine().split(";");
            br.close();
            src = x[0];
            for(int i=1;i<x.length;i++){
                ULI.Add(Integer.parseInt(x[i]));
            }
            System.out.println("Reduce finished.");
            BufferedWriter bw = new BufferedWriter(new FileWriter(Out,false));
            for(UniqueListInt.UElementInt e : ULI.UList){
                bw.write(src + ";" + e.ID + ";" + e.count + "\n");
            }
            bw.close();
            System.out.println("Write file finished.");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReduceGraph.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(ReduceGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

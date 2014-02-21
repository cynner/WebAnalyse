/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexto;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class ThaiDictCompare  implements Comparator<String>{

    @Override
    public int compare(String o1, String o2) {
        int p2 = 0;
        char[] ca2 = o2.toCharArray();
        char t1=0,t2=0;
        boolean hasTone = false;
        for(char c : o1.toCharArray()){
            if(p2 >= ca2.length)
                return 1;
            // check tone
            if(c >= '็' && c <= '์'){
                if(!hasTone){
                    t1 = c;
                    if (ca2[p2] >= '็' && ca2[p2] <= '์')
                        t2 = ca2[p2++];
                    hasTone = true;
                }
            }else {
                if(ca2[p2] >= '็' && ca2[p2] <= '์'){
                    if(!hasTone){
                        t2 = ca2[p2++];
                        hasTone = true;
                    }else{
                        p2++;
                    }
                    if(p2 >= ca2.length)
                        return 1;
                }
                if(c != ca2[p2]){
                    //System.out.println("gug");
                    return c - ca2[p2];
                }
                p2++;
            }
        }
        
                    //System.out.println("gug2");
        if(p2 == ca2.length)
            return t1 - t2;
        else
            return -1;
    }
    
    
    public static void main(String[] args){
        try {
            String FileIn = "country.txt";
            String FileOut = "countrys.txt";
            ArrayList<String> s = new ArrayList<String>();
            BufferedReader br = new BufferedReader(new FileReader(FileIn));
            BufferedWriter bw = new BufferedWriter(new FileWriter(FileOut));
            String Line;
            while((Line = br.readLine())!=null){
                s.add(Line);
            }
            br.close();
            Collections.sort(s,new ThaiDictCompare());
            for(String x : s){
                bw.write(x);
                bw.newLine();
            }
            bw.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ThaiDictCompare.class.getName()).log(Level.SEVERE, null, ex);
        } catch( IOException ex){
            
        }
         
         
    }
    
}

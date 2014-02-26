/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Analyse;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Malang
 */
public class SCC2DB {
    
    
    public static void main(String[] args){
        String FileName = "data/Graph/SCC.txt";
        int cnt = 0;
        SQLiteConnection conn = new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
        try(BufferedReader br = new BufferedReader(new FileReader(FileName))) {
            String Line;
            String[] strs;
            try{
                conn.open();
                conn.exec("BEGIN;");
                System.out.println("start");
                while((Line = br.readLine()) != null && !Line.equals("")){
                    if(cnt > 50000){
                        System.out.println("50000");
                        conn.exec("COMMIT;");
                        cnt = 0;
                        conn.exec("BEGIN;");
                    }
                    strs = Line.split(":");
                    conn.exec("UPDATE webpage SET scc_no=" + strs[1] + " WHERE id=" + strs[0] + ";");
                    cnt ++;
                }
                conn.exec("COMMIT;");
                System.out.println("Success");
            } catch (SQLiteException ex) {
                Logger.getLogger(SCC2DB.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                conn.dispose();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(SCC2DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

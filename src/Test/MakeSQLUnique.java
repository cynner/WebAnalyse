/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Test;

import Crawler.MyURL;
import DBDriver.TableConfig;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class MakeSQLUnique {

    public static class LAU{
        int ID;
        String URL;
        
        public LAU(int id,String URL){
            this.ID = id;
            this.URL = URL;
        }
    }
    
    
    public static void main(String[] args){
        ArrayList<LAU> arr = new ArrayList<>();
        SQLiteConnection db = new SQLiteConnection(TableConfig.FileWebPageDB);
        int beg,step=100000;
        SQLiteStatement stmt = null;
        String oldURL,newURL;
        int id;
        beg= args.length > 0 ? Integer.parseInt(args[0]) : 0;
        try {
            db.open();
            while(true){
                try{
                    stmt = db.prepare("SELECT id,url FROM webpage LIMIT " + beg + "," + step + ";");
                    if(stmt.step()){
                        do{
                            oldURL = stmt.columnString(1);
                            try {
                                newURL = (new MyURL(oldURL)).UniqURL;
                            } catch (Exception ex) {
                                Logger.getLogger(MakeSQLUnique.class.getName()).log(Level.SEVERE, null, ex);
                                newURL = null;
                            }
                            if(!oldURL.equals(newURL)){
                                System.out.println("OLD : " + oldURL);
                                System.out.println("NEW : " + newURL);
                                arr.add(new LAU(stmt.columnInt(0), newURL));
                            }
                        }while(stmt.step());
                    }else{
                        break;
                    }
                }catch (SQLiteException ex) {
                    Logger.getLogger(MakeSQLUnique.class.getName()).log(Level.SEVERE, null, ex);
                } finally{
                    stmt.dispose();
                }
                
                System.out.println("Update Qry #" + beg +"...");
                if (!arr.isEmpty()){
                    db.exec("BEGIN;");
                    for(LAU a : arr){
                        db.exec("UPDATE OR IGNORE webpage SET url='" + a.URL.replace("'", "''") + "' WHERE id=" + a.ID + ";");
                    }
                    db.exec("END;");
                    System.out.println("...END");
                    arr.clear();
                }
                beg += step;
            }
            System.out.println("Success...");
        } catch (SQLiteException ex) {
            Logger.getLogger(MakeSQLUnique.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            db.dispose();
        }
        
        
    }
    
}

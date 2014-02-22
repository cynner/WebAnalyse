/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Analyse;

import ArcFileUtils.ArcReader;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class PageCount {
    
    public static String DBPath = "resource/crawler.sqlite3";
    public static void main(String [] args){
        SQLiteConnection db = new SQLiteConnection(new File(DBPath));
        SQLiteStatement stmt;
        try {
            db.open();

            String lexDirName = "data/lexicalorg";
            File lexDir = new File(lexDirName);
            int cnt;
            String host;
            
            /*
            for (File f : lexDir.listFiles()) {
                ArcFileUtils.ArcReader ar = new ArcReader(f);
                cnt = 0;
                while (ar.Skip()) {
                    cnt++;
                }
                host = f.getName().replace("crawl-", "").replace(".arc.gz", "");
                System.out.println(host);
                //db.exec("BEGIN;");
                db.exec("UPDATE host SET page_count="+cnt+", mark=1 WHERE hostname='"+host+"';");
                //db.exec("COMMIT;");
                ar.close();
            }
            */
            stmt = db.prepare("SELECT hostname FROM host WHERE mark is NULL AND status=0;");
            while(stmt.step()){
                host = stmt.columnString(0);
                File f = new File(lexDirName + "/crawl-" + host + ".arc.gz");
                if(f.exists()){
                    try (ArcFileUtils.ArcReader ar = new ArcReader(f)) {
                        cnt = 0;
                        while (ar.Skip()) {
                            cnt++;
                        }
                        System.out.println(host);
                        //db.exec("BEGIN;");
                        db.exec("UPDATE host SET page_count=" + cnt + ", mark=1 WHERE hostname='"+host+"';");
                    } catch (IOException ex) {
                        Logger.getLogger(PageCount.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else{
                    System.err.println(host + "!!!");
                }
            }
        } catch (SQLiteException ex) {
            Logger.getLogger(PageCount.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            db.dispose();
        }
    }
}

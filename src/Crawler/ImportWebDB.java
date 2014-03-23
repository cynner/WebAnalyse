/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.MyRandomAccessFile;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class ImportWebDB {
    public static SQLiteConnection db = new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
    public static void main(String[] args){
        String base_dir = args.length > 0 ? args[0] : "data/crawler";
        String task_name = args.length > 1 ? args[1] : "task0001";
        String webdb_name = "webpageinfo.txt";
        String PathName = base_dir + "/" + task_name + "/" + webdb_name;
        try(MyRandomAccessFile raf = new MyRandomAccessFile(PathName, "r")){
            raf.readLong();
            String Line,cmd;
            String[] cols;
            try{
                db.open();
                db.exec("BEGIN;");
                System.out.println("BEGIN");
                while((Line = raf.readLine()) != null){
                    cols = Line.split(",");
                    //if(cols.length >= SiteCrawler.WebDBColumnWidth){
                        Line = Line.replaceAll(" ", "%20");
                        cmd = "INSERT OR IGNORE INTO webpage(url,language,file_size,comment_size,js_size,style_size,content_size) VALUES(" + Line + ");";
                        System.out.println(cmd);
                        db.exec(cmd);
                    //}
                }
                System.out.println("COMMIT");
                db.exec("COMMIT;");
                System.out.println("SUCCESS");
            } catch ( SQLiteException | IOException ex) {
                Logger.getLogger(ImportWebDB.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                db.dispose();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportWebDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportWebDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

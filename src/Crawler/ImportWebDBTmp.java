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
public class ImportWebDBTmp {
    public static SQLiteConnection db = new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
    public static void main(String[] args){
        String base_dir = args.length > 0 ? args[0] : "data/crawler";
        String task_name = args.length > 1 ? args[1] : "task0001";
        String webdb_name = "webpageinfo.txt";
        String PathName = base_dir + "/" + task_name + "/" + webdb_name;
        try(MyRandomAccessFile raf = new MyRandomAccessFile(PathName, "r")){
            raf.readLong();
            String Line,cmd,url;
            String[] cols;
           
            try{
                db.open(true);
                db.exec("BEGIN;");
                System.out.println("BEGIN");
                raf.readLong();
                while((Line = raf.readLine()) != null){
                    cols = Line.split(",");
                    if(cols.length >= SiteCrawler.WebDBColumnWidth){
                        try {
                            cols[0] = (new MyURL("http://" + cols[0].substring(0, cols[0].length() - 1))).UniqURL;
                            Line = "\"" + cols[0] + "\"";
                            for(int i=1;i<cols.length;i++)
                                Line += "," + cols[i];
                            cmd = "INSERT OR IGNORE INTO webpage(url,language,file_size,comment_size,js_size,style_size,content_size) VALUES(" + Line + ");";
                            System.out.println(cmd);
                            db.exec(cmd);
                        } catch (Exception ex) {
                            Logger.getLogger(ImportWebDBTmp.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    System.err.println(Line);
                    try{
                        if(raf.getFilePointer() < raf.length())
                            raf.readLong();
                    }catch(IOException ex){
                        System.out.println("EOF EOF---");
                        break;
                    }
                }
                System.out.println("COMMIT");
                db.exec("COMMIT;");
                System.out.println("SUCCESS");
            } catch ( SQLiteException | IOException ex) {
                Logger.getLogger(ImportWebDBTmp.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                db.dispose();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImportWebDBTmp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ImportWebDBTmp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

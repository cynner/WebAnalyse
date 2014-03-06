/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import Crawler.MainCrawler;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class FixWebDB {
    
    public static void main(String[] args) throws InterruptedException{
        File fd = new File("data/crawldata3/db/");
        String WebDBName = "resource/webpage.sqlite3";
        SQLiteQueue webdbq = new SQLiteQueue(new File(WebDBName));
        webdbq.start();
        for(File f : fd.listFiles()){
            webdbq.execute(new Webjob(f));
        }
        webdbq.join();
        webdbq.stop(true);
    }
    
    public static class Webjob extends SQLiteJob<Object>{
        public File FileName;
        
        public Webjob(File FileName){
            this.FileName = FileName;
            
        } 
        
        @Override
        protected Object job(SQLiteConnection connection) throws Throwable {
            
                    String line;
                    connection.exec("BEGIN;");
                    try (RandomAccessFile br = new RandomAccessFile(FileName,"r")){
                        //"url","language",file_size,comment_size,js_size,style_size,content_size
                        while ((line = br.readUTF()) != null) {
                            if(!line.isEmpty()){
                                connection.exec("INSERT OR IGNORE INTO webpage(url,language,file_size,comment_size,js_size,style_size,content_size) VALUES(" + line + ");");
                            }
                        }
                        
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    connection.exec("COMMIT;");
                    
                    FileName.delete();
                    return null;
                    
        }
        
    }
}

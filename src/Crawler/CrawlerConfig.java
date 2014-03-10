/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.WebArcRecord;
import DBDriver.Qjob;
import LanguageUtils.LanguageDetector;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class CrawlerConfig implements AutoCloseable{
    
    public static enum Status {
        Finished(0), Crawling(1), NotBegin(9), NotInScope(-1), NoHostIP(-2), NoHostLocation(-3), Failed(-9) ;
        public final int value;
        private Status(int value){this.value = value;}
        public static Status GetKey(int val){
            for (Status d : Status.values())
                if(d.value == val)
                    return d;
            return null;
        }
    };
    
    public SQLiteQueue dbq;
    public SQLiteQueue webdbq;
    public int MaxPreCrawl;
    public int log_id;
    
    public CrawlerConfig(int MaxPreCrawl){
        this.MaxPreCrawl = MaxPreCrawl;
        dbq = new SQLiteQueue(DBDriver.TableConfig.FileWebSiteDB);
        dbq.start();
        webdbq = new SQLiteQueue(DBDriver.TableConfig.FileWebPageDB);
        webdbq.start();
    }
    
    public boolean isAccept(WebArcRecord f){
        return "th".equals(LanguageDetector.Detect(f.Doc.text()));
        //return true;
    }
    
    
    public void UpdateStatus(String HostName, CrawlerConfig.Status stat){
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ", lastupdate=datetime('now','localtime'), status=" + stat.value + " WHERE hostname='"+HostName+"';"));
    }
    
    
    public void UpdatePageCount(String HostName, int PageCount, CrawlerConfig.Status stat){
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ",  lastupdate=datetime('now','localtime'), page_count=" + PageCount + ", status=" + stat.value + " WHERE hostname='"+HostName+"';"));
    
    }
    
    public void UpdateIP(String HostName, String IP){
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ", lastupdate=datetime('now','localtime'), ip='" + IP + "' WHERE hostname='"+HostName+"';"));
    
    }
    
    public void UpdateIP(String HostName, String IP, CrawlerConfig.Status stat){
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ", lastupdate=datetime('now','localtime'), ip='" + IP + "', status=" + stat.value + " WHERE hostname='"+HostName+"';"));
    }
    
    public void UpdateLocation(String HostName, String Location){
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ", lastupdate=datetime('now','localtime'), location='" + Location + "' WHERE hostname='"+HostName+"';"));
    }
    
    
    public void dumpWebDB(File dbFile){
        if(dbFile.exists()){
            webdbq.execute(new Webjob(dbFile));
        }
    }

    @Override
    public void close() throws Exception {
        try {
            dbq.stop(true).join();
            webdbq.stop(true).join();
        } catch (InterruptedException ex) {
            Logger.getLogger(CrawlerConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    
    public class Webjob extends SQLiteJob<Object>{
        public File FDB;
        
        public Webjob(File FDB){
            this.FDB = FDB;
            
        } 
        
        @Override
        protected Object job(SQLiteConnection connection) throws Throwable {
            
                    String line;
                    connection.exec("BEGIN;");
                    try (BufferedReader br = new BufferedReader(new FileReader(FDB))){
                        //"url","language",file_size,comment_size,js_size,style_size,content_size
                        while ((line = br.readLine()) != null) {
                            if(!line.isEmpty()){
                                connection.exec("INSERT OR IGNORE INTO webpage(url,language,file_size,comment_size,js_size,style_size,content_size) VALUES(" + line + ");");
                            }
                        }
                        
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(CrawlerConfig.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(CrawlerConfig.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    connection.exec("COMMIT;");
                    
                    FDB.delete();
                    return null;
                    
        }
        
    }
    
 
}

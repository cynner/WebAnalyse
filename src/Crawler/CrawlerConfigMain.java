/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.BGZFCompress;
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
public class CrawlerConfigMain extends CrawlerConfig implements AutoCloseable{
    public SQLiteQueue dbq;
    public SQLiteQueue webdbq;
    
    public CrawlerConfigMain(){
        dbq = new SQLiteQueue(DBDriver.TableConfig.FileWebSiteDB);
        dbq.start();
        webdbq = new SQLiteQueue(DBDriver.TableConfig.FileWebPageDB);
        webdbq.start();
    }
    
   @Override
    public void CrawlerFinishing(SiteCrawler s){
        dumpWebDB(s.WebDBFile);
        BGZFCompress.Compress(s.ArcFile, new File(s.ArcFile.getAbsolutePath() + ".gz"));
        s.ArcFile.delete();
        UpdatePageCount(s.HostName, s.URLLoaded.size(), CrawlerConfig.Status.Finished);
    }
    
    
    
    @Override
    public boolean isAccept(SiteCrawler s){
        return "th".equals(s.curPageLanguage);
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

    @Override
    public void CheckerFinishing(SiteCrawler s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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


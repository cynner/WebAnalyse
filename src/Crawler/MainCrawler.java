/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import Crawler.CrawlerConfig.Status;
import LanguageUtils.LanguageDetector;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class MainCrawler{
    
    public String DBName = "resource/crawler.sqlite3";
    public String WebDBName = "resource/webpage.sqlite3";
    public static String DefaultSitePath = "data/crawldata2";
    
    
    //public int LimitCrawlSite = 10000;
    public int MaxPagePerSite = 1000;
    public int CacheSize = 1000;
    public int Threads = 10;
    public String Dirname;
    public String Selcond = "(hostname like '%.th')";//"(log_id is NULL OR location='TH')";// "log_id is NULL";
    public String Fixedcond = "status > 0";
    
    SQLiteConnection dbc;
    
    public ArrayList<String> HostNameQueue = new ArrayList<>();
    public ArrayList<String> HostIPQueue = new ArrayList<>();
    public ArrayList<String> LocationQueue = new ArrayList<>();
    public ArrayList<Status> StatusQueue = new ArrayList<>();
    
    public CrawlerConfigMain cfg;
    

    public MainCrawler(String DirName, int MaxPreCrawl, int MaxPagePerSite){
        //super(MaxPreCrawl);
        this.Dirname = DirName;
        this.MaxPagePerSite = MaxPagePerSite;
        cfg = new CrawlerConfigMain();
    }
    
    public static void main(String[] args) throws IOException{
        String Dir = args.length > 0 ? args[0] : DefaultSitePath;
        LanguageDetector.init();
        GeoIP.LoadToMem();
        
        MainCrawler mc = new MainCrawler(Dir,3,1000);
        
        //mc.ImportSeedSite(new File("Hop2.pure"));
        //mc.RunExampleStatement();
        //mc.MyImportSeedSite(new File("sumout.txt"));
        //mc.RunExampleSelect();
        
        File dir = new File(Dir);
        
        if(!dir.isDirectory()){
            if(dir.exists()){
                System.err.println(Dir + " is not a directory");
                System.exit(1);
            }else{
                System.out.println("Crawl data will stored in " + Dir);
                dir.mkdir();
            }
        }
        
        mc.run();
    }
    
    // 1. ImportSeedSite()
    // 2. Crawl() Crawl Loop
    //    2.1 Next() - Get Site where status != finished(2) and != failed(-1)
    //    2.2 Prefetch() - Check some download or heaader before crawl
    //                      if Wrong type remove file set status = failed(-1)
    //                      then go to 2.1 else go to 2.3 
    //    2.3 Fetching() - Change status to crawling(1) Crawl 2.1
    //    2.4 Finishing() - if finished change status to finished(2)
    
    public void run(){
        String Location,HostName,HostIP;
        Status stat;
        int cycle=1;
        
        ExecutorService executor;
        
        /*db = new SQLiteConnection(new File(DBName));
        try {
            db.open(false);
        } catch (SQLiteException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
            
        while(NextCache()){
            
            System.out.println("====== newFixedThreadPool cycle #" + cycle +" ======");
            
            executor = Executors.newFixedThreadPool(this.Threads);
            //System.out.println("gg");
                
            while(Next()){
                Runnable worker; 
                HostIP = HostIPQueue.remove(0);
                HostName = HostNameQueue.remove(0);
                Location = LocationQueue.remove(0);
                stat = StatusQueue.remove(0);
                System.out.println(HostName);
                if(HostIP == null){
                    HostIP = GeoIP.Domain2IP(HostName);
                    if(HostIP != null){
                        cfg.UpdateIP(HostName, HostIP);
                    }else{
                        cfg.UpdateStatus(HostName, Status.NoHostIP);
                        continue;
                    }
                }
                if(Location == null){
                    Location = GeoIP.IP2ISOCountry(HostIP);
                
                    if(Location != null){
                        cfg.UpdateLocation(HostName, Location);
                    }else{
                        cfg.UpdateStatus(HostName, Status.NoHostLocation);
                        continue;
                    }
                    
                }
            
                worker = new SiteCrawler(HostName, HostIP, new File(Dirname + "/" + HostName + ".arc"),new File(Dirname + "/" + HostName + ".info"), cfg, true);
                /*
                if(Location.equals("TH")){
                    worker = new SiteCrawler(HostName, HostIP, Dirname, MaxPagePerSite, "/",true,this, SiteCrawler.Mode.Crawl);
                }else{
                    continue;
                    /*
                    worker = new SiteCrawler(HostName, HostIP, Dirname, MaxPagePerSite, 
                       "/",true,this, 
                       ((stat == Status.Crawling) ? SiteCrawler.Mode.Crawl :  SiteCrawler.Mode.preCrawl));
                    // * /
                }
                */
                
                executor.execute(worker);
            }
            
            executor.shutdown();
            
            System.out.println("====== shutdown cycle #" + cycle +" ======");
            cycle++;
            while (!executor.isTerminated()) {
            }
            
            try {
                cfg.dbq.stop(true).join();
                cfg.webdbq.stop(true).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
            cfg.dbq = new SQLiteQueue(DBDriver.TableConfig.FileWebSiteDB);
            cfg.dbq.start();
            cfg.webdbq = new SQLiteQueue(DBDriver.TableConfig.FileWebPageDB);
            cfg.webdbq.start();
        }
    }
    
    
    
    
    public void ImportSeedSite(File Seed) throws FileNotFoundException, IOException{
        
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        //BufferedReader br;
        try (BufferedReader br = new BufferedReader(new FileReader(Seed))){
            try{
                db.open(true);
                String Line;
                String [] x;
            
                // Status 0 For not begin, 1 for crawling, 2 for finished, -1 for can't download.
                //db.exec("DROP TABLE host;");
                db.exec("BEGIN;");
           
                db.exec("CREATE TABLE IF NOT EXISTS website(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, hostname VARCHAR(255) UNIQUE NOT NULL, ip VARCHAR(40), location VARCHAR(2), page_count INTEGER, status TINYINT, lastupdate DATETIME);");
                //Line = br.readLine();
                //x = Line.split(":");
                
                //String query = "INSERT INTO website(hostname,ip,location) VALUES('"+x[2]+"','"+x[1] + (x[0].equals("null") ? "',null," : "'," + x[0] + "')");
                while( (Line = br.readLine()) != null){
                    System.out.println(Line);
                    db.exec("INSERT OR IGNORE INTO website(hostname,status) VALUES('" 
                            + Line + "',0);");
                }
                System.out.println("Exec. ");
            
                db.exec("COMMIT;");
                System.out.println("Finished. ");
            
            } catch ( SQLiteException | IOException ex) {
                Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                db.dispose();
            }
        }
    }
    
    public void MyImportSeedSite(File Seed) throws IOException{
        
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        
        try (BufferedReader br = new BufferedReader(new FileReader(Seed))){
            try{
                db.open(true);
                String Line;
                String [] x;
            
                // Status 0 For not begin, 1 for crawling, 2 for finished, -1 for can't download.
                //db.exec("DROP TABLE host;");
                db.exec("BEGIN;");
           
                db.exec("CREATE TABLE IF NOT EXISTS website(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, hostname VARCHAR(255) UNIQUE NOT NULL, ip VARCHAR(40), location VARCHAR(2), page_count INTEGER, status TINYINT);");
                //Line = br.readLine();
                //x = Line.split(":");
                
                //String query = "INSERT INTO website(hostname,ip,location) VALUES('"+x[2]+"','"+x[1] + (x[0].equals("null") ? "',null," : "'," + x[0] + "')");
                while( (Line = br.readLine()) != null){
                    x = Line.split(":");
                    System.out.println(Line);
                    db.exec("INSERT INTO website(hostname,ip,location) VALUES('" 
                            + x[2] 
                            + (x[1].equals("null") ? "',null" : "','" + x[1] + "'") 
                            + (x[0].equals("null") ? ",null);" : ",'" + x[0] + "');"));
                }
                System.out.println("Exec. ");
            
                db.exec("COMMIT;");
                System.out.println("Finished. ");
            
            } catch ( SQLiteException | IOException ex) {
                Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                db.dispose();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void RunExampleStatement(){
        
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        try {
            db.open(false);
            //db.exec("UPDATE website SET status=2 WHERE 1;");
            //db.exec("ALTER TABLE website ADD COLUMN lastupdate DATETIME;");
            //db.exec("UPDATE website SET lastupdate=datetime('now','localtime') WHERE status=2;");
            db.exec("UPDATE website SET status=9 WHERE lastupdate > '2013-12-06 20:58:00';");
            //db.exec("UPDATE website SET lastupdate=datetime('now','localtime'), status=-2 WHERE status=9 AND hostname LIKE '%.myfri3nd.com';");
            
        } catch (SQLiteException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            db.dispose();
        }
    }
    
    public void RunExampleSelect(){
        
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        try {
            db.open(true);
            //SQLiteStatement st = db.prepare("SELECT id,hostname FROM website WHERE ip is null;");
            //SQLiteStatement st = db.prepare("SELECT * FROM website WHERE status=-1 LIMIT 100;");
            //SQLiteStatement st = db.prepare("SELECT * FROM website WHERE lastupdate > '2013-12-06 17:07:00' LIMIT 100;");
            
            SQLiteStatement st = db.prepare("SELECT * FROM website WHERE lastupdate > '2013-12-06 19:07:00' LIMIT 100;");
            //SQLiteStatement st = db.prepare("SELECT * FROM website WHERE lastupdate not null LIMIT 5;");
            try {
                int cols = st.columnCount();
                for(int i=0;i<cols;i++){
                    System.out.print(st.getColumnName(i) + "|");
                }
                System.out.println();
                while (st.step()) {
                    for(int i=0;i<cols;i++){
                        System.out.print(st.columnValue(i) + "|");
                    }
                    System.out.println();
                    /*
                    String ISO,IP;
                    IP = GeoIP.Domain2IP(st.columnString(1));
                    if(IP != null){
                        if((ISO = GeoIP.IP2ISOCountry(IP)) != null){
                            System.out.println("UPDATE!!");
                            db.exec("UPDATE website SET location = '" + ISO + "', ip = '" + IP + "' WHERE id = " + st.columnValue(0) );
                        }
                    }
                    */
                }
            } finally {
                st.dispose();
            }
        } catch (SQLiteException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            db.dispose();
        }
    }
    
    
    
    public boolean NextCache() {
        cfg.dbq.execute(new SQLiteJob<Object>() {
            @Override
            protected Object job(SQLiteConnection connection) throws SQLiteException {
                if (HostNameQueue.isEmpty()) {
                    //SQLiteConnection db = new SQLiteConnection(new File(DBName));
                    try {
                //connection.open(false);
                        //SQLiteStatement st = db.prepare("SELECT id,hostname FROM website WHERE ip is null;");
                        String cond = Fixedcond + ((Selcond != null && !Selcond.isEmpty()) ? " AND " + Selcond : "");
                        SQLiteStatement st = connection.prepare("SELECT hostname,ip,location,status FROM website WHERE " + cond + " LIMIT " + CacheSize + ";");
                        try {
                            while (st.step()) {
                                HostNameQueue.add(st.columnString(0));
                                HostIPQueue.add(st.columnString(1));
                                LocationQueue.add(st.columnString(2));
                                StatusQueue.add(Status.GetKey(st.columnInt(3)));
                            }
                        } finally {
                            st.dispose();
                        }
                    } catch (SQLiteException ex) {
                        Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        //connection.dispose();
                    }
                }
                return null;
            }
        }).complete();
        return !HostNameQueue.isEmpty();
    }
    
    public boolean Next(){
        return !HostNameQueue.isEmpty();
    }
    
    
}



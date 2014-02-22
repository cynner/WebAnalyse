/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.WebArcRecord;
import LanguageUtils.LanguageDetector;
import com.almworks.sqlite4java.SQLiteBlob;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;
import com.cybozu.labs.langdetect.Command;
import com.cybozu.labs.langdetect.Detector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import projecttester.ProjectTester;
import static projecttester.ThreadPool.KEYs;
import static projecttester.ThreadPool.getNextKey;

/**
 *
 * @author malang
 */
public class MainCrawler {
    
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
    
    public String DBName = "resource/crawler.sqlite3";
    public String WebDBName = "resource/webpage.sqlite3";
    public static String DefaultSitePath = "data/crawldata2";
    
    
    //public int LimitCrawlSite = 10000;
    public int MaxPreCrawl = 3;
    public int MaxPagePerSite = 1000;
    public int CacheSize = 1000;
    public int Threads = 10;
    public int log_id = 2;
    public String Dirname;
    public String Selcond = "(log_id is NULL OR location='TH')";// "log_id is NULL";
    public String Fixedcond = "status > 0";
    SQLiteQueue dbq;
    SQLiteQueue webdbq;
    SQLiteConnection dbc;
    
    public ArrayList<String> HostNameQueue = new ArrayList<>();
    public ArrayList<String> HostIPQueue = new ArrayList<>();
    public ArrayList<String> LocationQueue = new ArrayList<>();
    public ArrayList<Status> StatusQueue = new ArrayList<>();
    

    public MainCrawler(String DirName, int MaxPreCrawl, int MaxPagePerSite){
        this.Dirname = DirName;
        this.MaxPagePerSite = MaxPagePerSite;
        this.MaxPreCrawl = MaxPreCrawl;
    }
    
    public static void main(String[] args) throws IOException{
        String Dir = args.length > 0 ? args[0] : DefaultSitePath;
        String DBDir = Dir + "/db";
        LanguageDetector.init();
        
        MainCrawler mc = new MainCrawler(Dir,3,1000);
        
        //mc.ImportSeedSite(new File("Hop2.pure"));
        //mc.RunExampleStatement();
        //mc.MyImportSeedSite(new File("sumout.txt"));
        //mc.RunExampleSelect();
        
        File dir = new File(Dir);
        File DBDirf = new File(DBDir);
        
        if(!dir.isDirectory()){
            if(dir.exists()){
                System.err.println(Dir + " is not a directory");
                System.exit(1);
            }else{
                System.out.println("Crawl data will stored in " + Dir);
                dir.mkdir();
            }
        }
        
        if(!DBDirf.exists()){
            DBDirf.mkdir();
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
        
        dbq = new SQLiteQueue(new File(DBName));
        dbq.start();
        webdbq = new SQLiteQueue(new File(WebDBName));
        webdbq.start();
            
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
                        UpdateIP(HostName, HostIP);
                    }else{
                        UpdateStatus(HostName, Status.NoHostIP);
                        continue;
                    }
                }
                if(Location == null){
                    Location = GeoIP.IP2ISOCountry(HostIP);
                
                    if(Location != null){
                        UpdateLocation(HostName, Location);
                    }else{
                        UpdateStatus(HostName, Status.NoHostLocation);
                        continue;
                    }
                    
                }
            
                if(Location.equals("TH")){
                    worker = new SiteCrawler(HostName, HostIP, Dirname, MaxPagePerSite, "/",true,this, SiteCrawler.Mode.Crawl);
                }else{
                    continue;
                    /*
                    worker = new SiteCrawler(HostName, HostIP, Dirname, MaxPagePerSite, 
                       "/",true,this, 
                       ((stat == Status.Crawling) ? SiteCrawler.Mode.Crawl :  SiteCrawler.Mode.preCrawl));
                    */
                
                }
                
                executor.execute(worker);
            }
            
            executor.shutdown();
            
            System.out.println("====== shutdown cycle #" + cycle +" ======");
            cycle++;
            while (!executor.isTerminated()) {
            }
            
            try {
                dbq.stop(true).join();
                webdbq.stop(true).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
            dbq = new SQLiteQueue(new File(DBName));
            dbq.start();
            webdbq = new SQLiteQueue(new File(WebDBName));
            webdbq.start();
        }
        try {
            dbq.stop(true).join();
            webdbq.stop(true).join();
        } catch (InterruptedException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    // Can't remove
    public boolean isAccept(WebArcRecord f){
        return "th".equals(LanguageDetector.Detect(f.Doc.text()));
        //return true;
    }
    
    public class Qjob extends SQLiteJob<Object>{
        public String Command;
        
        public Qjob(String Command){
            this.Command = Command;
            
        } 
        
        @Override
        protected Object job(SQLiteConnection connection) throws Throwable {
            connection.exec(Command);
            return null;
        }
        
    }
    
    public void UpdateStatus(String HostName, Status stat){
        /*
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        try {
            db.open(false);
            db.exec("UPDATE website SET lastupdate=datetime('now','localtime'), status=" + stat.value + " WHERE hostname='"+HostName+"';");
        } catch (SQLiteException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.dispose();
        */
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ", lastupdate=datetime('now','localtime'), status=" + stat.value + " WHERE hostname='"+HostName+"';"));
    }
    
    
    
    
    public void UpdatePageCount(String HostName, int PageCount, Status stat){
        /*
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        try {
            db.open(false);
            db.exec("UPDATE website SET lastupdate=datetime('now','localtime'), page_count=" + PageCount + ", status=" + stat.value + " WHERE hostname='"+HostName+"';");
        } catch (SQLiteException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.dispose();
        */
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ",  lastupdate=datetime('now','localtime'), page_count=" + PageCount + ", status=" + stat.value + " WHERE hostname='"+HostName+"';"));
    
    }
    
    public void UpdateIP(String HostName, String IP){
        /*
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        try {
            db.open(false);
            db.exec("UPDATE website SET lastupdate=datetime('now','localtime'), ip='" + IP + "' WHERE hostname='"+HostName+"';");
        } catch (SQLiteException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.dispose();
        */
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ", lastupdate=datetime('now','localtime'), ip='" + IP + "' WHERE hostname='"+HostName+"';"));
    
    }
    
    public void UpdateIP(String HostName, String IP, Status stat){
        /*
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        try {
            db.open(false);
            db.exec("UPDATE website SET lastupdate=datetime('now','localtime'), ip='" + IP + "', status=" + stat.value + " WHERE hostname='"+HostName+"';");
        } catch (SQLiteException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.dispose();
        */
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ", lastupdate=datetime('now','localtime'), ip='" + IP + "', status=" + stat.value + " WHERE hostname='"+HostName+"';"));
    }
    
    public void UpdateLocation(String HostName, String Location){
        /*
        SQLiteConnection db = new SQLiteConnection(new File(DBName));
        try {
            db.open(false);
            db.exec("UPDATE website SET lastupdate=datetime('now','localtime'), location='" + Location + "' WHERE hostname='"+HostName+"';");
        } catch (SQLiteException ex) {
            Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        db.dispose();
        */
        dbq.execute(new Qjob("UPDATE website SET log_id=" + log_id + ", lastupdate=datetime('now','localtime'), location='" + Location + "' WHERE hostname='"+HostName+"';"));
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
        dbq.execute(new SQLiteJob<Object>() {
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
    
    
    public File FDB;
    public void dumpWebDB(String HostName){
        FDB = new File(Dirname + "/db/." + HostName + ".tmpDB");
        
        
        if(FDB.exists()){
            webdbq.execute(new Webjob(FDB));
            FDB.deleteOnExit();
        }
    }
 
    public class Webjob extends SQLiteJob<Object>{
        public File FileName;
        
        public Webjob(File FileName){
            this.FileName = FileName;
            
        } 
        
        @Override
        protected Object job(SQLiteConnection connection) throws Throwable {
            
                    String line;
                    BufferedReader br = null;
                    connection.exec("BEGIN;");
                    try {
                        br = new BufferedReader(new FileReader(FileName));
                        //"url","language",file_size,comment_size,js_size,style_size,content_size
                        while ((line = br.readLine()) != null) {
                            if(!line.isEmpty()){
                                connection.exec("INSERT OR IGNORE INTO webpage(url,language,file_size,comment_size,js_size,style_size,content_size) VALUES(" + line + ");");
                            }
                        }
                        
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MainCrawler.class.getName()).log(Level.SEVERE, null, ex);
                    } finally{
                        if(br!=null)
                            br.close();
                    }
                    connection.exec("COMMIT;");
                    return null;
                    
        }
        
    }
}



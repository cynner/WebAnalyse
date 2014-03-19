/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.MyRandomAccessFile;
import static Crawler.GeoIP.Domain2IP;
import LanguageUtils.LanguageDetector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class MainCrawlerList{
    public static final String DefaultWorkingDirectory = "data/crawler";
    
    // Config for CrawlerConfigList
    public static final String subDirTmp = "tmp";
    public static final String PrefixArc = "crawl-";
    public static final String SuffixArc = ".arc";
    public static final String PrefixInfo = "web-";
    public static final String SuffixInfo = ".info";
    public static final String strSeed = "seed.txt";
    public final String strWorkingDirectory;
    public final String TaskName;
    public final File fileSeed;
    public final String strDirTmp;
    
    public CrawlerConfig.Mode mode = CrawlerConfig.Mode.Crawl;
    public String AcceptOnlyPrefixPath;
    public int MaxPreCrawl = 3;
    public int log_id = 9;
    
    
    //public int LimitCrawlSite = 10000;
    public int Threads = 10;
    
    public CrawlerConfigList cfg;
    
    public static String getSeedPath(String strWorkingDirectory, String TaskName){
        return strWorkingDirectory + "/" + TaskName + "/" + strSeed;
    }

    public MainCrawlerList(String TaskName, String strWorkingDirectory, int MaxPagePerSite, String AcceptOnlyPrefixPath) throws IOException{
        //super(MaxPreCrawl);
        this.TaskName = TaskName;
        this.strWorkingDirectory = strWorkingDirectory;
        this.strDirTmp = strWorkingDirectory + "/" + subDirTmp ;
        this.AcceptOnlyPrefixPath = AcceptOnlyPrefixPath;
        
        cfg = new CrawlerConfigList(TaskName, strWorkingDirectory);
        cfg.AcceptOnlyPrefixPath = this.AcceptOnlyPrefixPath;
        cfg.MaxPreCrawl = this.MaxPreCrawl;
        cfg.AcceptOnlyPrefixPath = this.AcceptOnlyPrefixPath;
        cfg.MaxPage = MaxPagePerSite;
        cfg.MarginPage = MaxPagePerSite * 3;
        cfg.log_id = this.log_id;
        this.fileSeed = new File(getSeedPath(strWorkingDirectory, TaskName));
    }
    
    public static void Import(String TaskName, String strWorkingDirectory, File fileOrgSeed) throws IOException {
        File fileSeed = new File(getSeedPath(strWorkingDirectory, TaskName));
        if(fileSeed.exists())
            throw new IOException("Seed File Exists.");
        File fileDir = fileSeed.getParentFile();
        if(!fileDir.isDirectory()){
            if(fileDir.exists()){
                throw new IOException("Can't create directory " + fileDir.getCanonicalPath());
            }else{
                fileDir.mkdirs();
            }
        }
        Files.copy(fileOrgSeed.toPath(), new FileOutputStream(fileSeed));
    }
    
    public static void main(String[] args) throws IOException{
        String Dir = args.length > 0 ? args[0] : DefaultWorkingDirectory;
        String TaskName = args.length > 1 ? args[1] : "task-0003";
        String strFileSeed = args.length > 2 ? args[2] : (args.length > 0 ? null : "seed0001.txt");
        LanguageDetector.init();
        GeoIP.LoadToMem();
        if(strFileSeed != null){
            if(!(new File(getSeedPath(DefaultWorkingDirectory, TaskName))).exists())
                MainCrawlerList.Import(TaskName, Dir, new File(strFileSeed));
        }
        MainCrawlerList mc = new MainCrawlerList(TaskName,Dir,1000,"/");
        
        //mc.ImportSeedSite(new File("Hop2.pure"));
        //mc.RunExampleStatement();
        //mc.MyImportSeedSite(new File("sumout.txt"));
        //mc.RunExampleSelect();
        
        
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
    
    public void run() throws IOException{
        String HostName;
        ExecutorService executor;
        File fArc,fInfo;
        
        File fileDirTmp = new File(strDirTmp);
        if(!fileDirTmp.isDirectory()){
            if(!fileDirTmp.exists()){
                fileDirTmp.mkdirs();
            }else{
                throw new IOException("Can't create directory " + strDirTmp + ".");
            }
        }
        
        HashSet<String> hs = new HashSet<>();
        if(cfg.fileHostCrawled.exists()){
            long len;
            try(MyRandomAccessFile raf = new MyRandomAccessFile(cfg.fileHostCrawled, "r")){
                // just skip
                len = raf.readLong();
                while(raf.getFilePointer() < len){
                    hs.add(raf.readLine());
                }
            } catch (IOException ex){
                 Logger.getLogger(MainCrawlerList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try(BufferedReader br = new BufferedReader (new FileReader(fileSeed))){
            System.out.println("====== Starting ======");
            
            executor = Executors.newFixedThreadPool(this.Threads);
                
            
            
            while((HostName = br.readLine()) != null){

                if(!hs.contains(HostName)){
                    String Location, HostIP;
                    HostIP = Domain2IP(HostName);
                    CrawlerConfig.Status status;
                    if(HostIP == null){
                        status = CrawlerConfig.Status.NoHostIP;
                        cfg.UpdateHostInfo(HostName, null, null, status, 0);
                        cfg.addCrawledList(HostName);
                    }else{
                        Location = GeoIP.IP2ISOCountry(HostIP);
                        if(Location == null){
                            status = CrawlerConfig.Status.NoHostLocation;
                            cfg.UpdateHostInfo(HostName, HostIP, null, status, 0);
                            cfg.addCrawledList(HostName);
                        }else if(!Location.equals("TH") && !HostName.endsWith(".th")){
                            status = CrawlerConfig.Status.NotInScope;
                            cfg.UpdateHostInfo(HostName, HostIP, Location, status, 0);
                            cfg.addCrawledList(HostName);
                        }else{
                            fArc = new File(strDirTmp + "/" + PrefixArc + HostName + SuffixArc);
                            fInfo = new File(strDirTmp + "/" + PrefixInfo + HostName + SuffixInfo);
                            Runnable worker = new SiteCrawler(HostName, null, fArc,fInfo, cfg, true);
                            executor.execute(worker);
                        }
                    }
                }
            }
            
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            
            System.out.println("====== Success ======");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainCrawlerList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainCrawlerList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}



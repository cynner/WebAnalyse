/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.MyRandomAccessFile;
import LanguageUtils.LanguageDetector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author malang
 */
public class Main{
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
    
    public String AcceptOnlyPrefixPath;
    public int MaxPreCrawl = 3;
    public int log_id = 9;
    
    public final int Threads;
    
    //public int LimitCrawlSite = 10000;
    
    public CrawlerConfigList cfg;
    
    public static String getSeedPath(String strWorkingDirectory, String TaskName){
        return strWorkingDirectory + "/" + TaskName + "/" + strSeed;
    }

    public Main(String TaskName, String strWorkingDirectory, int MaxPagePerSite, int Threads, int Delay, int DelayFail, String AcceptOnlyPrefixPath) throws IOException {
        //super(MaxPreCrawl);
        this.TaskName = TaskName;
        this.strWorkingDirectory = strWorkingDirectory;
        this.strDirTmp = strWorkingDirectory + "/" + subDirTmp;
        this.AcceptOnlyPrefixPath = AcceptOnlyPrefixPath;

        cfg = new CrawlerConfigList(TaskName, strWorkingDirectory);
        cfg.AcceptOnlyPrefixPath = this.AcceptOnlyPrefixPath;
        cfg.MaxPreCrawl = this.MaxPreCrawl;
        cfg.AcceptOnlyPrefixPath = this.AcceptOnlyPrefixPath;
        cfg.MaxPage = MaxPagePerSite;
        cfg.MarginPage = MaxPagePerSite * 3;
        cfg.log_id = this.log_id;
        cfg.CrawlDelay = Delay;
        cfg.CrawlDelayFail = DelayFail;
        this.Threads = Threads;
        this.fileSeed = new File(getSeedPath(strWorkingDirectory, TaskName));
    }
    
    public void Delete(File fileOrgSeed) throws IOException {
        String Line;
        ArrayList<String> hs = new ArrayList<>();
        int n=0,rep=0;
        if(fileOrgSeed.exists() && fileSeed.exists()){
            try(BufferedReader br = new BufferedReader(new FileReader(fileSeed))){
                while((Line = br.readLine()) != null){
                    hs.add(Line);
                }
            } catch (IOException ex){
                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try(BufferedReader br = new BufferedReader(new FileReader(fileOrgSeed))){
                while((Line = br.readLine()) != null){
                    if(hs.remove(Line))
                        n++;
                    else
                        rep++;
                }
            } catch (IOException ex){
                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try(BufferedWriter bw = new BufferedWriter(new FileWriter(fileSeed, false))){
                for(String s : hs){
                    bw.write(s + "\n");   
                }
            } catch (IOException ex){
                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Deleted " + n + " seed site");
        System.out.println("Delete Fail " + rep + " site");
    }
    
    public static void Import(String TaskName, String strWorkingDirectory, File fileOrgSeed) throws IOException {
        File fileSeed = new File(getSeedPath(strWorkingDirectory, TaskName));
        File fileDir = fileSeed.getParentFile();
        if (!fileDir.isDirectory()) {
            if (fileDir.exists()) {
                throw new IOException("Can't create directory " + fileDir.getCanonicalPath());
            } else {
                fileDir.mkdirs();
            }
        }
        HashSet<String> websitelist = new HashSet<>();
        String Line;
        int n=0,rep=0;
        if(fileSeed.isFile()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileSeed))) {
                while ((Line = br.readLine()) != null) {
                    if (!Line.isEmpty()) {
                        websitelist.add(Line);
                    }
                }
            }
        }
        try(BufferedReader br = new BufferedReader(new FileReader(fileOrgSeed));
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileSeed, true))) {
            while ((Line = br.readLine()) != null) {
                if (!Line.isEmpty() && !websitelist.contains(Line)) {
                    websitelist.add(Line);
                    bw.write(Line + "\n");
                    n++;
                }else{
                    rep++;
                }
            }
        }
        System.out.println("Added " + n + " seed site");
        System.out.println("Repeat " + rep + " was droped");
    }
    
    public void Reload(File fileOrgSeed) throws IOException {
        String Line;
        ArrayList<String> hs = new ArrayList<>();
        int n=0,rep=0;
        if(cfg.fileHostCrawled.exists()){
            long len;
            try(MyRandomAccessFile raf = new MyRandomAccessFile(cfg.fileHostCrawled, "r")){
                // just skip
                len = raf.readLong();
                while(raf.getFilePointer() < len){
                    hs.add(raf.readLine());
                }
            } catch (IOException ex){
                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try(BufferedReader br = new BufferedReader(new FileReader(fileOrgSeed));
                MyRandomAccessFile raf = new MyRandomAccessFile(cfg.fileHostCrawled, "rw")) {
            while ((Line = br.readLine()) != null) {
                if (hs.remove(Line)) {
                    n++;
                }else{
                    rep++;
                }
            }
            raf.seek(Long.SIZE);
            for(String s : hs){
                raf.write((s + "\n").getBytes());
            }
            long pos = raf.getFilePointer();
            raf.seek(0);
            raf.writeLong(pos);
        }
        System.out.println("Re-Added " + n + " seed site");
        System.out.println("No effect " + rep + " site");
    }
    
    public static void main(String[] args) throws IOException{
        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");
        
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Crawler.Main").defaultHelp(true)
                .description("Crawler process from list.");
        parser.addArgument("-d")
                .dest("dir")
                .metavar("Dir")
                .type(String.class)
                .setDefault(DefaultWorkingDirectory)
                .help("Working Directory for crawler");
        parser.addArgument("-i","--input")
                .dest("input")
                .metavar("SeedFile")
                .type(String.class)
                .help("import or append seed file in one line one domain format");
        parser.addArgument("-r","--reload")
                .dest("reload")
                .metavar("SeedFile")
                .type(String.class)
                .help("import reload seed file in one line one domain format");
        parser.addArgument("--del")
                .dest("delete")
                .metavar("SeedFile")
                .type(String.class)
                .help("delete seed file in one line one domain format");
        parser.addArgument("-t")
                .dest("threads")
                .metavar("Threads")
                .type(Integer.class)
                .setDefault(10)
                .help("threads no");
        parser.addArgument("-p")
                .dest("limit_page")
                .metavar("LimitPage")
                .type(Integer.class)
                .setDefault(1000)
                .help("limit page per site");
        parser.addArgument("--delay")
                .dest("delay")
                .type(Integer.class)
                .setDefault(1000)
                .help("crawl delay in ms.");
        parser.addArgument("--delayfail")
                .dest("delayfail")
                .type(Integer.class)
                .setDefault(200)
                .help("crawl delay if fail in ms.");
        parser.addArgument("--start")
                .dest("start")
                .action(Arguments.storeTrue())
                .help("Start crawler");
        parser.addArgument("--dns")
                .dest("dns")
                .metavar("DNS")
                .type(String.class)
                .help("Set specific DNS server ip");
        parser.addArgument("TaskName")
                .nargs("?")
                .type(String.class)
                .setDefault("default")
                .help("To identify job");
        try {
            Namespace res = parser.parseArgs(args);
            String strWorkDir = res.getString("dir");
            String TaskName = res.getString("TaskName");
            String strImportFile = res.getString("input");
            if (strImportFile != null){
                // import seed file
                File fi = new File(strImportFile);
                if(fi.isFile()){
                    Import(TaskName, strWorkDir, fi);
                }else{
                    System.err.println(strImportFile + " is not a seed file.");
                    System.exit(1);
                }
            }
            
            Main mc = new Main(TaskName, strWorkDir, res.getInt("limit_page"),res.getInt("threads"),res.getInt("delay"),res.getInt("delayfail"), "/");
            
            if (res.get("reload") != null){
                File fi = new File(res.getString("reload"));
                if(fi.isFile()){
                    mc.Reload(fi);
                }else{
                    System.err.println(res.getString("reload") + " is not a seed file.");
                    System.exit(1);
                }
            }
            
            if (res.get("delete") != null){
                File fi = new File(res.getString("delete"));
                if(fi.isFile()){
                    mc.Delete(fi);
                }else{
                    System.err.println(res.getString("delete") + " is not a seed file.");
                    System.exit(1);
                }
            }
            
            if (res.getBoolean("start")) {
                if(res.getString("dns") != null){
                    System.out.println("Setting DNS to : " + res.getString("dns"));
                    System.setProperty("sun.net.spi.nameservice.nameservers", res.getString("dns"));
                    System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
                }
                LanguageDetector.init();
                mc.run();
            }
            
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
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
                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try(BufferedReader br = new BufferedReader (new FileReader(fileSeed))){
            System.out.println("====== Starting ======");
            
            executor = Executors.newFixedThreadPool(this.Threads);
                
            
            
            while((HostName = br.readLine()) != null){

                if (!hs.contains(HostName)) {
                    CrawlerConfig.Status status;
                    fArc = new File(strDirTmp + "/" + PrefixArc + HostName + SuffixArc);
                    fInfo = new File(strDirTmp + "/" + PrefixInfo + HostName + SuffixInfo);

                    Runnable worker = new SiteCrawler(HostName, "0.0.0.0", fArc, fInfo, cfg, true);
                    executor.execute(worker);
                }
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            
            System.out.println("====== Success ======");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import ArcFileUtils.MyRandomAccessFile;
import static Crawler.Main.DefaultWorkingDirectory;
import static Crawler.Main.Import;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import projecttester.ProjectTester;
import LanguageUtils.LanguageDetector;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author malang
 */
public class SiteChecker {


    public static String UserAgent = "princeofvamp@gmail.com";
    public static final int DefaultMarginPage = 20;
    public static final int DefaultThreadNo = 20;
    public static int ThreadNo;


    public static void main(String[] args) throws IOException {
        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");
        
        ArgumentParser parser = ArgumentParsers.newArgumentParser("SiteChecker").defaultHelp(true)
                .description("Check site info.");
        parser.addArgument("-i","--input")
                .dest("input")
                .metavar("SiteFile")
                .type(String.class)
                .required(true)
                .help("file site to be check contains one line one site");
        parser.addArgument("-db","--database")
                .dest("db_site")
                .metavar("DBSite")
                .type(String.class)
                .required(true)
                .help("result site database info");
        parser.addArgument("-o")
                .dest("output")
                .metavar("OutFile")
                .type(String.class)
                .help("result site to be crawl");
        parser.addArgument("-t")
                .dest("threads")
                .metavar("Threads")
                .type(Integer.class)
                .setDefault(DefaultThreadNo)
                .help("threads no");
        parser.addArgument("-p")
                .dest("max_pre_crawl")
                .metavar("MaxPreCrawl")
                .type(Integer.class)
                .setDefault(CrawlerConfig.DefaultMaxPreCrawl)
                .help("limit page pre-crawl per site");
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
        parser.addArgument("--dns")
                .dest("dns")
                .metavar("DNS")
                .type(String.class)
                .help("Set specific DNS server ip");
        try {
            Namespace res = parser.parseArgs(args);
            String strImportFile = res.getString("input");
            String strExportFile = res.getString("output");
            File DBSite = new File(res.getString("db_site"));
            try(CrawlerConfigList cfg = new CrawlerConfigList(DBSite)){
                cfg.MaxPreCrawl = res.get("max_pre_crawl");
                cfg.MarginPage = DefaultMarginPage;
                cfg.CrawlDelay = res.get("delay");
                cfg.CrawlDelayFail = res.get("delayfail");
                ThreadNo = DefaultThreadNo;
                MainRun(cfg, new File(strImportFile));
            }
            if(strExportFile != null){
                System.out.println("Writing output file...");
                SQLiteConnection db = new SQLiteConnection(DBSite);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(strExportFile))){
                    db.openReadonly();
                    SQLiteStatement stmt = db.prepare("SELECT hostname FROM website WHERE status=" + CrawlerConfig.Status.NotBegin.value + ";");
                    try {
                        while(stmt.step()){
                            bw.write(stmt.columnString(0) + "\n");
                        }
                    }finally{
                        stmt.dispose();
                    }
                } catch (SQLiteException ex) {
                    Logger.getLogger(SiteChecker.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    db.dispose();
                }
                
                System.out.println("Finished");
            }
            
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

    public static void MainRun(CrawlerConfigList cfg, File FileSeed) throws IOException {
        ExecutorService executor;
        String HostName;
        GeoIP.LoadToMem();
        LanguageDetector.init();
        
        try (BufferedReader br = new BufferedReader(new FileReader(FileSeed))) {
            System.out.println("====== Starting ======");

            executor = Executors.newFixedThreadPool(ThreadNo);
            while ((HostName = br.readLine()) != null) {
                Runnable worker = new SiteCrawler(HostName, cfg);
                executor.execute(worker);
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            System.out.println("====== Success ======");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SiteChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SiteChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

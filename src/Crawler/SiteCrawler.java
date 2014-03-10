/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import ArcFileUtils.MyRandomAccessFile;
import ArcFileUtils.WebArcReader;
import ArcFileUtils.WebArcRecord;
import ArcFileUtils.WebArcWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.BlockCompressedOutputStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import projecttester.ProjectTester;
import ArcFileUtils.WebUtils;
import LanguageUtils.LanguageDetector;
import com.almworks.sqlite4java.SQLiteQueue;

/**
 *
 * @author malang
 */
public class SiteCrawler implements Runnable {

    public SQLiteQueue dbq;

    public boolean isSetTimeZone = false;
    public static String MyIP = GetMyIP();
    public static String UserAgent = "princeofvamp@gmail.com";
    public int CrawlDelay = 100;
    public String HostName;
    public String HostIP;
    public String AcceptOnlyPrefixPath = "/";
    public int MarginPage;
    public int MaxPage;
    public ArrayList<String> URLQueue = new ArrayList<>();
    public ArrayList<String> URLCrash = new ArrayList<>();
    public ArrayList<String> URLLoaded = new ArrayList<>();
    public Fetcher Fetch;
    private Robotstxt robots;
    //private BufferedWriter bw;
    private boolean isAppend = true;
    //private boolean isOpenFile = false;
    public File ArcGZFile;
    public File ArcFile;
    public File TmpFile;
    public File WebDBFile;

    public MyRandomAccessFile rafWebDB;
    public int WebDBColumnWidth = 7;
    public WebArcWriter waw = null;
    public WebArcRecord record;
    public boolean PrintEnqueue = false;

    public CrawlerConfig crawlConf = null;

    public static enum Mode {

        preCrawl, Crawl
    };
    public Mode mode = Mode.Crawl;

    private final WebUtils wu = new WebUtils();

    public static String GetMyIP() {
        String s = null;
        try {
            s = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            System.err.println("Unknown Local IP");
        }
        return s;
    }

    public SiteCrawler(String HostName, String HostIP, File ArcFile, int MaxPage, String AcceptOnlyPrefixPath, boolean isAppend, CrawlerConfig crawlConf, Mode mode) {
        this.HostName = HostName;
        this.MaxPage = MaxPage;
        this.MarginPage = (int) (this.MaxPage * 3);
        this.isAppend = isAppend;
        this.crawlConf = crawlConf;
        this.mode = mode;
        this.AcceptOnlyPrefixPath = AcceptOnlyPrefixPath;
        this.ArcFile = ArcFile;
        this.TmpFile = new File(ArcFile.getParent() + "/." + ArcFile.getName() + ".tmp");
        this.ArcGZFile = new File(ArcFile.getPath() + ".gz");
        this.WebDBFile = new File(TmpFile.getPath() + "DB");
        if(!isSetTimeZone){
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            isSetTimeZone = true;
        }
        
        if (HostIP != null) {
            this.HostIP = HostIP;
        } else {
            try {
                InetAddress address = InetAddress.getByName(HostName);
                this.HostIP = address.getHostAddress();
                if (crawlConf != null) {
                    crawlConf.UpdateIP(this.HostName, this.HostIP);
                }
            } catch (UnknownHostException ex) {
                this.HostIP = null;
            }
        }
    }

    public SiteCrawler(String HostName, String DirName, int MaxPage, String AcceptOnlyPrefixPath, boolean isAppend) {
        this(HostName, null, new File(DirName + "/crawl-" + HostName + ".arc"), MaxPage, AcceptOnlyPrefixPath, isAppend, null, Mode.Crawl);
    }
    
    public SiteCrawler(String HostName, String DirName, int MaxPage, String AcceptOnlyPrefixPath, boolean isAppend, CrawlerConfig mc) {
        this(HostName, null, new File(DirName + "/crawl-" + HostName + ".arc"), MaxPage, AcceptOnlyPrefixPath, isAppend, mc, Mode.Crawl);
    }

    public SiteCrawler(String HostName, String HostIP, String DirName, int MaxPage, String AcceptOnlyPrefixPath, boolean isAppend) {
        this(HostName, HostIP, new File(DirName + "/crawl-" + HostName + ".arc"), MaxPage, AcceptOnlyPrefixPath, isAppend, null, Mode.Crawl);
    }

    public SiteCrawler(String HostName, String HostIP, String DirName, int MaxPage, String AcceptOnlyPrefixPath, boolean isAppend, CrawlerConfig mc, Mode mode) {
        this(HostName, HostIP, new File(DirName + "/crawl-" + HostName + ".arc"), MaxPage, AcceptOnlyPrefixPath, isAppend, mc, mode);
    }
    
    

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " " + HostName + " Start.");
        if (this.HostIP != null) {

            processCommand();
        } else if (crawlConf != null) {
            crawlConf.UpdateStatus(HostName, CrawlerConfig.Status.NoHostIP);
        }
        System.out.println(Thread.currentThread().getName() + " " + HostName + " End.");
    }

    private void processCommand() {
        this.Fetch = new Fetcher(UserAgent);
        this.robots = new Robotstxt(HostName, UserAgent);
        boolean tmpExists = TmpFile.exists(), arcExists = ArcFile.exists();

        // 1. Load Robots.txt
        Fetch.fetch("http://" + HostName + "/robots.txt");
        if (Fetch.Details.WebContent != null) {
            this.robots.AnalyseRobots(Fetch.Details.WebContent);
        }

        if (URLQueue.isEmpty()) {
            URLQueue.add("http://" + HostName + AcceptOnlyPrefixPath);
        }

        if (!tmpExists && arcExists) {
            this.ArcFile.renameTo(TmpFile);
            tmpExists = true;
            arcExists = false;
        }

        try{
            rafWebDB = new MyRandomAccessFile(WebDBFile,"rw");
            if (this.isAppend && tmpExists) {
                this.waw = new WebArcWriter(this.TmpFile, ReadFile());
            } else {
                this.waw = new WebArcWriter(this.TmpFile, this.ArcFile.getName(), this.isAppend, this.HostIP);
            }
        } catch (IOException ex) {
            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
            try{
                if(rafWebDB != null){
                    rafWebDB.close();
                }
                if(waw !=null){
                    waw.close();
                }
            } catch (IOException ex1) {
                Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return;
        }
        
        try {
            if (crawlConf != null && mode == Mode.preCrawl) {

                if (preCrawl()) {
                    crawlConf.UpdateStatus(HostName, CrawlerConfig.Status.Crawling);
                } else {
                    try {
                        this.waw.close();
                        this.rafWebDB.close();
                    } catch (IOException ex) {
                        Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    this.TmpFile.delete();
                    crawlConf.UpdateStatus(HostName, CrawlerConfig.Status.NotInScope);
                    removeUpdateDB();
                    return;
                }
            }
            Crawl();
        } finally {
            try {
                this.waw.close();
                this.rafWebDB.close();
            } catch (IOException ex) {
                Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if(crawlConf != null){
            crawlConf.dumpWebDB(WebDBFile);
        }
        if (arcExists) {
            this.ArcFile.delete();
        }
        this.TmpFile.renameTo(ArcFile);
        Compress(ArcFile, ArcGZFile);
        this.ArcFile.delete();

        if (crawlConf != null) {
            crawlConf.UpdatePageCount(HostName, URLLoaded.size(), CrawlerConfig.Status.Finished);
        }

    }

    public boolean preCrawl() {
        String Url;
        while (!URLQueue.isEmpty() && URLLoaded.size() < crawlConf.MaxPreCrawl) {
            // 2.1 Fetch
            Url = URLQueue.get(0);
            System.out.println("Fetch: " + Url);
            // 2.2 Compress html Extractlink & write to file
            if (Fetch.getHeader(Url)) {
                if (isAllowedResponseCode(Fetch.ResponseCode) && isAllowedHeader()) {
                    if (Fetch.getDocument()) {
                        Fetch.Details.WebContent = wu.HTMLCompress(Fetch.Details.Doc);
                        LinkFromRedir(Url, Fetch.Details.Doc);
                        AnalyseLink(Url, Fetch.Details.Doc);
                        URLLoaded.add(URLQueue.remove(0));
                        writeUpdateDB(Url, LanguageDetector.Detect(Fetch.Details.Doc.text()));
                        waw.WriteRecord(Fetch.Details);
                        try {
                            //Success & delay
                            Thread.sleep(this.CrawlDelay);
                        } catch (InterruptedException ex) {
                            System.err.println("Interupt while delay!...");
                            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (crawlConf.isAccept(Fetch.Details)) {
                            return true;
                        }
                    } else {
                        URLCrash.add(URLQueue.remove(0));
                    }
                } else {
                    if (!CodeHandler()) {
                        break;
                    }
                }
            } else {
                // Unknown and add to crash
                URLCrash.add(URLQueue.remove(0));
            }
        }
        return false;
    }

    public byte mySizeData;
    //public static byte mySizeSize;
    public int SizeOriginal;
    public int SizeData;
    public int SizeC;

    public void Crawl() {
        String Url;
        while (!URLQueue.isEmpty() && URLLoaded.size() < MaxPage) {
            // 2.1 Fetch
            Url = URLQueue.get(0);
            System.out.println("Fetch: " + Url);
            // 2.2 Compress html Extractlink & write to file
            if (Fetch.getHeader(Url)) {
                if (isAllowedResponseCode(Fetch.ResponseCode) && isAllowedHeader()) {
                    if (Fetch.getDocument()) {
                        //System.out.println(Fetch.Details.charset);
                        //System.err.println(new String(Fetch.Details.Data,Charset.forName("tis-620")));
                        Fetch.Details.WebContent = wu.HTMLCompress(Fetch.Details.Doc);
                        waw.WriteRecord(Fetch.Details);
                        LinkFromRedir(Url, Fetch.Details.Doc);
                        AnalyseLink(Url, Fetch.Details.Doc);
                        URLLoaded.add(URLQueue.remove(0));
                        writeUpdateDB(Url, LanguageDetector.Detect(Fetch.Details.Doc.text().trim()));
                        try {
                            //Success & delay
                            Thread.sleep(this.CrawlDelay);
                        } catch (InterruptedException ex) {
                            System.err.println("Interupt while delay!...");
                            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        URLCrash.add(URLQueue.remove(0));
                    }
                } else {
                    if (!CodeHandler()) {
                        break;
                    }
                }
            } else {
                // Unknown and add to crash
                URLCrash.add(URLQueue.remove(0));
            }
        }
    }

    public void writeUpdateDB(String Url, String lang) {
        //"url","language",file_size,comment_size,js_size,style_size,content_size
        try {
            rafWebDB.write(("\"" + Url.replaceAll("\"", "\"\"") + "\"," + (lang == null ? "null" : "\"" + lang + "\"") + "," + wu.FileSize + "," + wu.CommentSize + "," + wu.ScriptSize + "," + wu.StyleSize + "," + wu.ContentSize + "\n").getBytes());
            //System.out.println("\"" + Url.replaceAll("\"", "\"\"") + "\"," + (lang == null ? "null" : "\"" + lang + "\"") + "," + wu.FileSize + "," + wu.CommentSize + "," + wu.ScriptSize + "," + wu.StyleSize + "," + wu.ContentSize);
        } catch (IOException ex) {
            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeUpdateDB() {
        try {
            rafWebDB.close();
        } catch (IOException ex) {
            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        WebDBFile.delete();

    }

    public boolean CodeHandler() {
        if (Fetch.ResponseCode == 0) {
            // 0 No Internet Connection -- Stop
            ProjectTester.SIGTERM = true;
            return false;
        } else if (Fetch.ResponseCode >= 300) {
            if (Fetch.ResponseCode < 400) {
                // 3xx Redirect or No Login -- Stop
                ProjectTester.SIGTERM = true;
                return false;
            } else if (Fetch.ResponseCode < 500) {
                // 4xx Client error -- Skip & add Crash URL
                URLCrash.add(URLQueue.remove(0));
                return true;
            } else {
                // 5xx Server error -- Skip & add Crash URL
                URLCrash.add(URLQueue.remove(0));
                return true;
            }
        } else {
            // Unknown and add to crash
            URLCrash.add(URLQueue.remove(0));
            return true;
        }
    }

    public boolean isAllowedResponseCode(int ResponseCode) {
        return ResponseCode >= 200 && ResponseCode < 300;
    }

    public boolean isAllowedHeader() {
        return "text/html".equals(Fetch.Details.ArchiveContentType);
    }

    public boolean isAllowedPreFixPath(String Path) {
        return Path.startsWith(AcceptOnlyPrefixPath);
    }

    public long ReadFile() {
        long LastPos = -1;
        long dbPos;
        String Line;
        try (WebArcReader war = new WebArcReader(TmpFile, "utf-8")) {
            // Read Update db
            dbPos = rafWebDB.getFilePointer();
            Line = rafWebDB.readLine();
            if (Line == null || Line.split(",").length != WebDBColumnWidth) {
                rafWebDB.seek(dbPos);
            } else {
                while (war.Next()) {
                    try {
                        LinkFromRedir(war.Record.URL, war.Record.Doc);
                        AnalyseLink(war.Record.URL, war.Record.Doc);
                        URLQueue.remove(war.Record.URL);
                        URLLoaded.add(war.Record.URL);
                        // Read Update db
                        dbPos = rafWebDB.getFilePointer();
                        Line = rafWebDB.readLine();
                        if (Line == null || Line.split(",").length != WebDBColumnWidth) {
                            rafWebDB.seek(dbPos);
                            break;
                        }
                    } catch (Exception e) {
                    }
                }
                LastPos = war.LastPos;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return LastPos;
    }

    /*
     * Must check url.size() before call
     */
    public void AddURL(MyURL url) {
        if (url.getProtocol().startsWith("http")
                && HostName.equals(url.getHost())
                && isAllowedPreFixPath(url.getPath())
                && robots.isAllowPath(url.getPath())) {
            if (!URLQueue.contains(url.UniqURL)
                    && !URLCrash.contains(url.UniqURL)
                    && !URLLoaded.contains(url.UniqURL)) {
                URLQueue.add(url.UniqURL);
                if (PrintEnqueue) {
                    System.out.println("Enqueue: " + url.UniqURL);
                }
            }
        }
    }

    public void LinkFromRedir(String url, Document doc) {
        try {
            MyURL src = new MyURL(url);
            Elements links = doc.select("meta");
            for (Element e : links) {
                try {
                    if (URLQueue.size() < MarginPage) {
                        if ("refresh".equalsIgnoreCase(e.attr("http-equiv"))) {
                            String[] tmp = e.attr("content").split(";");
                            tmp = tmp[tmp.length - 1].split("=");
                            AddURL(src.resolve(tmp[tmp.length - 1].trim()));
                        }
                        //System.out.println(src.resolve(e.attr("href")).UniqURL);
                    } else {
                        break;
                    }
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
        }
    }

    public void AnalyseLink(String url, Document doc) {
        if (URLQueue.size() < MarginPage) {
            try {
                MyURL src = new MyURL(url);
                Elements links = doc.select("a");
                for (Element e : links) {
                    try {
                        if (URLQueue.size() < MarginPage) {
                            AddURL(src.resolve(e.attr("href")));
                            //System.out.println(src.resolve(e.attr("href")).UniqURL);
                        } else {
                            break;
                        }
                    } catch (Exception ex) {
                    }
                }

                /* Additional link in Frame */
                links = doc.select("frame,iframe");
                for (Element e : links) {
                    try {
                        if (URLQueue.size() < MarginPage) {
                            AddURL(src.resolve(e.attr("src")));
                            //System.out.println(src.resolve(e.attr("href")).UniqURL);
                        } else {
                            break;
                        }
                    } catch (Exception ex) {
                    }
                }

            } catch (Exception ex) {
            }
        }

    }

    public static void main(String[] args) {
        /*
         try {
         MyURL u = new MyURL("http://www.sat.or.th/th/main/Default.aspx");
         MyURL x = u.resolve("../main/sitemap.html");
         System.out.println(x.UniqURL);
         } catch (Exception ex) {
         Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        //Crawler c = new Crawler("www.sat.or.th", ".", 200, true);
        String HostName = args.length > 0 ? args[0] : "chaosuan.me.engr.tu.ac.th";
        String StorePath = args.length > 1 ? args[1] : "data/crawldata";
        int limit = args.length > 2 ? Integer.parseInt(args[2]) : 1000;
        String SubHostPath = args.length > 3 ? args[3] : "/";
        CrawlerConfig cfg = new CrawlerConfig(3);
        SiteCrawler c = new SiteCrawler(HostName, StorePath, limit, SubHostPath, false, cfg);
        c.run();
        /*
         ArcUtils au = new ArcUtils("crawl-127.0.0.1.arc");
         while(au.Next()){
         System.out.println(au.GetHeader());
         System.out.println();
         System.out.println(au.GetContent());
         System.out.println();
         System.out.println();
         }*/
    }

    public void Compress(File Src, File Dst) {
        try (DataInputStream br = new DataInputStream(new FileInputStream(Src))) {
            try (BlockCompressedOutputStream BCOS = new BlockCompressedOutputStream(Dst)) {
                byte[] bytes = new byte[4096];
                int len;
                while ((len = br.read(bytes)) >= 0) {
                    BCOS.write(bytes, 0, len);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

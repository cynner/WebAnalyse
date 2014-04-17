/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import ArcFileUtils.MyRandomAccessFile;
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
import com.almworks.sqlite4java.SQLiteQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author malang
 */
public class SiteCrawlerChecker implements Runnable {

    public static final File hostinfo = new File("data/hostinfo.result.txt");
    public SQLiteQueue dbq;

    public boolean isSetTimeZone = false;
    public static String MyIP = GetMyIP();
    public static String UserAgent = "princeofvamp@gmail.com";
    public int CrawlDelay = 100;
    public String HostName;
    public ArrayList<String> URLQueue = new ArrayList<>();
    public ArrayList<String> URLCrash = new ArrayList<>();
    public ArrayList<String> URLLoaded = new ArrayList<>();
    public Fetcher Fetch;
    //private Robotstxt robots;
    //private BufferedWriter bw;
    //private boolean isOpenFile = false;
    public String HostIP;

    public boolean PrintEnqueue = false;

    //public CrawlerConfig crawlConf = null;
    public String curPageLanguage;
    public String SiteLang;
    public String SiteLocale;
    public String SiteInscope;
    public int MaxPreCrawl = 3;
    public int MarginPage = 20;
    public static int ThreadNo=20;

    public static String GetMyIP() {
        String s = null;
        try {
            s = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            System.err.println("Unknown Local IP");
        }
        return s;
    }

    public SiteCrawlerChecker(String HostName) {
        this.HostName = HostName;
        //this.crawlConf = crawlConf;
        if (!isSetTimeZone) {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            isSetTimeZone = true;
        }

        this.SiteInscope = "n";
        this.SiteLang = "--";
            try {
                InetAddress address = InetAddress.getByName(HostName);
                this.HostIP = address.getHostAddress();
                this.SiteLocale = GeoIP.IP2ISOCountry(this.HostIP);
                if("TH".equals(this.SiteLocale))
                    this.SiteInscope = "y";
            } catch (UnknownHostException ex) {
                this.HostIP = null;
            }
    }

    /*
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
     */
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " " + HostName + " Start.");
        if (this.HostIP != null) {
            processCommand();
        }
        UpdateHostInfo(HostName,HostIP,SiteLocale,SiteLang,SiteInscope);
        System.out.println(Thread.currentThread().getName() + " " + HostName + " End.");
    }

    private void processCommand() {
        this.Fetch = new Fetcher(UserAgent);

        if (URLQueue.isEmpty()) {
            URLQueue.add("http://" + HostName + "/");
        }

        preCrawl();
        
    }

    public boolean preCrawl() {
        String Url;
        while (!URLQueue.isEmpty() && URLLoaded.size() < MaxPreCrawl) {
            // 2.1 Fetch
            Url = URLQueue.get(0);
            System.out.println("Fetch: " + Url);
            // 2.2 Compress html Extractlink & write to file
            if (Fetch.getHeader(Url)) {
                if (isAllowedResponseCode(Fetch.ResponseCode) && isAllowedHeader()) {
                    if (Fetch.getDocument()) {
                        //Fetch.Details.WebContent = wu.HTMLCompress(Fetch.Details.Doc);
                        LinkFromRedir(Url, Fetch.Details.Doc);
                        AnalyseLink(Url, Fetch.Details.Doc);
                        URLLoaded.add(URLQueue.remove(0));
                        curPageLanguage = LanguageDetector.Detect(Fetch.Details.Doc.text());
                        try {
                            //Success & delay
                            Thread.sleep(this.CrawlDelay);
                        } catch (InterruptedException ex) {
                            System.err.println("Interupt while delay!...");
                            Logger.getLogger(SiteCrawlerChecker.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if ("th".equals(curPageLanguage)) {
                            SiteLang = "th";
                            SiteInscope = "y";
                            
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
    
    public boolean isAllowedPath(String path) {
        path = path.toLowerCase();
        return path.endsWith(".html") || path.endsWith(".php") || path.endsWith("/")  ;
    }

    /*
     * Must check url.size() before call
     */
    public void AddURL(MyURL url) {
        if (url.getProtocol().startsWith("http")
                && HostName.equals(url.getHost())
                && isAllowedPath(url.getPath())) {
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
                Elements links = doc.select("a, area");
                for (Element e : links) {
                    try {
                        if (URLQueue.size() < MarginPage) {
                            AddURL(src.resolve(e.attr("href")));
                            //System.out.println(src.resolve(e.attr("href")).UniqURL);
                        } else {
                            break;
                        }
                    } catch (Exception ex) {
                        //Not importance silence
                        //Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public static void UpdateHostInfo(String HostName, String IP, String Location, String Lang, String Inscope){
        //hostname, ip , location , page_count , status , log_id , lastupdate)
        Long pos;
        synchronized (hostinfo) {
            boolean fileExists = hostinfo.exists();
            try (MyRandomAccessFile raf = new MyRandomAccessFile(hostinfo, "rw")) {
                if (fileExists) {
                    pos = raf.readLong();
                    raf.seek(pos);
                } else {
                    pos = (long) (Long.SIZE / 8);
                    raf.writeLong(pos);
                }
                
                raf.write((Inscope + "," + Lang + "," + Location + "," + IP + "," +  HostName + "\n").getBytes("utf-8"));
                pos = raf.getFilePointer();
                raf.seek(0);
                raf.writeLong(pos);
            } catch (IOException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        /*
         try {
         MyURL u = new MyURL("http://www.sat.or.th/th/main/Default.aspx");
         MyURL x = u.resolve("../main/sitemap.html");
         System.out.println(x.UniqURL);
         } catch (Exception ex) {
         Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        //Crawler c = new Crawler("www.sat.or.th", ".", 200, true);
        String FileHostName = args.length > 0 ? args[0] : "checklist.txt";
        MainRun(new File(FileHostName));
    }
    
    public static void MainRun(File FileSeed) throws IOException{
        ExecutorService executor;
        String HostName;
        try(BufferedReader br = new BufferedReader (new FileReader(FileSeed))){
            System.out.println("====== Starting ======");
            
            executor = Executors.newFixedThreadPool(ThreadNo);
            while((HostName = br.readLine()) != null){
                Runnable worker = new SiteCrawlerChecker(HostName);
                executor.execute(worker);
            }
            
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            
            System.out.println("====== Success ======");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SiteCrawlerChecker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SiteCrawlerChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

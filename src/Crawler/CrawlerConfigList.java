/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.BGZFCompress;
import ArcFileUtils.MyRandomAccessFile;
import static Crawler.SiteCrawler.strWebDBColumnHeader;
import DBDriver.QjobInsertOrUpdate;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Included crawler structure
 *
 * @author wiwat
 *
 */
public class CrawlerConfigList extends CrawlerConfig implements AutoCloseable{

    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static final String subDirArcGZ = "arc";
    public static final String SuffixGZ = ".gz";
    public static final String strMergeWebInfo = "webpageinfo.txt";
    public static final String strHostInfo = "websiteinfo.txt";
    public static final String strHostCrawled = "websitecrawled.txt";
    public static final String strSiteDBColumnHeader = "hostname,status,page_count,lastupdate";
    public static final String[] SiteDBColumnHeader = new String[]{"hostname","ip","location","language","status"};
    public final String TaskName;
    public final File fileMergeWebInfo;
    public final File fileHostInfo;
    public final File fileHostCrawled;
    public final String strWorkingDirectory;
    public final String strDirArcGZ;
    public final SQLiteQueue sqlQSite;

    public CrawlerConfigList(String TaskName, String strWorkingDirectory) throws IOException {
        this.TaskName = TaskName;
        this.strWorkingDirectory = strWorkingDirectory;
        this.strDirArcGZ = strWorkingDirectory + "/" + TaskName + "/" + subDirArcGZ ;
        this.fileMergeWebInfo = new File(strWorkingDirectory + "/" + TaskName + "/" + strMergeWebInfo);
        this.fileHostInfo = new File(strWorkingDirectory + "/" + TaskName + "/" + strHostInfo);
        this.fileHostCrawled = new File(strWorkingDirectory + "/" + TaskName + "/" + strHostCrawled);
        this.sqlQSite = null;
        
        File[] D = new File[3];
        D[0] = new File(this.strWorkingDirectory);
        D[1] = new File(this.strDirArcGZ);
        D[2] = new File(strWorkingDirectory + "/" + TaskName);
        for (File d : D) {
            if (!d.isDirectory()) {
                if (!d.exists()) {
                    d.mkdirs();
                } else {
                    throw new IOException(this.strWorkingDirectory + " is not directory.");
                }
            }
        }
    }
    
    public CrawlerConfigList(File fileDBSite){
        if(!fileDBSite.exists()){
            SQLiteConnection conn = new SQLiteConnection(fileDBSite);
            try {
                conn.open(true);
                conn.exec(DBDriver.TableConfig.CreateTableWebSiteDB);
            } catch (SQLiteException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                conn.dispose();
            }
        }
        this.sqlQSite = new SQLiteQueue(fileDBSite);
        this.sqlQSite.start();
        this.TaskName = null;
        this.fileMergeWebInfo = null;
        this.fileHostInfo = null;
        this.fileHostCrawled = null;
        this.strWorkingDirectory = null;
        this.strDirArcGZ = null;
    }
    
    public File getArcGZFile(String ArcFileName){
        return new File(this.strDirArcGZ + "/" + ArcFileName + SuffixGZ);
    }

    public void addWebInfo(File InfoFile) {
        Long pos;
        String Line;
        synchronized (fileMergeWebInfo) {
            boolean fileExists = fileMergeWebInfo.exists();
            try (MyRandomAccessFile br = new MyRandomAccessFile(InfoFile,"r");
                    MyRandomAccessFile raf = new MyRandomAccessFile(fileMergeWebInfo, "rw")) {

                if (fileExists) {
                    raf.seek(0);
                    pos = raf.readLong();
                    raf.seek(pos);
                } else {
                    raf.seek(0);
                    byte[] b = (strWebDBColumnHeader + "\n").getBytes();
                    raf.writeLong(Long.SIZE + b.length);
                    raf.write(b);
                }
                long filepos = br.readLong();

                while (br.getFilePointer() < filepos) {
                    Line = br.readLine();
                    raf.write((Line + "\n").getBytes());
                }
                pos = raf.getFilePointer();
                raf.seek(0);
                raf.writeLong(pos);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void addCrawledList(String HostName) {
        Long pos;
        synchronized (fileHostCrawled) {
            boolean fileExists = fileHostCrawled.exists();
            try (MyRandomAccessFile raf = new MyRandomAccessFile(fileHostCrawled, "rw")) {

                if (fileExists) {
                    pos = raf.readLong();
                    raf.seek(pos);
                } else {
                    pos = (long) (Long.SIZE);
                    raf.writeLong(pos);
                }
                raf.write((HostName + "\n").getBytes("utf-8"));
                pos = raf.getFilePointer();
                raf.seek(0);
                raf.writeLong(pos);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void CrawlerFinishing(SiteCrawler s){
        if(s.URLLoaded.size() > 0 && s.status == Status.Finished){
            addWebInfo(s.WebDBFile);
            BGZFCompress.Compress(s.ArcFile, getArcGZFile(s.ArcFile.getName()));
        }
        UpdateHostInfo(s.HostName, s.status, s.URLLoaded.size());
        addCrawledList(s.HostName);
        if(s.WebDBFile.exists())
            s.WebDBFile.delete();
        if(s.ArcFile.exists())
            s.ArcFile.delete();
    }
    
    @Override
    public void CheckerFinishing(SiteCrawler s){
        if("TH".equals(s.SiteLocale) || "th".equals(s.SiteLang) || s.HostName.endsWith("th"))
            s.status = Status.NotBegin;
        else
            s.status = Status.NotInScope;
        String[] Vals = new String[]{"'" + s.HostName.replace("'", "''") + "'",
            "'" + s.HostIP + "'","'" + s.SiteLocale + "'",
            "'" + s.SiteLang + "'", "" + s.status.value };
        sqlQSite.execute(new QjobInsertOrUpdate("website",SiteDBColumnHeader,Vals));
    }

    @Override
    public boolean isAccept(SiteCrawler s) {
        return "th".equals(s.curPageLanguage);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean isCrawled(File ArcFile){
        File chk = new File(this.strDirArcGZ + "/" + ArcFile.getName() + SuffixGZ);
        return chk.exists();
    }
    
    public void UpdateHostInfo(String HostName, Status stat, int page_count){
        //hostname,status,page_count,lastupdate
        Long pos;
        synchronized (fileHostInfo) {
            boolean fileExists = fileHostInfo.exists();
            try (MyRandomAccessFile raf = new MyRandomAccessFile(fileHostInfo, "rw")) {
                if (fileExists) {
                    raf.seek(0);
                    pos = raf.readLong();
                    raf.seek(pos);
                } else {
                    raf.seek(0);
                    byte[] b = (strSiteDBColumnHeader + "\n").getBytes();
                    raf.writeLong((long) (Long.SIZE + b.length));
                    raf.write(b);
                }
                
                raf.write(("\"" + HostName.replace("\"", "\"\"") + "\"," + stat.value + "," + page_count + ",\"" + dateFormat.format(new Date()) + "\"\n").getBytes("utf-8"));
                pos = raf.getFilePointer();
                raf.seek(0);
                raf.writeLong(pos);
            } catch (IOException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void close(){
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        if (sqlQSite != null) {
            try {
                sqlQSite.stop(true).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}

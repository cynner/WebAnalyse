/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.BGZFCompress;
import ArcFileUtils.MyRandomAccessFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Included crawler structure
 *
 * @author wiwat
 *
 */
public class CrawlerConfigList extends CrawlerConfig {

    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static final String subDirArcGZ = "arc";
    public static final String SuffixGZ = ".gz";
    public static final String strMergeWebInfo = "webpageinfo.txt";
    public static final String strHostInfo = "websiteinfo.txt";
    public static final String strHostCrawled = "websitecrawled.txt";
    public final String TaskName;
    public final File fileMergeWebInfo;
    public final File fileHostInfo;
    public final File fileHostCrawled;
    public final String strWorkingDirectory;
    public final String strDirArcGZ;

    public CrawlerConfigList(String TaskName, String strWorkingDirectory) throws IOException {
        this.TaskName = TaskName;
        this.strWorkingDirectory = strWorkingDirectory;
        this.strDirArcGZ = strWorkingDirectory + "/" + subDirArcGZ + "/" + TaskName;
        this.fileMergeWebInfo = new File(strWorkingDirectory + "/" + TaskName + "/" + strMergeWebInfo);
        this.fileHostInfo = new File(strWorkingDirectory + "/" + TaskName + "/" + strHostInfo);
        this.fileHostCrawled = new File(strWorkingDirectory + "/" + TaskName + "/" + strHostCrawled);
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
                    pos = raf.readLong();
                    raf.seek(pos);
                } else {
                    pos = (long) (Long.SIZE / 8);
                    raf.writeLong(pos);
                }
                long filepos = br.readLong();

                while ((Line = br.readLine()) != null && br.getFilePointer() <= filepos) {
                    raf.write((Line + "\n").getBytes("utf-8"));
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
                    pos = (long) (Long.SIZE / 8);
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
    public void Finishing(SiteCrawler s){
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
    public boolean isAccept(SiteCrawler s) {
        return "th".equals(s.curPageLanguage);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean isCrawled(File ArcFile){
        File chk = new File(this.strDirArcGZ + "/" + ArcFile.getName() + SuffixGZ);
        return chk.exists();
    }
    
    public void UpdateHostInfo(String HostName, Status stat, int page_count){
        //hostname, ip , location , page_count , status , log_id , lastupdate)
        Long pos;
        synchronized (fileHostInfo) {
            boolean fileExists = fileHostInfo.exists();
            try (MyRandomAccessFile raf = new MyRandomAccessFile(fileHostInfo, "rw")) {
                if (fileExists) {
                    pos = raf.readLong();
                    raf.seek(pos);
                } else {
                    pos = (long) (Long.SIZE / 8);
                    raf.writeLong(pos);
                }
                
                raf.write((HostName + "," + stat.value + "," + page_count + "," + dateFormat.format(new Date()) + "\n").getBytes("utf-8"));
                pos = raf.getFilePointer();
                raf.seek(0);
                raf.writeLong(pos);
            } catch (IOException ex) {
                Logger.getLogger(CrawlerConfigList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}

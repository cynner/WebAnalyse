/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import Crawler.SiteCrawler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.filefilter.WildcardFileFilter;
    
/**
 *
 * @author malang
 */
public class ThreadPool{
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static ArrayList<String> KEYs = new ArrayList<>();
    
    public static void RunHostExtract(){
        File dir = new File(ProjectTester.inputDir);
        File[] lst = dir.listFiles((FileFilter)(new WildcardFileFilter("*.arc")));
        HostExtractor.initial(ProjectTester.outputFile);
        ExecutorService executor = Executors.newFixedThreadPool(ProjectTester.ThreadNo);
        //int i=0;
        System.out.println(dateFormat.format(new Date()) + " Started...");
        for (File f : lst) {
            Runnable worker = new HostExtractor(f);
            executor.execute(worker);
            /*
            if(i>100){
                HostExtractor.HostLock = true;
                try {
                    HostExtractor.bw.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    HostExtractor.HostLock = false;
                }
                i=0;
            }
            i++;
            */
        }
        
        
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println(dateFormat.format(new Date()) + " Finishing...");
        HostExtractor.finishing();
        System.out.println(dateFormat.format(new Date()) + "Finished all threads");
    }
    
    public static void RunReduceGraph(String InputDir, String OutputDir) {
        File dir = new File(InputDir);
        File[] lst = dir.listFiles((FileFilter)(new WildcardFileFilter("*.mlink")));
                
        ExecutorService executor = Executors.newFixedThreadPool(ProjectTester.ThreadNo);
        int Work = lst.length;
        for (int i=0;i<Work;i++) {
            Runnable worker = new ReduceGraph(lst[i], OutputDir + HostGraph.HostFromFile(lst[i]) + ".mlink2");
            executor.execute(worker);
        }
        
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
    
    public static void RunHostGraph(String InDir, String OutDir){
        File dir = new File(InDir);
        File key = new File(ProjectTester.keyPath);
        //System.out.println(InDir);
        String[] works;
        String Host;
        if (!key.exists()) {

            File[] files = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".arc");
                }
            });


            if (OutDir.charAt(OutDir.length() - 1) != '/') {
                OutDir += "/";
            }

            File odir = new File(OutDir);
            if (!odir.exists()) {
                odir.mkdir();
            }
            HostGraph.CreateHostMap(files);
            System.out.println("Create map finished.");
            HostGraph.SaveHostMap(ProjectTester.keyPath);
            System.out.println("Save map finished.");
        }else{
            HostGraph.LoadHostMap(ProjectTester.keyPath);
        }
        ProjectTester.WorkBufferSize = ProjectTester.ThreadNo * 10;
        ExecutorService executor = Executors.newFixedThreadPool(ProjectTester.ThreadNo);
        while (!ProjectTester.SIGTERM && !ProjectTester.SIGINT && getNextKey()) {
            while(!KEYs.isEmpty()){
                //System.out.println(KEYs.get(0));
                Host = KEYs.remove(0).split(" ")[0];
                //System.out.println(Host);
                File f = dir.listFiles((FileFilter)(new WildcardFileFilter( "*" + Host + "*.arc")))[0];
                //System.out.println(OutDir);
                Runnable worker = new HostGraph(Host, f, new File(OutDir + Host + ".mlink2"));
                executor.execute(worker);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
    
    public static void RunLinkExtractor(String InDir, String OutDir, int CntThread) {
        File dir = new File(InDir);
        File[] files = dir.listFiles();
        
        if(OutDir.charAt(OutDir.length()-1) != '/')
            OutDir += "/";
        File odir = new File(OutDir);
        if(!odir.exists())
            odir.mkdir();
        
        ExecutorService executor = Executors.newFixedThreadPool(CntThread);
        for (File file : files) {
            Runnable worker = new LinkExtractor(file, new File(OutDir + file.getName() + ".mlink"));
            executor.execute(worker);
            
          }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
    
    public static void RunCrawler(int MaxPage){
        String Host;
        ProjectTester.WorkBufferSize = ProjectTester.ThreadNo * 10;
        ExecutorService executor = Executors.newFixedThreadPool(ProjectTester.ThreadNo);
        ProjectTester.WorkBufferSize = ProjectTester.ThreadNo * 10;
        while (!ProjectTester.SIGTERM && !ProjectTester.SIGINT && getNextKey()) {
            while(!KEYs.isEmpty()){
                //System.out.println(KEYs.get(0));
                Host = KEYs.remove(0);
                System.out.println(Host);
                //System.out.println(Host);
                //File f = dir.listFiles((FileFilter)(new WildcardFileFilter( "*" + Host + "*.arc")))[0];
                //System.out.println(OutDir);
                Runnable worker = new SiteCrawler(Host, ProjectTester.outputDir, MaxPage, "/", true);
                executor.execute(worker);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        
        if(ProjectTester.SIGTERM )
            System.out.println("End by sigterm..");
        System.out.println("Finished all threads");
    }
    
    public static void RunExampleThread(int CntThread, int Work)  {
        
        ExecutorService executor = Executors.newFixedThreadPool(CntThread);
        
        for (int i=0;i<Work;i++) {
            Runnable worker = new ExampleThread();
            executor.execute(worker);
        }
        
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
    
    @SuppressWarnings("empty-statement")
    public static boolean getNextKey() {
        File l = new File(ProjectTester.assignedPath + ".lock");
        File f = new File(ProjectTester.assignedPath);
        String[] conf;
        long pos = 0;
        //System.out.println(l.getAbsolutePath());
        try {
            System.out.println(l.getAbsolutePath());
            while (true) {
                while (l.exists());
                if (l.createNewFile()) {
                    break;
                }
            }
            
            if(f.exists()){
                conf = lastline(f).split("\t");
                pos = Long.parseLong(conf[4]);
            }
            getNLine(new File(ProjectTester.keyPath), ProjectTester.WorkBufferSize, pos);
            
            l.delete();
        } catch (IOException ex) {
            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
        }
        return !KEYs.isEmpty();
    }
    
    public static void getNLine(File file,int NoLine, long startfrom){
        String Line;
        int i=0;
        String key;
        //int idx;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")){
            raf.seek(startfrom);
            if((Line = raf.readLine()) != null){
                key = Line.split("\t")[0];
                i++;
                KEYs.add(key);
                while((Line = raf.readLine()) != null && i < NoLine){
                    i++;
                    KEYs.add(Line.split("\t")[0]);
                }
                SaveAssign(key, i, raf.getFilePointer());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
        }
        //return result;
    }
    
    public static void SaveAssign(String key,int no,long nextoffset){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ProjectTester.assignedPath, true))){
            bw.newLine();
            bw.write(dateFormat.format(new Date()) + "\t" + ProjectTester.hostName + "\t" + key + "\t" + no + "\t" + nextoffset);
        } catch (IOException ex) {
            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String lastline(File file) {
        try (RandomAccessFile fileHandler = new RandomAccessFile(file, "r")){
            long fileLength = file.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    } else {
                        break;
                    }
                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    } else {
                        break;
                    }
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException ex) {
            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (java.io.IOException ex) {
            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } 
    }
    
    public static void InputDirToKey(String InputDir, String Format){
        File dir = new File(InputDir);
        File[] lst = dir.listFiles((FileFilter)(new WildcardFileFilter(Format)));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ProjectTester.keyPath))){
            for(File f : lst){
                bw.write(f.getName() + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    


}

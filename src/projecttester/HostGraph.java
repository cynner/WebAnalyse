/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.io.*;
import java.net.IDN;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author malang
 */
public class HostGraph implements Runnable {
    
    public static HashMap<String,Integer> Mapper;
    
    public String HostName;
    public int HostNum;
    
    public UniqueListInt ULI;
    
    private File InputFile;
    private File OutputFile;
    private BufferedWriter bw;
    
    //public boolean running = true;
    
    
    
    
    public HostGraph(String HostName, File InputFile, File OutputFile){
        this.HostName = HostName;
        this.InputFile = InputFile;
        this.OutputFile = OutputFile;
        this.HostNum = Mapper.get(HostName);
        this.ULI = new UniqueListInt();
    }
    
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" Start-Anlysefile " + InputFile);
        processCommand();
        System.out.println(Thread.currentThread().getName()+" End-Anlysefile " + InputFile);
    }
    
    private void processCommand() {
        ArcUtils au = new ArcUtils(InputFile);
        while(au.Next()){ // SIGTERM handle !SIGTERM &&
            Construct(au.GetContent());
            //System.out.println(au.GetHeader());
        }
        try {
            bw = new BufferedWriter(new FileWriter(OutputFile, false));
            for(UniqueListInt.UElementInt e : ULI.UList)
                bw.write(HostNum+";"+e.ID+";"+e.count+"\n");
            bw.close();
            ULI.Clear();
            ULI = null;
        } catch (SecurityException | IOException ex) {
            Logger.getLogger(HostGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void Construct(String Content){
        Document doc = Jsoup.parse(Content);
        Elements es = doc.select("a");
        int idx;
        for(Element e : es){
            idx = getIntHostFromLink(e.attr("href"));
            if(idx >= 0)
                ULI.Add(idx);
        }
    }
    
    public int getIntHostFromLink(String Link){
        int lidx,idx = Link.indexOf(":"),v;
        if(idx < 0){
            return HostNum;
        }else{
            idx += 3;
            if(Link.length() > idx){
                lidx = Link.indexOf('/',idx);
                try{
                    if(lidx < 0){
                        v = Mapper.get(IDN.toASCII(Link.substring(idx)).toLowerCase());
                    }else{
                        v = Mapper.get(IDN.toASCII(Link.substring(idx,lidx)).toLowerCase());
                    }
                    return v;
                }catch (NullPointerException ex){
                    return -1;
                }
            }else{
                return -1;
            }
        }
    }
    
    public static void LoadHostMap(String FileName){
        try {
            String Line;
            String[] KV;
            BufferedReader br = new BufferedReader(new FileReader(FileName));
            Mapper = new HashMap<>();
            
            while((Line = br.readLine())!=null){
                KV = Line.split(" ");
                if(KV.length > 1){
                    Mapper.put(KV[0], Integer.parseInt(KV[1]));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HostGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex){
            Logger.getLogger(HostGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void CreateHostMap(String InputDirectory){
        File[] dir = (new File(InputDirectory)).listFiles();
        int i=1;
        Mapper = new HashMap<>();
        for(File f : dir){
            Mapper.put(HostFromFile(f), i++);
        }
    }
    
    public static void CreateHostMap(File[] dir){
        int i=1;
        Mapper = new HashMap<String, Integer>();
        for(File f : dir){
            Mapper.put(HostFromFile(f), i++);
        }
    }
    
    public static void SaveHostMap(String OutputFileName){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OutputFileName, false))){
            for(Map.Entry<String, Integer> e : Mapper.entrySet()){
                bw.write(e.getKey() + " " + e.getValue() + "\n");
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(HostGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String HostFromFile(File f){
        //System.out.println(f.getName());
        //System.out.println(f.getName().replaceFirst("^crawl\\-", "").replaceAll("\\-2013.*", ""));
        return f.getName().replaceFirst("^crawl\\-", "").replaceAll("\\-2013.*", "");
    }
}

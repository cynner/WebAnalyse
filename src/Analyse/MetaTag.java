/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Analyse;

import ArcFileUtils.WebArcReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author malang
 */
public class MetaTag {
    
    //public static String ResultDir="data/result";
    public static String StrOutputFile="data/result/MetaSumary.txt";
    public static String StrOutputDB="data/result/MetaSumary.sqlite3";
    public static String StrInput="data/arc";
    
    
    public static class myInt{
        public int val;
        public myInt(){
            val = 1;
        }
    }
    
    public static void main(String[] args){
        File OutputFile = new File(StrOutputFile);
        File Dir = new File(OutputFile.getParent());
        if(!Dir.exists()){
            Dir.mkdir();
        }else if(!Dir.isDirectory()){
            System.err.println(Dir.getName() + " is not directory!");
            System.exit(1);
        }
        HashMap<String,myInt> hm = new HashMap<String,myInt>();
        int cntdoc=0;
        int cntmeta=0;
        String MetName;
        myInt Mi;
        File InputDir = new File(StrInput);
        for(File f : InputDir.listFiles()){
            
            System.out.println(f.getName());
            WebArcReader war = new WebArcReader(f,"utf-8");
            while(war.Next()){
                Elements es = war.Record.Doc.getElementsByTag("meta");
                if(es != null){
                    cntdoc++;
                    for(Element e : es){
                        cntmeta++;
                        MetName = e.attr("name").toLowerCase();
                        Mi = hm.get(MetName);
                        if(Mi != null){
                            Mi.val++;
                        }else{
                            hm.put(MetName, new myInt());
                        }
                            
                    }
                }
            }
            war.close();
        }
        System.out.println("Success");
        FileWriter fw;
        try {
            fw = new FileWriter(OutputFile);

            fw.write("Page Contains meta : " + cntdoc + "\n");
            fw.write("Meta Count : " + cntmeta + "\n");

            for (Map.Entry<String,myInt> e : hm.entrySet()) {
                fw.write(e.getKey() + " : " + e.getValue().val + "\n");
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(MetaTag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}

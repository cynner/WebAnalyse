/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LanguageUtils;

import ArcFileUtils.ArcReader;
import ArcFileUtils.WebArcReader;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import projecttester.ArgUtils;

/**
 *
 * @author malang
 */
public class FileLanguageDetector {
    public Detector detector;
    
    public BufferedWriter bw = null;
    public static String InputFile = null;
    public static String InputDir = "data/TxtThai/ภูมิภาค";
    public static String OutputFile = null;
        
    
    public static void main(String[] args){
        
        try {
            DetectorFactory.loadProfile("resource/profiles");
        } catch (LangDetectException ex) {
            Logger.getLogger(FileLanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        FileLanguageDetector DL = new FileLanguageDetector();
        
        HashMap<String,String> Args = ArgUtils.Parse(args);
        
        
        if(Args.containsKey("o"))
            OutputFile = Args.get("o");
        if(Args.containsKey("i"))
            InputFile = Args.get("i");
        if(Args.containsKey("id"))
            InputDir = Args.get("id");
        
        
        
        if(OutputFile != null){
            try {
                DL.bw = new BufferedWriter(new FileWriter(OutputFile));
            } catch (IOException ex) {
                Logger.getLogger(FileLanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        
        if(InputFile != null ){
            File f = new File(InputFile);
            DL.DetectFile(f);
        }
        
        if(InputDir != null){
            //System.out.print(InputDir);
            File f = new File(InputDir);
            DL.DetectDir(f);
        }
        
        if(DL.bw != null){
            try {
                DL.bw.close();
            } catch (IOException ex) {
                Logger.getLogger(FileLanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
       
    }
    
    public void DetectDir(File Dir){
        for(File f : Dir.listFiles()){
            if(f.isDirectory())
                DetectDir(f);
            else
                DetectFile(f);
        }
    }
    
    public void DetectFile(File f){
        try (ArcReader ar = new ArcReader(f)) {
            String lang;
            while(ar.Next()){
                if (ar.Record.ArchiveLength > 0) {
                    try {
                        detector = null;
                        detector = DetectorFactory.create();
                        
                        detector.append(ar.Record.ArchiveContent);
                        lang = detector.detect();
                        if (bw != null) {
                            bw.write(lang + ";" + ar.Record.URL + "\n");
                        } else {
                            System.out.println(lang + ";" + ar.Record.URL);
                        }
                    } catch (LangDetectException ex) {
                        System.err.println(ar.Record.URL);
                        Logger.getLogger(FileLanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(FileLanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileLanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

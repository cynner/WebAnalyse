/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package LanguageUtils;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class LanguageDetector {
    
    public static boolean isLoadedProfile = false;
    
    public static void init(){
        if(!isLoadedProfile){
            
            isLoadedProfile = true;
            try {
                DetectorFactory.loadProfile("resource/profiles");
            } catch (LangDetectException ex) {
                Logger.getLogger(FileLanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static String Detect(String Content){
        init();
        String lang = null;
        try {
            if(Content != null && !Content.equals("")){
                Detector detector = DetectorFactory.create();

                detector.append(Content);
                
                lang = detector.detect();
            }else{
                return null;
            }
        } catch (LangDetectException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lang;
    }
    
    
    public static void main(String[] args){
        String Content = "กักกกกๆๆๆ 日本人 Gug a wish 日本語 日本語 日本語 日本人";
        String lang;
        Detect(Content);
        Detector detector;
        try {
            detector = DetectorFactory.create();
            
            detector.append(Content);
            lang = detector.detect();
            System.out.println("Lang " +lang);
            for( Language l : detector.getProbabilities()){
                System.out.println(l.lang+" " + l.prob);
            }
        } catch (LangDetectException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}

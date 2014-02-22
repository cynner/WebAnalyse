/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.io.*;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.archive.io.*;
import org.archive.io.arc.*;

/**
 *
 * @author malang
 */
public class ArcUtils {
    
    private Iterator<ArchiveRecord> iAR;
    private ArchiveRecord curAR;
    
    private ARCReader arcr;
    
    public String GetContent(){
        ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
        try {
            curAR.dump(BAOS);
            BAOS.close();
            return BAOS.toString();
        } catch (IOException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public String GetHeader(){
        return curAR.getHeader().toString();
    }
    
    public boolean hasNext(){
        return iAR.hasNext();
    }
    
    public boolean Next(){
        if(iAR.hasNext()){
            curAR = iAR.next();
            return true;
        }else{
            try {
                arcr.close();
            } catch (IOException ex) {
                Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
    }
    
    public ArcUtils(String FileName){
        try {
            arcr = ARCReaderFactory.get(FileName);
            arcr.setParseHttpHeaders(false);
            iAR = arcr.iterator();
            if(iAR.hasNext())
                curAR = iAR.next();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArcUtils(File file){
        
        try {
            arcr = ARCReaderFactory.get(file);
            arcr.setParseHttpHeaders(false);
            iAR = arcr.iterator();
            if(iAR.hasNext())
                curAR = iAR.next();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArcUtils(){
    }
    
    public static void TestRead(File FileName){
        ArcUtils a = new ArcUtils();
        try {
            ArcUtils.AnalyseAll(FileName,a, ArcUtils.class.getMethod("CatContent", String.class, String.class)) ;
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void CatContent(String Header, String Content){
        System.out.println(ArcUtils.URLFromHeader(Header));
        System.out.println(Content);
        System.out.println("----------------------------------------------------");
    }
    
    public static void AnalyseAll (File ArcFileName, Object Obj, Method Met){
        try {
            ARCReader a = ARCReaderFactory.get(ArcFileName);
            a.setParseHttpHeaders(false);
            ArchiveRecord AR;
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            Iterator<ArchiveRecord> x = a.iterator();
            
            // Skip 0 header
            if(x.hasNext())
                AR = x.next();
            
            while (x.hasNext()){
                AR = x.next();
                
                AR.dump(BAOS);
                BAOS.close();
                // Medthod here 
                //Met.invoke(Obj, AR.getHeader().toString(), BAOS.toString().replaceFirst("^.*([^\n]*\n){6}", "")); 
                Met.invoke(Obj, AR.getHeader().toString(), BAOS.toString()); 
                // System.out.println(BAOS.toString());
            }
            a.close();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        //return Pages;
    }
    
    public static void AnalyseContent (File ArcFileName, Object Obj, Method Met){
        try {
            ARCReader a = ARCReaderFactory.get(ArcFileName);
            a.setParseHttpHeaders(false);
            ArchiveRecord AR;
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            Iterator<ArchiveRecord> x = a.iterator();
            
            // Skip 0 header
            if(x.hasNext())
                AR = x.next();
            
            while (x.hasNext()){
                AR = x.next();
                
                AR.dump(BAOS);
                BAOS.close();
                // Medthod here 
                //Met.invoke(Obj, BAOS.toString().replaceFirst("^.*([^\n]*\n){6}", "")); 
                Met.invoke(Obj, BAOS.toString());
                // System.out.println(BAOS.toString());
            }
            a.close();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        //return Pages;
    }
    
    public static void AnalyseHeader (File ArcFileName, Object Obj, Method Met){
        try {
            ARCReader a = ARCReaderFactory.get(ArcFileName);
            a.setParseHttpHeaders(false);
            ArchiveRecord AR;
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            Iterator<ArchiveRecord> x = a.iterator();
            // Skip 0 header
            if(x.hasNext())
                AR = x.next();
            while (x.hasNext()){
                AR = x.next();
                Met.invoke(Obj, AR.getHeader().toString()); 
            }
            a.close();
            
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArcUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        //return Pages;
    }
    
    
    public static String URLFromHeader(String Header){
        return Header.replaceFirst("^.*subject-uri=", "").replaceFirst(",.*", "");
    }
}

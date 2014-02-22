/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;

/**
 *
 * @author malang
 */
public class ApacheArchReader {
        
    private Iterator<ArchiveRecord> iAR;
    private ArchiveRecord curAR;
    
    private ARCReader arcr;
    
    public String GetContent(){
        String res;
        ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
        try {
            curAR.dump(BAOS);
            res = BAOS.toString();
            BAOS.close();
            return res;
        } catch (IOException ex) {
            Logger.getLogger(ApacheArchReader.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(ApacheArchReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
    }
    
    public ApacheArchReader(String FileName){
        try {
            arcr = ARCReaderFactory.get(FileName);
            arcr.setParseHttpHeaders(false);
            iAR = arcr.iterator();
            if(iAR.hasNext())
                curAR = iAR.next();
        } catch (IOException ex) {
            Logger.getLogger(ApacheArchReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ApacheArchReader(File file){
        
        try {
            arcr = ARCReaderFactory.get(file);
            arcr.setParseHttpHeaders(false);
            iAR = arcr.iterator();
            if(iAR.hasNext())
                curAR = iAR.next();
        } catch (IOException ex) {
            Logger.getLogger(ApacheArchReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ApacheArchReader(){
    }
    
    public static void TestRead(File FileName){

    }
    
    public void CatContent(String Header, String Content){
        System.out.println(ApacheArchReader.URLFromHeader(Header));
        System.out.println(Content);
        System.out.println("----------------------------------------------------");
    }
    
    
    public static String URLFromHeader(String Header){
        return Header.replaceFirst("^.*subject-uri=", "").replaceFirst(",.*", "");
    }
}

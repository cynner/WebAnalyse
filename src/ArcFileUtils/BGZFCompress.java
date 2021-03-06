/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ArcFileUtils;

import Crawler.SiteCrawler;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 *
 * @author malang
 */
public class BGZFCompress {
    public static void main(String[] args){
        String FileIn, FileOut;
        FileIn = args.length > 0 ? args[0] : "crawl-100kg.diaryclub.com.arc";
        FileOut = args.length >=2 ? args[1] : FileIn + ".gz";
        Compress(new File(FileIn), new File(FileOut));
    }
    
    public static void Compress(File Src, File Dst){
        try (BlockCompressedOutputStream BCOS = new BlockCompressedOutputStream(Dst);
                DataInputStream br =  new DataInputStream(new FileInputStream(Src))){
            byte[] bytes = new byte[4096];
            int len;
            while( (len = br.read(bytes)) >= 0){
                BCOS.write(bytes,0,len);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import ArcFileUtils.ArcFilenameFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 *
 * @author malang
 */
public class GZ2BGZF {
    public static String dirinname = "data/arc-org/";
    public static String diroutname = "data/arc-org2/";
    public static void main(String[] args){
        File dirin = new File(dirinname);
        File dirout = new File(diroutname);
        if(!dirout.isDirectory()){
            if(!dirout.exists()){
                dirout.mkdir();
            }else{
                System.err.println("Dir out is not Dicrtory");
                System.exit(1);
            }
        }
        for(File f : dirin.listFiles(new ArcFilenameFilter())){
            File o = new File(diroutname + "/" + f.getName());
            ConvertGZ2BGZ(f, o);
        }
    }
    
    public static void ConvertGZ2BGZ(File GZFile, File BGZFile){byte[] 
            b = new byte[2048];
            int len;
            try(GZIPInputStream gin = new GZIPInputStream(new FileInputStream(GZFile)); 
                    BlockCompressedOutputStream BCOS = new BlockCompressedOutputStream(BGZFile)){
                while((len = gin.read(b))>=0){
                    BCOS.write(b,0,len);
                }
            } catch (IOException ex) {
                Logger.getLogger(GZ2BGZF.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}

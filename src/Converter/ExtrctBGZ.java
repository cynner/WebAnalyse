/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import ArcFileUtils.ArcReader;
import ArcFileUtils.BGZFReader;
import ArcFileUtils.BGZFWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author malang
 */
public class ExtrctBGZ {
    public static void main(String[] args) throws IOException{
        String FileIn = "data/arc/crawl-026916789.nalueng.com.arc.bgz";
        String FileOut = "data/gg.txt";
        
        ArcReader ar = new ArcReader(new File(FileIn));
        
        FileWriter fw = new FileWriter(FileOut);
        
        while(ar.Next()){
            fw.write("\n" + ar.Record.URL + "\n");
            fw.write(ar.Record.ArchiveContent);
            fw.flush();
        }
        
        fw.close();
        ar.close();
        
        /*
        BGZFReader br = new BGZFReader(new File(FileIn));
        FileWriter fw;// = new FileWriter(FileOut);
        
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(FileOut));
        byte[]b = new byte[4096];
        int v;
        while((v = br.read(b)) > 0)
            dos.write(b);
        //v = br.read(b);
        //dos.write(b, 0, v);
        System.err.println(v);
                
        br.close();
        dos.close();
        /*
        FileIn = "data/gg";
        FileOut = "data/gg.1.gz";
        BGZFWriter.Compress(new File(FileIn), new File(FileOut));
        
        FileIn = "data/gg.1.gz";
        FileOut = "data/gg1";
        br = new BGZFReader(new File(FileIn));
        fw = new FileWriter(FileOut,false);
        String line;
        while((line=br.readLine()) != null){
            fw.write(line + "\n");
            //System.err.println(br.getRealOffset());
        }
        fw.close();
        br.close();
                */
                
        
    }
}

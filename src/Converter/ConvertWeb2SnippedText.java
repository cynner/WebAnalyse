/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import ArcFileUtils.CompressedArcWriter;
import ArcFileUtils.WebArcReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;

/**
 *
 * @author malang
 */
public class ConvertWeb2SnippedText {
    /**
     * 
     * @param args args[0] = InputDir , args[1] = OutputDir 
     * 
     */
    public static void main(String args[]){
        String InDir=null,OutDir=null;
        args = new String[]{"data/arcgz","data/snipped"};
        if(args.length < 2){
            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.print("Indir: ");
                InDir = br.readLine();
                System.out.print("Outdir: ");
                OutDir = br.readLine();
            } catch (IOException ex) {
                Logger.getLogger(ConvertWeb2SnippedText.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            InDir = args[0];
            OutDir = args[1];
        }
        File outd = new File(OutDir);
        if(!outd.exists()){
            outd.mkdir();
        }else if(!outd.isDirectory()){
            System.exit(1);
        }
        File lstin = new File(InDir);
        //File OutTMP;
        File OutGZ;
        Element e;
        for(File f : lstin.listFiles()){
            
            OutGZ = new File(OutDir + "/" + f.getName());
            if (!OutGZ.exists()) {
                System.out.println(OutGZ.getName());
                try (WebArcReader war = new WebArcReader(f, "utf-8"); 
                        CompressedArcWriter aw = new CompressedArcWriter(OutGZ)) {
                    System.out.println(OutGZ.getName());
                    while (war.Next()) {
                        war.Record.ArchiveContent = war.Record.Doc.title() + "\n";
                        if ((e = war.Record.Doc.body()) != null) {
                            war.Record.ArchiveContent += e.text();
                        }
                        aw.WriteRecordFromContent(war.Record);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ConvertWeb2SnippedText.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}

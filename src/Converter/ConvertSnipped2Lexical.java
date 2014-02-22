/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Converter;

import ArcFileUtils.ArcReader;
import ArcFileUtils.CompressedArcWriter;
import Lexto.LuceneLexicalTH;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class ConvertSnipped2Lexical {

    public static void main(String[] args) {
        args = new String[]{"data/snipped", "data/lexical"};

        if (args.length < 2) {
            System.err.println("ERR: Please Enter 2 Argument [inputDir outputDir]");
            System.exit(1);
        }

        String InDirName = args[0];
        String OutDirName = args[1];

        File InDir = new File(InDirName);
        File OutDir = new File(OutDirName);

        if (!InDir.isDirectory()) {
            System.err.println("ERR: InputDir '" + InDirName + "' does not exists.");
            System.exit(2);
        }

        if (!OutDir.isDirectory()) {
            if (!OutDir.exists()) {
                OutDir.mkdir();
            } else {
                System.err.println("ERR: OutDir '" + InDirName + "' does not Directory.");
                System.exit(3);
            }

        }

        File OutFile;
        LuceneLexicalTH lex = new LuceneLexicalTH();
        String[] content;
        for (File f : InDir.listFiles()) {
            System.out.println(f.getName());
            OutFile = new File(OutDirName + "/" + f.getName());
            if (!OutFile.exists()) {
                try (ArcReader ar = new ArcReader(f); CompressedArcWriter caw = new CompressedArcWriter(OutFile)) {
                    while (ar.Next()) {

                        //System.out.println("_"+ar.Record.ArchiveContent+"_");
                        content = ar.Record.ArchiveContent.split("\n");
                        if (content.length > 0) {
                            ar.Record.ArchiveContent = lex.strSplitContent(content[0]) + "\n";
                            if (content.length > 1) {
                                ar.Record.ArchiveContent += lex.strSplitContent(content[1]);
                            }
                        } else {
                            ar.Record.ArchiveContent = "\n";
                        }

                        //ar.Record.ArchiveContent = lex.strSplitContent(ar.Record.ArchiveContent);
                        caw.WriteRecordFromContent(ar.Record);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(ConvertSnipped2Lexical.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}

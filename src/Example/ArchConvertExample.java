/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Example;

import ArcFileUtils.ArcFileConverter;
import java.io.File;

/**
 *
 * @author malang
 */
public class ArchConvertExample {
    
    public static ArcFileConverter ac = new ArcFileConverter();
    public static String StrIn = "Thai/การพนัน";
    public static String StrOut = "TxtThai/การพนัน";
    public static String SkipTo = "Thai/สังคม/ศาสนา/การทำนายดวงชะตา/crawl-www.payakorn.com.arc ";
    public static boolean skip = true;
    
    public static void main(String[] args){
        /*
        WebArcReader war = new WebArcReader(new File("Thai/สุขภาพ/สุขภาพหญิง/crawl-www.beautyfullallday.com.arc"), true);
        while(war.Next()){
            System.out.println(war.Record.URL);
        }*/
        
        //args = new String[]{"webtotxt","file"};
        //projecttester.ProjectTester.main(args);
        //String PostPath = "";
        File InDir = new File(StrIn);
        //File OutDir = new File(StrOut);
        
        ConvDir(InDir, StrOut + "/");
        //ArcDirConverter.WebToTextDir(new File(InputDir), new File(OutputDir));
    }
    
    public static void ConvDir(File Dir, String PostPath){
        File OD = new File(PostPath);
        if(!OD.isDirectory())
            OD.mkdirs();
        for(File f : Dir.listFiles()){
            System.out.println(f.getPath());
            if(f.isDirectory()){
                ConvDir(f, PostPath + f.getName() + "/");
            }else{
                if(skip){
                    if(SkipTo.equals(f.getPath())){
                        skip = false;
                        ac.WebToTextFile(f, new File(PostPath + f.getName()));
                    }
                }else{
                    //if(!ExtractLinkFromDMOZ.isUTF8(f))
                        ac.WebToTextFile(f, new File(PostPath + f.getName()));
                }
            }
        }
    }
}

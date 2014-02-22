/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Example;

import Lexto.MyLexto;
import java.io.*;
import projecttester.MyFileUtils;
import ArcFileUtils.WebUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class WebContentFilter {
    public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static String DirectoryName = "WebContentFilter/";
    public static void main(String[] args) throws IOException{
        System.out.println("Enter web html here:");
        String line;
        String s = "";
        while (!(line = reader.readLine()).equals("$END$"))
            s += line;
        WebUtils wu = new WebUtils(s);
        File d = new File(DirectoryName);
        if(!d.isDirectory())
            d.mkdir();
        int i=0;
        while((d = new File(DirectoryName + i + ".txt")).exists())
            i++;
        try{
            MyLexto ml = new MyLexto();
            MyFileUtils.WriteFile(d, ml.Split2Str(wu.doc.text()));
        }catch (IOException ex){
            Logger.getLogger(WebContentFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.io.*;

/**
 *
 * @author malang
 */
public class MyFileUtils {
    public static void WriteFile(String FileName, String Content) throws IOException{
        WriteFile(new File(FileName),Content);
    }
    
    public static void WriteFile(File f, String Content) throws IOException{
        FileWriter fw = new FileWriter(f);
        fw.write(Content);
        fw.close();
    }
    
}

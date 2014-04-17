/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package webanalyse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class Main {
    public static final String strFileLastProperties = "lastproperty.txt"; 

    /**
     * @param args the command line arguments
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.IllegalAccessException
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException{
        // TODO code application logic here
        if(args.length > 0) {
            Class<?> cls = Class.forName(args[0]);
            Method m = cls.getMethod("main", String[].class);
            String[] params = Arrays.copyOfRange(args, 1, args.length);
            WriteLastProperties(new File(strFileLastProperties));
            try {
                m.invoke(null, (Object) params);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            MainForm.main(args);
        }
    }
    
    public static void WriteLastProperties(File f){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
            for(Map.Entry<Object,Object> e : System.getProperties().entrySet()){
                bw.write(e.getKey() + " : " + e.getValue() + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

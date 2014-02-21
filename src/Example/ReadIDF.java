/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Example;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class ReadIDF {
    public static void main(String[] args) throws FileNotFoundException{
        String IDFFilename = "data/IDF.bin";
        int n1,n2;
        int[] tarr,carr;
        DataInputStream dir = new DataInputStream(new FileInputStream(IDFFilename));
        try {
            dir.skip(96);
            n1 = dir.readInt();
            System.out.println(n1);
            tarr = new int[n1];
            for(int i=0;i<n1;i++){
                tarr[i] = dir.readInt();
                System.out.println("DCNo " + tarr[i]);
                System.out.println("Freq " + dir.readInt());
            }
            n2 = dir.readInt();
            System.out.println(n2);
            carr= new int[n2];
            for(int i=0;i<n2;i++){
                carr[i] = dir.readInt();
                System.out.println("DCNo " + carr[i]);
                System.out.println("Freq " + dir.readInt());
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ReadIDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

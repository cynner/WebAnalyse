/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ContentClassifier;

import cc.mallet.classify.Classifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author wiwat
 */
public class MalletUtils {
    public static void saveClassifier(Classifier cls, File serializedFile)
        throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream (serializedFile))) {
            oos.writeObject (cls);
        }
    }
    
    public static Classifier loadClassifier(File serializedFile)
        throws FileNotFoundException, IOException, ClassNotFoundException {
        Classifier result;
        try (ObjectInputStream ois = new ObjectInputStream (new FileInputStream (serializedFile))) {
            result = (Classifier) ois.readObject();
        }
        return result;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.NaiveBayes;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.types.InstanceList;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class MalletTrainer {

    public static void main(String[] args) {
        String MalletFile = args.length > 0 ? args[0] : "resource/THContent.mallet";
        String ClassifierFile = args.length > 1 ? args[1] : "resource/THContent.class";
        InstanceList instances = InstanceList.load(new File(MalletFile));
        try {
            NaiveBayesTrainer cls = new NaiveBayesTrainer(instances.getPipe());
            
            //cls.setDocLengthNormalization(16384000.0);
            System.out.println(cls.getDocLengthNormalization());
            NaiveBayes nb = cls.train(instances);
            System.out.println(nb.getAccuracy(instances));
            
            MalletUtils.saveClassifier(nb, new File(ClassifierFile));
        } catch (IOException ex) {
                Logger.getLogger(MalletTrainer.class.getName()).log(Level.SEVERE, null, ex);

        }
    }
}

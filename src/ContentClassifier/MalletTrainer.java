/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import cc.mallet.classify.NaiveBayes;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.types.InstanceList;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class MalletTrainer {

    public static void main(String[] args) {
        //args = new String[]{"001System.arc", "txtaomy.arc", "txtamnat.arc"};
        //MalletArcImport importer = new MalletArcImport();
        //importer.readDirectories(new File("TxtThai/การพนัน"), true);
        InstanceList instances = InstanceList.load(new File("THnew.mallet"));
        try {
            NaiveBayesTrainer cls = new NaiveBayesTrainer(instances.getPipe());
            
            //cls.setDocLengthNormalization(16384000.0);
            System.out.println(cls.getDocLengthNormalization());
            NaiveBayes nb = cls.train(instances);
            System.out.println(nb.getAccuracy(instances));
            
            Trial trial = new Trial(nb, instances);

        // The Trial class implements many standard evaluation                                             
        //  metrics. See the JavaDoc API for more details.                                                 

        System.out.println("Accuracy: " + trial.getAccuracy() );
        
        for(int i=0;i< nb.getLabelAlphabet().toArray().length;i++){
            System.out.println("Precision for class '" +
                 nb.getLabelAlphabet().lookupLabel(i) + "': " +
                 trial.getPrecision(i) + " Rec: " + trial.getRecall(i) + " F-Mea: " + trial.getF1(i));
        }
            
/*
            ArrayList<Classification> clsns = nb.classify(instances);
            for (Classification clsn : clsns) {
                Labeling labeling = clsn.getLabeling();

                System.out.println("-------");
                for (int rank = 0; rank < labeling.numLocations(); rank++) {
                    System.out.print(labeling.getLabelAtRank(rank) + ":"
                            + labeling.getValueAtRank(rank) + " ");
                }
            }
        */
        } catch (Exception ex) {
                Logger.getLogger(MalletTrainer.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

}

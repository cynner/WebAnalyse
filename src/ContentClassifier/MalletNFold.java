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

/**
 *
 * @author malang
 */
public class MalletNFold {
    
    public static void main(String[] args) {
        //args = new String[]{"001System.arc", "txtaomy.arc", "txtamnat.arc"};
        //MalletArcImport importer = new MalletArcImport();
        //importer.readDirectories(new File("TxtThai/การพนัน"), true);
        InstanceList instances1 = InstanceList.load(new File("THnew.mallet"));
        InstanceList[] tt;
        InstanceList.CrossValidationIterator Crs = instances1.crossValidationIterator(10);
        while(Crs.hasNext()){
            tt = Crs.nextSplit();
            System.out.println(" : w" + tt[1].getInstanceWeight(0));
            System.out.println(" : w" + tt[1].getInstanceWeight(4000));

        try {
            NaiveBayesTrainer cls = new NaiveBayesTrainer(tt[0].getPipe());
            
            cls.setDocLengthNormalization(16384000.0);
            System.out.println(cls.getDocLengthNormalization());
            NaiveBayes nb = cls.train(tt[0]);
            //System.out.println(nb.getAccuracy(instances));
            
            Trial trial = new Trial(nb, tt[1]);

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
        } catch (Exception e) {

        }
        }
    }
}

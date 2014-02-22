/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import ArcFileUtils.MalletArcIterator;
import cc.mallet.classify.*;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import projecttester.ArgUtils;

/**
 *
 * @author malang
 */
public final class MalletClassifier {
    
    public static String StrFileIn = "ThaiTxt";
    public static String StrFileOut = "class.out";
    public static String StrFileSummary = "class.sum";
    public static String StrFileClassifier = "TH.naive.class";
    
    public BufferedWriter bwOut;
    public BufferedWriter bwSummary;
    public Classifier classifier;
    
    
    public MalletClassifier(File Output, File Summary, File Classifier) throws IOException, FileNotFoundException, ClassNotFoundException{
        this.bwOut = new BufferedWriter(new FileWriter(Output));
        this.bwSummary = new BufferedWriter(new FileWriter(Summary));
        this.loadClassifier(Classifier);
    }
    
    public MalletClassifier(File Output, File Summary, Classifier classifier) throws IOException{
        this.bwOut = new BufferedWriter(new FileWriter(Output));
        this.bwSummary = new BufferedWriter(new FileWriter(Summary));
        this.classifier = classifier;
    }
    
    public MalletClassifier(File Output, File Summary, InstanceList trainingInstances) throws IOException{
        this.bwOut = new BufferedWriter(new FileWriter(Output));
        this.bwSummary = new BufferedWriter(new FileWriter(Summary));
        this.trainClassifier(trainingInstances);
    }
    
    public void trainClassifier(InstanceList trainingInstances) {

        ClassifierTrainer trainer = new NaiveBayesTrainer();
        classifier = trainer.train(trainingInstances);
    }
    
    public void saveClassifier( File serializedFile)
        throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream (serializedFile))) {
            oos.writeObject (classifier);
        }
    }
    
    public void loadClassifier(File serializedFile)
        throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream (new FileInputStream (serializedFile))) {
            classifier = (Classifier) ois.readObject();
        }

    }
    
    public void printLabelings(File file) throws IOException {

        // Create a new iterator that will read raw instance data from                                     
        //  the lines of a file.                                                                           
        // Lines should be formatted as:                                                                   
        //                                                                                                 
        //   [name] [label] [data ... ]                                                                    
        //                                                                                                 
        //  in this case, "label" is ignored.                                                              

        MalletArcIterator reader = new MalletArcIterator(file);     

        // Create an iterator that will pass each instance through                                         
        //  the same pipe that was used to create the training data                                        
        //  for the classifier.                                                                            
        Iterator instances = classifier.getInstancePipe().newIteratorFrom(reader);

        // Classifier.classify() returns a Classification object                                           
        //  that includes the instance, the classifier, and the                                            
        //  classification results (the labeling). Here we only                                            
        //  care about the Labeling.                                                                       
        while (instances.hasNext()) {
            Labeling labeling = classifier.classify(instances.next()).getLabeling();

            // print the labels with their weights in descending order (ie best first)                     

            for (int rank = 0; rank < labeling.numLocations(); rank++){
                System.out.print(labeling.getLabelAtRank(rank) + ":" +
                                 labeling.getValueAtRank(rank) + " ");
            }
            System.out.println();
        }
    }
    
    public void printBestFile(File file) throws IOException {
        int [] sum = new int[classifier.getLabelAlphabet().toArray().length];
                                                   
        MalletArcIterator reader = new MalletArcIterator(file);
                                                     
        Iterator<Instance> instances = classifier.getInstancePipe().newIteratorFrom(reader);
        Instance ins;                                                 
        while (instances.hasNext()) {
            ins = instances.next();
            Labeling labeling = classifier.classify(ins).getLabeling();

            // print the labels with their weights in descending order (ie best first)
            sum[labeling.getBestIndex()]++;
            //System.out.println(labeling.getBestLabel()+" "+ins.getName());
            bwOut.write( labeling.getLabelAtRank(0)+" "+ins.getName()+"\n");

        }
        reader.remove();
        int max = 0;
        for(int i=0;i<sum.length;i++){
            if(sum[i] > sum[max])
                max = i;
        }
        bwSummary.write(classifier.getLabelAlphabet().lookupLabel(max) + " " + file.getName() + "\n");
    }
    
    public void printBestDir(File dir) throws IOException{
        for(File f : dir.listFiles()){
            if(f.isDirectory())
                printBestDir(f);
            else
                printBestFile(f);
        }
    }
    
    public void printBest(File file) throws IOException {
        if(file.isDirectory()){
            printBestDir(file);
        }else{
            printBestFile(file);
        }
    }
    
    public void evaluate( File file) throws IOException {

        // Create an InstanceList that will contain the test data.                                         
        // In order to ensure compatibility, process instances                                             
        //  with the pipe used to process the original training                                            
        //  instances.                                                                                     

        InstanceList testInstances = new InstanceList(classifier.getInstancePipe());
                                        
        //   [name] [label] [data ... ]                                          


        Trial trial = new Trial(classifier, testInstances);

        // The Trial class implements many standard evaluation                                             
        //  metrics. See the JavaDoc API for more details.                                                 

        System.out.println("Accuracy: " + trial.getAccuracy());

        for(int i=0;i< classifier.getLabelAlphabet().toArray().length;i++){
            System.out.println("Precision for class '" +
                 classifier.getLabelAlphabet().lookupLabel(i) + "': " +
                 trial.getPrecision(i));
        }
    }
    
    public void close(){
        try {
            bwOut.close();
            bwSummary.close();
        } catch (IOException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void main(String[] args){
        MalletClassifier MC = null;
        HashMap<String,String> Args = ArgUtils.Parse(args);
        /*
        StrFileIn = Args.get("i");
        StrFileOut = Args.get("o");
        StrFileSummary = Args.get("s");
        StrFileClassifier = Args.get("c");
        */
        try {        
            MC = new MalletClassifier(new File(StrFileOut), new File(StrFileSummary), new File(StrFileClassifier));
            //MC.printBest(new File(StrFileIn));
            
            for(int i=0;i<15;i++)
                System.out.println(MC.classifier.getLabelAlphabet().lookupLabel(i) + "");
            
            /*
            args = new String[]{"001System.arc", "txtaomy.arc", "txtamnat.arc"};
            MalletArcImport importer = new MalletArcImport();
            importer.readDirectories(new File("TxtThai/การพนัน"), true);
            
            try{
            Classifier cls = loadClassifier(new File("TH.naive.class"));
            ArrayList<Classification> clsns = cls.classify(importer.instances);
            for(Classification clsn : clsns){
            Labeling labeling = clsn.getLabeling();
            
            System.out.println("-------");
            for (int rank = 0; rank < labeling.numLocations(); rank++){
            System.out.print(labeling.getLabelAtRank(rank) + ":" +
            labeling.getValueAtRank(rank) + " ");
            }
            }
            }catch(Exception e){
            
            }
            */
        } catch ( IOException | ClassNotFoundException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex){
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(MC != null)
                MC.close();
        }
        
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import ArcFileUtils.MalletArcIterator;
import ArcFileUtils.MalletArcWebIterator;
import cc.mallet.classify.*;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import java.io.*;
import java.util.ArrayList;
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
    
    public static String StrFileIn = "crawl-abc.trf.or.th.arc.gz";
    public static String StrFileOut = "class.out";
    public static String StrFileSummary = "class.sum";
    public static String StrFileClassifier = "resource/TH.naive.class";
    
    public BufferedWriter bwOut;
    public BufferedWriter bwSummary;
    public Classifier classifier;
    
    public SQLiteConnection db = new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
    
    
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

        MalletArcWebIterator reader = new MalletArcWebIterator(file);     

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
            System.out.println(labeling.getLabelAtRank(0)+" "+ins.getName());

        }
        reader.remove();
        int max = 0;
        for(int i=0;i<sum.length;i++){
            if(sum[i] > sum[max])
                max = i;
        }
        System.out.println(classifier.getLabelAlphabet().lookupLabel(max) + " " + file.getName() + "\n");
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
    
    public void saveSQL(File file){
        try {
            db.open();
            if(file.isDirectory()){
                saveSQLDir(file);
            }else{
                saveSQLFile(file);
            }
        } catch (SQLiteException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            db.dispose();
        }
        
    }
    
    private void saveSQLFile(File file){
        try {
            System.out.println(file.getName());
            MalletArcWebIterator reader = new MalletArcWebIterator(file);
            
            Iterator<Instance> instances = classifier.getInstancePipe().newIteratorFrom(reader);
            Instance ins;
            
            db.exec("BEGIN;");
            while (instances.hasNext()) {
                ins = instances.next();
                Labeling labeling = classifier.classify(ins).getLabeling();
                
                db.exec("UPDATE webpage SET category=\"" + labeling.getLabelAtRank(0) + "\" WHERE url=\"" + ins.getName().toString().replaceAll("\"", "\"\"") + "\";");
                
                
            }
            db.exec("COMMIT;");
            reader.remove();
            
        } catch (IOException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLiteException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveSQLDir(File file){
        for(File f : file.listFiles()){
            if(f.isDirectory())
                saveSQLDir(f);
            else
                saveSQLFile(f);
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
        //HashMap<String,String> Args = ArgUtils.Parse(args);
        /*
        StrFileIn = Args.get("i");
        StrFileOut = Args.get("o");
        StrFileSummary = Args.get("s");
        StrFileClassifier = Args.get("c");
        */ 
        StrFileIn = args.length > 0 ? args[0] : "data/crawldata";
        try {        
            MC = new MalletClassifier(new File(StrFileOut), new File(StrFileSummary), new File(StrFileClassifier));
            
            
            MC.loadClassifier(new File(StrFileClassifier));
            
            //MC.printLabelings(new File(StrFileIn));
            MC.saveSQL(new File(StrFileIn));
            
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

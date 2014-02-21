/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import ArcFileUtils.MalletArcIterator;
import cc.mallet.classify.AdaBoostTrainer;
import cc.mallet.classify.BaggingTrainer;
import cc.mallet.classify.BalancedWinnowTrainer;
import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.ConfidencePredictingClassifierTrainer;
import cc.mallet.classify.DecisionTreeTrainer;
import cc.mallet.classify.MCMaxEntTrainer;
import cc.mallet.classify.MaxEntPRTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayes;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.classify.PRAuxClassifierOptimizable;
import cc.mallet.classify.Trial;
import cc.mallet.classify.WinnowTrainer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.PrintInputAndTarget;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import java.beans.DesignMode;
import java.io.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;

/**
 *
 * @author malang
 */
public class MalletArcImport {
    public Pipe pipe;
    public InstanceList instances;

    public MalletArcImport() {
        pipe = buildPipe();
        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        instances = new InstanceList(pipe);
    }

    public Pipe buildPipe() {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers, 
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern =
            Pattern.compile("\\S+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field: 
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
        pipeList.add(new Target2Label());

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        pipeList.add(new FeatureSequence2FeatureVector());

        // Print out the features and the label
        // pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }
/*
    public void readArchives(File Arc, String Category) {
        readArchives(new File[] {Arc}, Category, false);
    }
  */  
    public void readArchives(File[] Arcs, String Category, boolean recursive) {

        // Construct a file iterator, starting with the 
        //  specified directories, and recursing through subdirectories.
        // The second argument specifies a FileFilter to use to select
        //  files within a directory.
        // The third argument is a Pattern that is applied to the 
        //   filename to produce a class label. In this case, I've 
        //   asked it to use the last directory name in the path.
        for(File Arc : Arcs){
            // MalletArcIterator iterator = new MalletArcIterator(Arc, Category);
            // Now process each instance provided by the iterator.
            if(Arc.isDirectory() && recursive)
                readDirectory(Arc, Category, recursive);
            else{
                System.out.println(Arc.getPath());
                Iterator<Instance> L = new MalletArcIterator(Arc, Category);
                instances.addThruPipe(L);
                L.remove();
                
            }
        }
    }
    
    public void readDirectory(File Arc, String Category, boolean recursive) {
        readArchives(Arc.listFiles(), Category, recursive);
    }
    
    public void readDirectories(File Arc, boolean recursive){
        for(File dir : Arc.listFiles()){
            if(dir.isDirectory()){
                readDirectory(dir, dir.getName(), recursive);
            }   
        }
    }

    public static void main (String[] args) throws IOException {

        // = new String[]{"001System.arc", "txtaomy.arc", "txtamnat.arc"};
        MalletArcImport importer = new MalletArcImport();
        importer.readDirectories(new File("TxtThai"), true);
        
        importer.instances.save(new File("THnew.mallet"));
        
        

    }

    /** This class illustrates how to build a simple file filter */
    class TxtFilter implements FileFilter {

        /** Test whether the string representation of the file 
         *   ends with the correct extension. Note that {@ref FileIterator}
         *   will only call this filter if the file is not a directory,
         *   so we do not need to test that it is a file.
         */
        public boolean accept(File file) {
            return file.toString().endsWith(".arc");
        }
    }
}

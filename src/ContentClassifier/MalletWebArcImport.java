/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import ArcFileUtils.MalletArcWebIterator;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import java.io.*;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author malang
 */
public class MalletWebArcImport {
    public Pipe pipe;
    public InstanceList instances;

    public MalletWebArcImport() {
        pipe = buildPipe();
        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        instances = new InstanceList(pipe);
    }

    public final Pipe buildPipe() {
        ArrayList pipeList = new ArrayList();

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
                try {
                    System.out.println(Arc.getPath());
                    Iterator<Instance> L = new MalletArcWebIterator(Arc, Category);
                    instances.addThruPipe(L);
                    L.remove();
                } catch (IOException ex) {
                    Logger.getLogger(MalletWebArcImport.class.getName()).log(Level.SEVERE, null, ex);
                }
                
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

        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");

        ArgumentParser parser = ArgumentParsers.newArgumentParser("ContentClassifier.MalletWebArcImport").defaultHelp(true)
                .description("Import Site Arc to classified");
        parser.addArgument("-o", "--output")
                .dest("output")
                .metavar("OUTPUT")
                .type(String.class)
                .required(true)
                .help("Output file name");
        parser.addArgument("DIR_IN")
                .dest("dir_in")
                .type(String.class)
                .help("Web archives directory input");
        
        
        try {
            Namespace res = parser.parseArgs(args);

            System.out.println("Starting ...");
            
            String ResultFile = res.getString("output");
            MalletWebArcImport importer = new MalletWebArcImport();
            importer.readDirectories(new File(res.getString("dir_in")), true);
        
            importer.instances.save(new File(ResultFile));
            System.out.println("Success");
        
       
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        
        
        

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

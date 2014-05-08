/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import cc.mallet.classify.NaiveBayes;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.types.InstanceList;
import java.io.File;
import java.io.IOException;
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
public class MalletTrainer {

    public static void main(String[] args) {
        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");

        ArgumentParser parser = ArgumentParsers.newArgumentParser("ContentClassifier.MalletTrainer").defaultHelp(true)
                .description("Make classifier");
        parser.addArgument("-c", "--class")
                .dest("class")
                .metavar("FILE")
                .type(String.class)
                .help("Classifier file")
                .required(true);
        parser.addArgument("FILE_IN")
                .dest("file_in")
                .type(String.class)
                .help("Mallet input");

        try {
            Namespace res = parser.parseArgs(args);

            System.out.println("Starting ...");

            String MalletFile = res.getString("file_in");
            String ClassifierFile = res.getString("class");
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

        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }

    }
}

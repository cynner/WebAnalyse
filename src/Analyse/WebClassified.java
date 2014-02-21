/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Analyse;

import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.CommandOption.File;

/**
 *
 * @author malang
 */
public class WebClassified {
    
    public static void main(String[] args) {
    CharSequence obj = "hello";
    String str = "hello";
    System.out.println("Type of obj: " + obj.getClass().getSimpleName());
    System.out.println("Type of str: " + str.getClass().getSimpleName());
    System.out.println("Value of obj: " + obj);
    System.out.println("Value of str: " + str);
    System.out.println("Is obj a String? " + (obj instanceof String));
    System.out.println("Is obj a CharSequence? " + (obj instanceof CharSequence));
    System.out.println("Is str a String? " + (str instanceof String));
    System.out.println("Is str a CharSequence? " + (str instanceof CharSequence));
    System.out.println("Is \"hello\" a String? " + ("hello" instanceof String));
    System.out.println("Is \"hello\" a CharSequence? " + ("hello" instanceof CharSequence));
    System.out.println("str.equals(obj)? " + str.equals(obj));
    System.out.println("(str == obj)? " + (str == obj));
    }
    
    /*
    public static Pipe pipe = buildPipe;
    public static void main(String[] args){
        new Input2CharSequence("UTF-8");
        InstanceList instances = new InstanceList(pipe);
        Instance d = new Instance(args, args, pipe, args);
        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);
    }
    
    public Classifier trainClassifier(InstanceList trainingInstances) {

        // Here we use a maximum entropy (ie polytomous logistic regression)                               
        //  classifier. Mallet includes a wide variety of classification                                   
        //  algorithms, see the JavaDoc API for details.                                                   

        ClassifierTrainer trainer = new MaxEntTrainer();
        return trainer.train(trainingInstances);
    }
     * 
     */
}

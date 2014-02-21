/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ContentClassifier;

import ArcFileUtils.ArcReader;
import ArcFileUtils.MalletArcIterator;
import ArcFileUtils.MalletArcIteratorLimit;
import cc.mallet.classify.NaiveBayes;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 *
 * @author malang
 */
public class MalletEval {
    public int   CatNo = 0;
    public int   NFold = 10;
    public ArrayList<Integer> CatCount = new ArrayList<Integer>();
    public int tmpCnt;
    
    public void CountRec(File f){
        ArcReader ar = new ArcReader(f);
        while(ar.Skip()){
            tmpCnt++;
        }
        ar.close();
    }
    
    public void Count(File Dir){
        for(File f : Dir.listFiles()){
            if(f.isDirectory())
                Count(f);
            else
                CountRec(f);
        }
    }
    
    public void TenFold(File Dir){
        
        /*
        สุขภาพ 1753
        ข่าว 1899
        วิทยาศาสตร์ 1396
        กีฬา 2301
        การพนัน 1146
        สังคม 7473
        อ้างอิง 4852
        ภูมิภาค 5498
        ศิลปะ 4114
        ธุรกิจ 11366
        นันทนาการ 6529
        คอมพิวเตอร์ 8533
        บ้าน 1937
          ซื้อของ 11173
        เกม 985
        */
        // Count
        /*
        File[] Files = Dir.listFiles();
        for(int i=0; i<Files.length;i++ ){
            if(Files[i].isDirectory()){
                tmpCnt = 0;
                Count(Files[i]);
                CatCount.add(tmpCnt);
                CatNo++;
                System.out.println(Files[i].getName() + " " + tmpCnt);
            }
        }
        */
        /*
        Import(Files);
        for(int i=0;i<NFold;i++){
            instances[i].save(new File("fold" + i + ".mallet"));
        }*/
        instances = new InstanceList[NFold];
        for(int i=0; i<NFold; i++){
            instances[i] = InstanceList.load(new File("fold" + i + ".mallet"));
        }
        InstanceList TrainIns = new InstanceList(pipe);
        for(int i=0;i<NFold-1;i++){
            TrainIns.addThruPipe(instances[i].iterator());
        }
        
        NaiveBayesTrainer cls = new NaiveBayesTrainer(TrainIns.getPipe());
            
            //cls.setDocLengthNormalization(16384000.0);
            System.out.println(cls.getDocLengthNormalization());
            NaiveBayes nb = cls.train(TrainIns);
            
            
            Trial trial = new Trial(nb, instances[NFold -1 ]);

        // The Trial class implements many standard evaluation                                             
        //  metrics. See the JavaDoc API for more details.                                                 

        System.out.println("Accuracy: " + trial.getAccuracy() );
        
        for(int i=0;i< nb.getLabelAlphabet().toArray().length;i++){
            System.out.println("Precision for class '" +
                 nb.getLabelAlphabet().lookupLabel(i) + "': " +
                 trial.getPrecision(i) + " Rec: " + trial.getRecall(i) + " F-Mea: " + trial.getF1(i));
        }
    }
    
    public static void main(String[] args){
        MalletEval e = new MalletEval();
        e.TenFold(new File("TxtThai"));
    }
        
        
        
        
        
        
    public Pipe pipe;
    public InstanceList[] instances;
    public int CurCatNo;
    public int curFold;
        public int avgRec;
        public int maxRec;
        public int curRec;
    

    public MalletEval() {
    }
    
    public void Import(File[] Files){
       
        pipe = buildPipe();
        //  Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        instances = new InstanceList[CatNo];
        for(int i=0; i < NFold; i++)
            instances[i] = new InstanceList(pipe);
        
        CurCatNo = 0;
        for(int i=0; i < Files.length; i++){
            if(Files[i].isDirectory()){
                curFold = 0;
                curRec = 0;
                avgRec = CatCount.get(CurCatNo) / NFold;
                maxRec = avgRec;
                readSep(Files[i], Files[i].getName());
                CurCatNo++;
            }
        }
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
    
    public void readSep(File Dir, String Category){
        for(File f : Dir.listFiles()){
            if(f.isDirectory()){
                readSep(f, Category);
            }else{
                System.out.println(f.getPath() + " Lim: " +  (maxRec - curRec) + " Fold: " + curFold + " Prog: (" + curRec + "/" + maxRec +  ")");
                
                MalletArcIteratorLimit L = new MalletArcIteratorLimit(f, Category, maxRec - curRec);
                instances[curFold].addThruPipe(L);
                curRec += L.Current;
                if(curRec >= maxRec){
                    curFold++;
                    if(curFold < NFold - 1){
                        maxRec = (curFold + 1) * avgRec;
                    }else{
                        curFold = 9;
                        maxRec = CatCount.get(CurCatNo) + 1;
                    }
                    if(L.ARhasNext()){
                        int c = L.Current;
                        L.Limit = c + maxRec - curRec;
                        instances[curFold].addThruPipe(L);
                        curRec += L.Current - c;
                    }
                }
                L.remove();
            }
        }
    }
    
    
    
    
        
    
}

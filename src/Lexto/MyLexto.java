/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexto;

import java.io.*;
import java.net.URI;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class MyLexto {
    public LongLexTo tokenizer;
    
    public MyLexto(){
        //LongLexTo tokenizer = null;
        try {
            URI uri = Lexto.LongLexTo.class.getResource("lexitron.txt").toURI();
            tokenizer = new LongLexTo(new File(uri));

            //Add words file
            String[] FileNameList = new String[]{"country.txt", "english", "addition.txt"};
            for (String FileName : FileNameList) {
                uri = Lexto.LongLexTo.class.getResource(FileName).toURI();
                File unknownFile = new File(uri);

                if (unknownFile.exists()) {
                    tokenizer.addDict(unknownFile);
                }
            }
            
        } catch (Exception ex) {
        }
    }
    
    public String Split2Str(String Str){
        int begin,end;
        //int type;
        //int i=0;
        //Vector typeList;
        String Output="";
        tokenizer.wordInstance(Str);
        //typeList=tokenizer.getTypeList();
        begin = tokenizer.first();
        while (tokenizer.hasNext()) {
            end = tokenizer.next();
            
            //type=((Integer)typeList.elementAt(i++)).intValue();
            if(Str.charAt(begin) != ' '){
                Output += Str.substring(begin, end) + " ";
            }
            begin = end;
        }
        return Output;
    }
    
    public String Split2Str(File file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String Line,Str="";
            while((Line=br.readLine())!=null)
                Str += Line + "\n";
            return Split2Str(Str);
        } catch (IOException ex) {}
        return null;
    }
    

    
    public static void main(String[] args) throws IOException {
        MyLexto ml = new MyLexto();
        File TestFile = new File("test.txt");
        System.out.println(ml.Split2Str(TestFile));
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import Lexto.LuceneLexicalTH;
import cc.mallet.types.Instance;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */

public class MalletArcWebIterator implements Iterator<Instance> {

    public String Label;
    public WebArcReader AR;
    
    private LuceneLexicalTH lex = new LuceneLexicalTH(true);
    
    public MalletArcWebIterator(File ArcFile, String Label) throws IOException{
        AR = new WebArcReader(ArcFile, "utf-8");
        this.Label = Label;
    }
    
    public MalletArcWebIterator(File ArcFile) throws IOException{
        this(ArcFile, "unknown");
    }
    /*
    public MalletArcIterator(File ArcFile, String Label){
        this(ArcFile, Label);
    }
    */
    
    @Override
    public boolean hasNext() {
        return AR.hasNext();
    }

    @Override
    public Instance next() {
        //System.out.println(AR.Record.URL);
        AR.Next();
        String Title = lex.strSplitContent(AR.Record.Doc.title());
        return new Instance (Title + Title + lex.strSplitContent(AR.Record.Doc.body().text()), Label, AR.Record.URL, null);
    }

    @Override
    public void remove() {
        try {
            AR.close();
            //throw new UnsupportedOperationException("Not supported yet.");
        } catch (IOException ex) {
            Logger.getLogger(MalletArcWebIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

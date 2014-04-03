/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import Lexto.LuceneLexicalTH;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.jsoup.nodes.Element;

/**
 *
 * @author malang
 */

public class MalletArcWebIterator implements Iterator<Instance> {

    public String Label;
    public WebArcReader AR;
    
    private final LuceneLexicalTH lex = new LuceneLexicalTH(true);
    
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
        TokenSequence toks = new TokenSequence();
        
        try(TokenStream ts = lex.getTokenStream(AR.Record.Doc.title())){
            ts.reset();
            CharTermAttribute cta = ts.addAttribute(CharTermAttribute.class);
            while(ts.incrementToken()){
                // X2
                toks.add(new Token(cta.toString()));
                toks.add(new Token(cta.toString()));
            }
            ts.end();
        } catch (IOException ex) {
            Logger.getLogger(MalletArcWebIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
        Element body = AR.Record.Doc.body();
        if(body != null) {
            try (TokenStream ts = lex.getTokenStream(body.text())) {
                ts.reset();
                CharTermAttribute cta = ts.addAttribute(CharTermAttribute.class);
                while (ts.incrementToken()) {
                    toks.add(new Token(cta.toString()));
                }
                ts.end();
            } catch (IOException ex) {
                Logger.getLogger(MalletArcWebIterator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Instance (toks, Label, AR.Record.URL, null);
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

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
public class MalletArcIterator implements Iterator<Instance> {

    public String Label;
    ArcReader AR;
    
    public MalletArcIterator(File ArcFile, String Label) throws IOException{
        AR = new ArcReader(ArcFile);
        this.Label = Label;
    }
    
    public MalletArcIterator(File ArcFile) throws IOException{
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
        return new Instance (AR.Record.ArchiveContent, Label, AR.Record.URL, null);
    }

    @Override
    public void remove() {
        try {
            AR.close();
            //throw new UnsupportedOperationException("Not supported yet.");
        } catch (IOException ex) {
            Logger.getLogger(MalletArcIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

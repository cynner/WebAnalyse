/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import cc.mallet.types.Instance;

import java.io.File;
import java.util.Iterator;

/**
 *
 * @author malang
 */
public class MalletArcIterator implements Iterator<Instance> {

    public String Label;
    ArcReader AR;
    
    public MalletArcIterator(File ArcFile, String Label){
        AR = new ArcReader(ArcFile);
        this.Label = Label;
    }
    
    public MalletArcIterator(File ArcFile){
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
        AR.close();
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

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
public class MalletArcIteratorLimit implements Iterator<Instance> {

    public String Label;
    public int Limit;
    public int Current;
    ArcReader AR;
    
    public MalletArcIteratorLimit(File ArcFile, String Label, int Limit){
        AR = new ArcReader(ArcFile);
        this.Label = Label;
        this.Limit = Limit;
        this.Current = 0;
    }
    
    public MalletArcIteratorLimit(File ArcFile, int Limit){
        this(ArcFile, "unknown", Limit);
    }
    /*
    public MalletArcIterator(File ArcFile, String Label){
        this(ArcFile, Label);
    }
    */
    
    public boolean ARhasNext(){
        return AR.hasNext();
    }
    
    @Override
    public boolean hasNext() {
        return (Current < Limit) && AR.hasNext();
    }

    @Override
    public Instance next() {
        //System.out.println(AR.Record.URL);
        AR.Next();
        Current++;
        return new Instance (AR.Record.ArchiveContent, Label, AR.Record.URL, null);
    }

    @Override
    public void remove() {
        AR.close();
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

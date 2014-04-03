/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import ArcFileUtils.MalletArcIterator;
import ArcFileUtils.MalletArcWebIterator;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import java.io.*;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class MalletFileConverter {

    public MalletFileConverter() {
    }


    public static void main (String[] args) throws IOException {

        // = new String[]{"001System.arc", "txtaomy.arc", "txtamnat.arc"};
        String InputFile = args.length > 0 ? args[0] : "resource/THContent.mallet";
        String ResultFile = args.length > 1 ? args[1] : "resource/THContentNewPipe.mallet";
        MalletWebArcTokenImport exporter = new MalletWebArcTokenImport();
        InstanceList IL = InstanceList.load(new File(InputFile));
        while(!IL.isEmpty())
            exporter.instances.addThruPipe(IL.remove(0));
        
        exporter.instances.save(new File(ResultFile));

    }

}

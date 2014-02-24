/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ArcFileUtils;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author malang
 */
public class ArcFilenameFilter implements FilenameFilter{

    @Override
    public boolean accept(File dir, String name) {
        return name.matches("(?i).*\\.w?arc(\\.gz)?$");
    }
    
}

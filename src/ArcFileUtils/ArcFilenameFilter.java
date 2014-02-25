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
    
    public final String PATTERN;
    
    public enum AcceptType{
        ArcOnly,
        WArcOnly,
        NonCompressedFile,
        ArcGzOnly,
        WArcGzOnly,
        CompressedFile,
        All
    }
    
    public ArcFilenameFilter(AcceptType AType){
        switch(AType){
            case ArcOnly:
                PATTERN = "(?i).*\\.arc$";
                break;
            case WArcOnly:
                PATTERN = "(?i).*\\.warc$";
                break;
            case NonCompressedFile:
                PATTERN = "(?i).*\\.w?arc$";
                break;
            case ArcGzOnly:
                PATTERN = "(?i).*\\.arc\\.gz$";
                break;
            case WArcGzOnly:
                PATTERN = "(?i).*\\.warc\\.gz$";
                break;
            case CompressedFile:
                PATTERN = "(?i).*\\.w?arc\\.gz$";
                break;
            default: // ALL
                PATTERN = "(?i).*\\.w?arc(\\.gz)?$";
                break;
        }
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.matches("(?i).*\\.w?arc(\\.gz)?$");
    }
    
    
}

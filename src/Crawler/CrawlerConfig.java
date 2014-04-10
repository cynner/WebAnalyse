/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.WebArcRecord;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wiwat
 */
public abstract class CrawlerConfig {
    
    public static enum Status {
        Finished(0), Crawling(1), NotBegin(9), NotInScope(-1), NoHostIP(-2), NoHostLocation(-3), Failed(-9) ;
        public final int value;
        private Status(int value){this.value = value;}
        public static Status GetKey(int val){
            for (Status d : Status.values())
                if(d.value == val)
                    return d;
            return null;
        }
    };
    
    
    public String AcceptOnlyPrefixPath = "/";
    
    public int MaxPreCrawl;
    public int MaxPage;
    public int MarginPage;
    public int log_id;
    public static final String DefaultRegAcceptedExt = "(\\.(?i)(html?|aspx?|php)|/)$";
    public static final String DefaultRegAcceptedPre = "^.*";
    public static final Pattern DefaultPatAcceptedPath = Pattern.compile("^.*(\\.(?i)(html?|aspx?|php)|/)$");

    public Pattern patAcceptedPath = DefaultPatAcceptedPath;
    
    public abstract void Finishing(SiteCrawler s);
    
    public abstract boolean isAccept(SiteCrawler s);
}

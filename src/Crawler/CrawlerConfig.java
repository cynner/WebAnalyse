/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.WebArcRecord;
import java.io.File;

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
    
    public static enum Mode {

        preCrawl, Crawl
    };
    
    public Mode mode = Mode.Crawl;
    public String AcceptOnlyPrefixPath = "/";
    
    public int MaxPreCrawl;
    public int MaxPage;
    public int MarginPage;
    public int log_id;
    
    public abstract void Finishing(SiteCrawler s);
    
    public abstract boolean isAccept(WebArcRecord f);
}

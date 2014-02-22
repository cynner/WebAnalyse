/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Example.lucene;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author malang
 */
public class TestSearch {
    
    public static void main(String[] args) throws ParseException, IOException{
        
        
    Analyzer analyzer = new ThaiAnalyzer(Version.LUCENE_45);
    
    Directory index = FSDirectory.open(new File("data/indexingonly"));
    
    // 2. query
    String querystr = args.length > 0 ? args[0] : "golf user";

    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    Query q = new MultiFieldQueryParser(Version.LUCENE_45, new String[] {"content"}, analyzer).parse(querystr);

    // 3. search
    int hitsPerPage = 10;
        try (IndexReader reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(q, collector);
            
            TopDocs td = collector.topDocs(5);
            ScoreDoc[] hits = td.scoreDocs;
            
            // 4. display results
            System.out.println("Found " + hits.length + " hits. from " + td.totalHits + " docs." );
            for(int i=0;i<hits.length;++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("url") + "\t" + d.get("title")+ "\t" + d.get("content"));
                
            }   }
    }
}

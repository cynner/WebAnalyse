/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main.WebAPI;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author malang
 */
public class Search {
    /**
     * 
     * @param args args[0] is a query
     * 
     * @throws IOException
     * @throws ParseException
     * @throws InvalidTokenOffsetsException 
     */
    
    public static void main(String[] args) throws IOException, ParseException, InvalidTokenOffsetsException {
        //... Above, create documents with two fields, one with term vectors (tv) and one without (notv)
        Analyzer analyzer = new ThaiAnalyzer(Version.LUCENE_45);

        Directory index = FSDirectory.open(new File("data/indexing"));
        String querystr = args.length > 0 ? args[0] : "mike lab";
        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query query = new MultiFieldQueryParser(Version.LUCENE_45, new String[] {"content"}, analyzer).parse(querystr);

        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        

        TopDocs hits = searcher.search(query, 10);

        SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
        Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
        String Preview;
        for (int i = 0; i < 10; i++) {
            int id = hits.scoreDocs[i].doc;
            Document doc = searcher.doc(id);
            String text;
            Preview = "";
            System.out.println(doc.get("url"));
            System.out.println(doc.get("title"));
            text = doc.get("content");
            TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "content", analyzer);
            TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, 10);//highlighter.getBestFragments(tokenStream, text, 3, "...");
            int k=0;
            for (TextFragment frag1 : frag) {
                if ((frag1 != null) && (frag1.getScore() > 0)) {
                    Preview += (frag1.toString()) + "...<br>";
                    k++;
                    // Get 2 Line Preview
                    if(k>=2)
                        break;
                }
            }
            //Term vector
            System.out.println("-------------");
        }
    }
}

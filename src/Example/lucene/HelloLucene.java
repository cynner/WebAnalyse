/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Example.lucene;

/**
 *
 * @author malang
 */
import java.io.File;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.store.FSDirectory;

public class HelloLucene {
  public static void main(String[] args) throws IOException, ParseException {
    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
    Analyzer analyzer = new ThaiAnalyzer(Version.LUCENE_45);
    

    // 1. create the index
    Directory index = FSDirectory.open(new File("indexing"));

    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);
      try (IndexWriter w = new IndexWriter(index, config)) {
          addDoc(w, "Lucene in Action 123.456", "193398817");
          addDoc(w, "Lucene for Dummies 123 456", "55320055Z");
          addDoc(w, "Managing Gigabytes 123456", "55063554A");
          addDoc(w, "พระพรหมพระพราย", "9900333X");
          addDoc(w, "พระมหากษัตริย์ไทยผู้ทรงวัยมาหานามะประเทศชาิตพัฒนาราวี", "9900333X");
      }

    // 2. query
    String querystr = args.length > 0 ? args[0] : "พรหม";

    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    Query q = new QueryParser(Version.LUCENE_45, "title", analyzer).parse(querystr);

    // 3. search
    int hitsPerPage = 10;
      try (IndexReader reader = DirectoryReader.open(index)) {
          IndexSearcher searcher = new IndexSearcher(reader);
          TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
          searcher.search(q, collector);
          ScoreDoc[] hits = collector.topDocs().scoreDocs;
          
          // 4. display results
          System.out.println("Found " + hits.length + " hits.");
          for(int i=0;i<hits.length;++i) {
              int docId = hits[i].doc;
              Document d = searcher.doc(docId);
              System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
          } }
  }

  private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("title", title, Field.Store.YES));

    // use a string field for isbn because we don't want it tokenized
    doc.add(new StringField("isbn", isbn, Field.Store.YES));
    
    //doc.add(new org.);
    w.addDocument(doc);
  }
}
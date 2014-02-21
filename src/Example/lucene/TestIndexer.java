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
import ArcFileUtils.ArcReader;
import java.io.File;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;

public class TestIndexer{
  public static void main(String[] args) throws IOException, ParseException {
    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
    Analyzer analyzer = new ThaiAnalyzer(Version.LUCENE_45);
    String InDirName = "data/test_snipped";
    File InDir = new File(InDirName);
    
    // 1. create the index
    Directory index = FSDirectory.open(new File("data/indexingonly"));

    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_45, analyzer);

    IndexWriter w = new IndexWriter(index, config);
    String [] s;
        int id=1;
    for(File f : InDir.listFiles()){
        ArcReader ar = new ArcReader(f);
        System.out.println(f.getName());
        while(ar.Next()){
            s = ar.Record.ArchiveContent.split("\n");
            switch(s.length){
                case 2:
                    addDoc(w, id++, ar.Record.URL,  s[0], s[1]);
                    break;
                case 1:
                    addDoc(w, id++, ar.Record.URL, s[0], "");
                    break;
                default:
                    break;
            }
        }
        ar.close();
    }
    w.close();

    // 2. query
    String querystr = args.length > 0 ? args[0] : "คอมพิวเตอร์";

    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    Query q = new QueryParser(Version.LUCENE_45, "title", analyzer).parse(querystr);

    // 3. search
    int hitsPerPage = 10;
    IndexReader reader = DirectoryReader.open(index);
    IndexSearcher searcher = new IndexSearcher(reader);
    
    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
    searcher.search(q, collector);
    ScoreDoc[] hits = collector.topDocs().scoreDocs;
    
    // 4. display results
    System.out.println("Found " + hits.length + " hits.");
    for(int i=0;i<hits.length;++i) {
      int docId = hits[i].doc;
      Document d = searcher.doc(docId);
      System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("url")  + "\t" + d.get("title")+ "\t" + d.get("content"));
    }

    // reader can only be closed when there
    // is no need to access the documents any more.
    reader.close();
  }

  private static void addDoc(IndexWriter w,int id, String url, String title, String content) throws IOException {
    Document doc = new Document();
    doc.add(new StoredField("id", id));
    doc.add(new TextField("url", url, Field.Store.YES));
    doc.add(new TextField("title", title, Field.Store.NO));
    // use a string field for isbn because we don't want it tokenized
    doc.add(new TextField("content", content, Field.Store.NO));
    
    //doc.add(new org.);
    w.addDocument(doc);
  }
}

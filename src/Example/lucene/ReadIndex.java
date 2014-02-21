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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

/**
 *
 * @author malang
 */
public class ReadIndex {

    public static void main(String[] args) throws IOException, ParseException {

        Analyzer analyzer = new ThaiAnalyzer(Version.LUCENE_45);

        Directory index = FSDirectory.open(new File("data/indexing"));

    // 2. query
        //String querystr = args.length > 0 ? args[0] : "golf user";
    // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        //Query q = new MultiFieldQueryParser(Version.LUCENE_45, new String[] {"content"}, analyzer).parse(querystr);
        //IndexReader indexReader = IndexReader.open(path);
        IndexReader reader = DirectoryReader.open(index);
        //IndexSearcher searcher = new IndexSearcher(reader);

    //Terms terms = SlowCompositeReaderWrapper.wrap(reader).terms("content");
        //TermsEnum te = terms.iterator(TermsEnum.EMPTY);
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms("content");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef = null;
        while ((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            int docFreq = iterator.docFreq();
            System.out.println(term + " " + docFreq);
        }
    }

}

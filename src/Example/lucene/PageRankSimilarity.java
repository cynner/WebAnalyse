/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Example.lucene;

import java.io.IOException;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author malang
 */
public class PageRankSimilarity extends Similarity {

    private final Similarity sim;

    public PageRankSimilarity(Similarity sim) {
        this.sim = sim; // wrap another similarity
    }

    @Override
    public long computeNorm(FieldInvertState fis) {
        return sim.computeNorm(fis);
    }

    @Override
    public SimWeight computeWeight(float f, CollectionStatistics cs, TermStatistics... tss) {
        return sim.computeWeight(f, cs, tss);
    }

    @Override
    public SimScorer simScorer(SimWeight sw, AtomicReaderContext arc) throws IOException {
        final Bits values = arc.reader().getDocsWithField("pageRank");
        return new SimScorer() {

            @Override
            public float score(int i, float f) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float computeSlopFactor(int i) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float computePayloadFactor(int i, int i1, int i2, BytesRef br) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        //return sim.simScorer(sw, arc) ;
    }
}
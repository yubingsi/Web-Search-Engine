package edu.nyu.cs.cs2580;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2 based on a refactoring of your favorite
 * Ranker (except RankerPhrase) from HW1. The new Ranker should no longer rely
 * on the instructors' {@link IndexerFullScan}, instead it should use one of
 * your more efficient implementations.
 */
public class RankerFavorite extends Ranker {
	public static double LAMBDA = 0.5;
	
  public RankerFavorite(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
	  ((IndexerInvertedCompressed) _indexer).loadCompressedIndex(query);
	  Vector<ScoredDocument> retrieval_results = new Vector<ScoredDocument> ();
	  int nextDocid = 0;
	   while(nextDocid < ((IndexerInvertedCompressed) _indexer).documents.size()) {
		   Document nextDoc= ((IndexerInvertedCompressed) _indexer).nextDoc(query, nextDocid);
		   System.out.println(nextDoc);
		   if(nextDoc == null) {
			   break;
		   }
		   nextDocid = nextDoc._docid;
		   retrieval_results.add(runquery(query, nextDoc));
	   }
	   if(retrieval_results.size() == 0) {
		   System.out.println("no match result");
		   return retrieval_results;
	   }

	    Collections.sort(retrieval_results);
	    Collections.reverse(retrieval_results);
	    if(retrieval_results.size() >= numResults){
		    List<ScoredDocument> tempDocuments = retrieval_results.subList(0, numResults);
		    Vector<ScoredDocument> temp=new Vector<ScoredDocument>();
		    temp.addAll(tempDocuments);
		    return temp;
	    }
	    return retrieval_results;
	    
  }
  
  /**
	 *  Use the information returned by the index in the document object for that document id.
	 * Accessing this information is performed through functions: 
	 * Vector<String> get_body_vector(): returns a vector of words in the document body.
	 * int get_numviews(): returns the number of times the document was viewed in the last hour.
	 * @param query
	 * @param did
	 * @return
	 */
	public ScoredDocument runquery(Query query, Document d){
	    return runQueryQL(d, query);
	}
	
	private ScoredDocument runQueryQL(Document d,Query query) {
		double score = 1.0;
		Vector<String> tokens = query._tokens;
		if(tokens == null) {
		}
		for(String term : tokens) {
			double temp = compute_QL_score(d,term);
			score = score * temp;
		}
		return new ScoredDocument(d,score);
	}
	
	private double compute_QL_score(Document d, String term) {
		int bodySize = ((IndexerInvertedCompressed)(_indexer)).documents.get(d._docid).getNumTerms();
		long total_terms_in_collection = _indexer._totalTermFrequency;
		long tf_in_collection = ((IndexerInvertedCompressed)(_indexer)).corpusTermFrequency(term);
		int frequency_in_doc = 0;
		if(term.contains(" ")) {
			// phrase
			frequency_in_doc = ((IndexerInvertedCompressed)(_indexer)).documentPhraseFrequency(term, d.getUrl());
		}
		else {
			// how many time term occured corpus;
			frequency_in_doc = ((IndexerInvertedCompressed)(_indexer)).documentTermFrequency(term, d.getUrl());
		}
		return (1-LAMBDA) * ((double)frequency_in_doc  / bodySize) + 
				LAMBDA *  ( (double)tf_in_collection / total_terms_in_collection);
		
	}
}

package edu.nyu.cs.cs2580;

/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */
public class DocumentIndexed extends Document {
	private static final long serialVersionUID = 9184892508124423115L;
	private Indexer indexer = null;

	private int numTerms;
	
	public DocumentIndexed(int docid, Indexer indexer) {
  		super(docid);
  		this.indexer = indexer;
  	}
	
	public void setNumTerms(int num) {
		this.numTerms = num;
	}
  
	public int getNumTerms() {
		return numTerms;
	}
	
	// Number of documents in which {@code term} appeared, over the full corpus.
	public int corpusDocFrequencyByTerm(String term) {
		return indexer.corpusDocFrequencyByTerm(term);
	}
	
	// Number of times {@code term} appeared in corpus. 
	public int corpusTermFrequency(String term) {
		return indexer.corpusTermFrequency(term);
	}
	
	// Number of times {@code term} appeared in the document {@code url}.
	public int documentTermFrequency(String term) {
		return indexer.documentTermFrequency(term, getUrl());
	}
	
	@Override
	public String toString() {
		return _docid + ";;" + getUrl() + ";;" + numTerms +" ";
	}
	
}

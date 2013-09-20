package edu.nyu.cs.cs2580;

/**
 * Document with score.
 * 
 * @author fdiaz
 * @author congyu
 */
class ScoredDocument implements Comparable<ScoredDocument> {
  private Document _doc;
  private double _score;

  public ScoredDocument(Document doc, double score) {
    _doc = doc;
    _score = score;
  }

  public Document getDocument() {
	  return this._doc;
  }
  public String asTextResult() {
    StringBuffer buf = new StringBuffer();
    buf.append(_doc._docid).append("\t");
    buf.append(_doc.getUrl()).append("\t");
    buf.append(_score);
    return buf.toString();
  }

  /**
   * @CS2580: Student should implement {@code asHtmlResult} for final project.
   */
  public String asHtmlResult() {
    return "";
  }

  @Override
  public int compareTo(ScoredDocument o) {
	double a = 0.000002;
	double b = 0.00005;
	double thisScore = this._score + _doc.getNumViews() * a + this._doc.getPageRank() * b;
	double oScore = o._score + o._doc.getPageRank() * b + o._doc.getNumViews() * a;
    if (thisScore == oScore) {
      return 0;
    }
    return (thisScore > oScore)? 1 : -1;
  }
}

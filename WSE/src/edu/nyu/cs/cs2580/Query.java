package edu.nyu.cs.cs2580;

import java.util.Scanner;
import java.util.Vector;

/**
 * Representation of a user query.
 * 
 * In HW1: instructors provide this simple implementation.
 * 
 * In HW2: students must implement {@link QueryPhrase} to handle phrases.
 * 
 * @author congyu
 * @auhtor fdiaz
 */
public class Query {
  public String _query = null;
  public Vector<String> _tokens = new Vector<String>();

  public Query(String query) {
    _query = query;
  }

  public void processQuery() {
    if (_query == null) {
      return;
    }
    Scanner s = new Scanner(_query);
    while (s.hasNext()) {
    	String tmp = s.next();
    	tmp = tmp.trim().toLowerCase();
    	tmp = FileManager.cleanString(tmp);
    	if(!tmp.equals("")) {
    		_tokens.add(new Stemmer().stem(tmp));
    	}    		
    }
    s.close();
  }
}

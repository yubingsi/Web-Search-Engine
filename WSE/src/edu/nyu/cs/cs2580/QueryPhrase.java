package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {
	
	public QueryPhrase(String query) {
		super(query);
	}

	@Override
	public void processQuery() {
		dealQuotes();
		dealHyphen();
	}
	
	// treat the string in quotes as unseparatable
	private void dealQuotes() {
		if(_query.contains("\"")) {
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			Matcher m = p.matcher(_query);
			String outQuote = _query;
			while (m.find()) {
				String tmp = m.group(1).trim().toLowerCase();
				tmp = FileManager.cleanString(tmp);
				_tokens.add(tmp);
				outQuote = outQuote.replaceAll(m.group(0), " ");
			}
			outQuote = FileManager.deleteMultiSpace(outQuote);
			String[] outQuotest = outQuote.trim().split(" ");
			for(String s: outQuotest) {
				s = s.trim().toLowerCase();
				if(!s.equals("")) {
					_tokens.add(s);
				}				
			}
		}
		else {
			String[] terms = _query.trim().split(" ");
			for(String s: terms) {
				s = s.trim().toLowerCase();
				if(!s.equals("")) {
					_tokens.add(s);
				}	
			}
		}		
	}
	
	private void dealHyphen() {
		Vector<String> tokens = new Vector<String>();
		for(String s: _tokens) {
			if(!s.contains("-") && !s.contains("_")) {
				tokens.add(s);
			}
		}
		for(String s: _tokens) {
			if(s.contains("-") || s.contains("_")) {
				String tmp = s;
				tmp = tmp.replaceAll("-", " ");
				tmp = tmp.replaceAll("_", " ");
				tokens.add(tmp);
			}
		}
		_tokens.clear();
		for(String s: tokens) {
			_tokens.add(s);
		}
	}
}

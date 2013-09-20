package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class QueryExpansion {
	Map<String,Integer> termOccurence = new HashMap<String,Integer>();
	List<Map.Entry<String,Double>> termPossibility = new ArrayList<Map.Entry<String,Double>>();
	double total;	
	Set<String> stopWords = new HashSet<String>();
	
	public QueryExpansion() {
		stopWords.add("the"); stopWords.add("of"); stopWords.add("and"); stopWords.add("in");
		stopWords.add("to"); stopWords.add("a"); stopWords.add("on");
		stopWords.add("for"); stopWords.add("by"); stopWords.add("with"); stopWords.add("was");
		stopWords.add("that"); stopWords.add("from"); stopWords.add("at"); stopWords.add("as");
		stopWords.add("is"); stopWords.add("this"); stopWords.add("it"); stopWords.add("an");
		stopWords.add("or"); stopWords.add("which"); stopWords.add("be"); stopWords.add("are");
		stopWords.add("has"); stopWords.add("us"); stopWords.add("on"); stopWords.add("you");
		stopWords.add("your"); stopWords.add("our"); stopWords.add("my"); stopWords.add("me");
		stopWords.add("his"); stopWords.add("he"); stopWords.add("i"); stopWords.add("1");
		stopWords.add("2"); stopWords.add("3"); stopWords.add("4"); 

	}
	public String runExpansion(Vector<ScoredDocument> scoredDocs, int numterms,String query) {
		if(scoredDocs.size() == 0) {
			// no relative doc
			System.out.println("No relative documents. Try a new query");
			return "";
		}
		for(ScoredDocument doc : scoredDocs) {
			String title = doc.getDocument().getUrl();
//			System.out.println(title);
			String[] docInfo = FileManager.readFile(title);
			String content = docInfo[1];
			processContent(content);
		}
		computePosibility(numterms);
		normalize();
		//print();
		return outputToFile(query);
	}
	
	// each term's occurrence time
	private void processContent(String content) {
		Scanner s = new Scanner(content);
		while(s.hasNext()) {
			String term = s.next();
			if(stopWords.contains(term) || term.length()<=1){
				continue;
			}
			Integer count = termOccurence.get(term);
			this.termOccurence.put(term, count==null? 1 : count+1);
		}
	}
	
	private void normalize() {
		for(Map.Entry<String,Double> termPossibility : this.termPossibility) {
			total += termPossibility.getValue();
		}
		
		if(total == 0) {
			System.out.println("total term should not be 0");
		}
	}
	
	private void computePosibility(int numterms) {
		double numerator = 0;
		double divider = 0;
		if(numterms > this.termOccurence.size()) {
			numterms = this.termOccurence.size();
		} 
		
		List<Map.Entry<String,Integer>> list = reversedSortMapByValue().subList(0, numterms);
	
		for(Map.Entry<String,Integer> termOccurence : list) {
			divider += termOccurence.getValue();
		}
		
		for(Map.Entry<String,Integer> termOccurence : list) {
			numerator = termOccurence.getValue();
			Map<String,Double> tempMap = new HashMap<String,Double>();
			tempMap.put(termOccurence.getKey(), numerator/divider);
			Set<Map.Entry<String,Double>> tempSet = tempMap.entrySet();
			for(Map.Entry<String,Double> entry : tempSet) {
				this.termPossibility.add(entry);
			}
			
		}
	}
	
	private List<Map.Entry<String,Integer>> reversedSortMapByValue() {
		List<Map.Entry<String,Integer>> list = 
				new ArrayList<Map.Entry<String,Integer>>(termOccurence.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {
			@Override
			public int compare(Map.Entry<String,Integer> arg0, Map.Entry<String,Integer> arg1) {
				return arg1.getValue() - arg0.getValue();
				
			}
		});
		return list;
	}
	
	private void print() {
		for(Map.Entry<String,Double> e : this.termPossibility) {
			System.out.println(e.getKey() + "\t" + e.getValue()/total);
		}
		
	}
	
	private String outputToFile(String fileName) {
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<String,Double> e : this.termPossibility) {
			sb.append(e.getKey()).append("\t").append(e.getValue()/total).append("\n");
		}
		
		String filePath = "data/queryExpansion/" + fileName;
		try {
			FileManager.saveFile(filePath, sb.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

}

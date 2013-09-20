package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Spearman {

	private Map<String,Double> pageRank = new HashMap<String,Double>();
	private Map<String,Double> numviews = new HashMap<String,Double>();
	
	private void rankPageRankValues(String pathToPageRank) throws IOException {
		pageRank = load(pathToPageRank);
		//System.out.println("page Rank size " + pageRank.size());
		List<Map.Entry<String, Double>> list = 
				new ArrayList<Map.Entry<String, Double>>(pageRank.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String,Double>>(){
			public int compare(Map.Entry<String, Double> arg0,
					Map.Entry<String, Double> arg1) {
				if(arg1.getValue() == arg0.getValue()) {
					return arg0.getKey().compareTo(arg1.getKey());
				}
				return (arg1.getValue() - arg0.getValue() > 0)? 1 : 0;
			}	
		});
		double rank = 1;
		//StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, Double> entry : list) {
			pageRank.put(entry.getKey(), rank);
//			sb.append(entry.getKey()).append("\t\t").append(rank).append("\n");
//			FileManager.saveFile("data/WenPageRank", sb.toString());
			rank ++;
		}
	}
	
	private void rankNumviewsValues(String pathToNumviews) throws IOException {
		numviews = load(pathToNumviews);
		List<Map.Entry<String, Double>> list = 
				new ArrayList<Map.Entry<String, Double>>(numviews.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String,Double>>(){
			public int compare(Map.Entry<String, Double> arg0,
					Map.Entry<String, Double> arg1) {
				if(arg1.getValue() == arg0.getValue()) {
					return arg0.getKey().compareTo(arg1.getKey());
				}
				return (arg1.getValue() - arg0.getValue() > 0)? 1 : 0;
			}	
		});
		double rank = 1;
		//StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, Double> entry : list) {
			numviews.put(entry.getKey(), rank);
//			sb.append(entry.getKey()).append("\t\t").append(rank).append("\n");
//			FileManager.saveFile("data/Wen", sb.toString());
			rank ++;
		}
	}
	
	private Map<String,Double> load(String path) throws IOException {
		Map<String,Double> values = new HashMap<String,Double>();
		System.out.println(path);
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();
		while(line != null) {
			String[] info = line.trim().split(";;");
			if(info.length != 2) {
				System.out.println("wrong file format");
				System.exit(-1);
			}
			try {
			values.put(info[0],Double.parseDouble(info[1]));
			} catch (NumberFormatException e) {
				System.out.println("line " + line);
			}
			line = br.readLine();
		}
		br.close();
		return values;
	}
	
	private double computeSpearmanRankCoefficient() {
		double numerator = 0;
		double divider = 0;
		//double Z = computeZ();
		
		Set<String> fileNames = numviews.keySet();
		//double temp1 = 0, temp2 = 0;
		for(String name : fileNames) {
			double xk = (pageRank.get(name) == null) ? 0 : pageRank.get(name);
			double yk = numviews.get(name) == null ? 0 :numviews.get(name);
			//numerator = (xk-Z) * (yk-Z) + numerator;
			//temp1 = Math.pow((xk-Z),2) + temp1;
			//temp2 = Math.pow((yk-Z),2) + temp2;
			numerator += Math.pow((xk - yk),2);		
		}
		double fileNum = fileNames.size();
		//divider = temp1 * temp2;
		//return numerator / divider;
		numerator = 6 * numerator;
		divider = fileNum * (Math.pow(fileNum, 2) - 1); 
		double p = 1 - numerator / divider;
		return p;
	}
	
//	private double computeZ() {
//		double Z = 0;
//		Set<String> fileNames = pageRank.keySet();
//		for(String name : fileNames) {
//			Z += pageRank.get(name);
//		}
//		return Z / pageRank.size();
//	}
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		String pathToPageRank = args[0];
		String pathToNumviews = args[1];
		Spearman spearman = new Spearman();
		
		try {
			spearman.rankPageRankValues(pathToPageRank);
			spearman.rankNumviewsValues(pathToNumviews);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(spearman.computeSpearmanRankCoefficient());	
	}

}

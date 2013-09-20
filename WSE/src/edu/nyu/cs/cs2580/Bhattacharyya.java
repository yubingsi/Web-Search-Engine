package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bhattacharyya {

	private String computeB(String pathToPrf,String pathToOutput) throws IOException {
		StringBuffer sb = new StringBuffer();
		List<Pair<String,String>> files = this.getFiles(pathToPrf);
		for(Pair<String,String> file : files) {
			for(Pair<String,String> fileTo : files) {
				if(fileTo.equals(file)){
					continue;
				}
				double B = this.computeTwoFile(file.getRight(), fileTo.getRight());
				sb.append(file.getLeft()).append("\t").append(fileTo.getLeft()).append("\t").append(B).append("\n");
			}
		}
		FileManager.saveFile(pathToOutput, sb.toString());
		return sb.toString();
	}
	
	private List<Pair<String,String>> getFiles(String pathToPrf) {
		List<Pair<String,String>> qm = new ArrayList<Pair<String,String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(pathToPrf));
			String line= br.readLine();
			while(line != null) {
				String query = line.trim().split(":")[0];
				String fileName = line.trim().split(":")[1];
				qm.add(new Pair<String,String>(query,fileName));
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return qm;
	}
	
	private double computeTwoFile(String fileName, String fileNameTo) throws IOException {
		double B = 0;
		BufferedReader brFrom = new BufferedReader(new FileReader(fileName));
		String lineFrom = brFrom.readLine();
		while(lineFrom != null) {
			String term = lineFrom.trim().split("\t")[0];
			String posibility = lineFrom.trim().split("\t")[1];
			BufferedReader brTo = new BufferedReader(new FileReader(fileNameTo));
			String lineTo = brTo.readLine();
			while(lineTo != null) {
				String termTo = lineTo.trim().split("\t")[0];
				String posibilityTo = lineTo.trim().split("\t")[1];
				if(term.equals(termTo)){
					B += Math.sqrt(Double.parseDouble(posibility) * Double.parseDouble(posibilityTo));
					break;
				}
				lineTo = brTo.readLine();
			}
			brTo.close();
			lineFrom = brFrom.readLine();
		}
		brFrom.close();
		return B;
	}
	public static void main(String[] args) {
		String pathToPrf = args[0];
		// String pathToPrf = "data/queryExpansion/";
		String pathToOutput = args[1];
		//String pathToOutput = "data/querySimilarity/similarity";
		Bhattacharyya BC = new Bhattacharyya();
		try {
			BC.computeB(pathToPrf, pathToOutput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("Similarity file alread generate under directory " + args[1]);
		System.out.println(args[0] + " " + args[1]);
	}

}

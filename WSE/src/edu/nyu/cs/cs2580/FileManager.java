package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileManager {	

	/**
	 * 
	 * @param folderPath
	 * @return
	 */
	public static List<String> getFileNames(String folderPath) {
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		List<String> filels = new ArrayList<String>();
		for(int i = 0; i<files.length; i++) {
			if(files[i].isFile() && !files[i].getName().endsWith(".DS_Store")) {
				filels.add(files[i].getName());
			}
		}
		return filels;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static String[] readFile(String fileName){
		String filePath = "data/wiki/" + fileName;
		String title = "";
		String content = "";
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = null;
			while ((line = inputf.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("<title>")) {
					line = line.substring(7);
					line = line.split("</")[0];
					title = cleanHTML(line.trim());
					title = deleteMultiSpace(title);
				}
				else {
					sb.append(line+ " ");
				}				
			}
			inputf.close();
			content = sb.toString();
		} catch (IOException io) {}
		content = cleanHTML(content);
		content = deleteMultiSpace(content);
		content = content.toLowerCase();
		String[] result = {title, content};
		return result;
	}
	
	
	
	
	public static Map<String,Integer> readDocDictionary(String docDicFile) {
		String filePath = "data/pageRank/" + docDicFile;
		Map<String,Integer> docDictionary = new HashMap<String,Integer>();
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = "";
			while ((line = inputf.readLine()) != null) {
				line = line.trim();
				String[] maps = line.split(";;");
				docDictionary.put(maps[0], Integer.parseInt(maps[1]));
			}
			inputf.close();			
		}catch(IOException io) {
			io.printStackTrace();
		}
		return docDictionary;
	}
	
	public static Map<Integer,String> readRevDocDictionary(String docDicFile) {
		String filePath = "data/pageRank/" + docDicFile;
		Map<Integer,String> docDictionary = new HashMap<Integer,String>();
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = "";
			while ((line = inputf.readLine()) != null) {
				line = line.trim();
				String[] maps = line.split(";;");
				docDictionary.put(Integer.parseInt(maps[1]), maps[0]);
			}
			inputf.close();			
		}catch(IOException io) {
			io.printStackTrace();
		}
		return docDictionary;
	}
	
	public static double[] readDocLinksColumn(int numDocs, int docid, double lambda,  Map<Integer,Integer> linkCount) {
		double[] w = new double[numDocs];
		double iniValue = (1-lambda)/numDocs;	
		String folderPath = "data/mining";
		List<String> fileNames = getFileNames(folderPath);
		for(String fileName: fileNames) {
			if(fileName.contains("links")) {
				String filePath = "data/mining/" + fileName;
				try {
					BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
					String line = "";
					while ((line = inputf.readLine()) != null) {
						line = line.trim();
						String[] maps = line.split(",");
						if(Integer.parseInt(maps[1]) == docid) {	
							w[Integer.parseInt(maps[0])-1] = lambda*1.0/linkCount.get(Integer.parseInt(maps[0])) + iniValue;
						}
					}
					inputf.close();			
				}catch(IOException io) {
					io.printStackTrace();
				}
			}	
		}
		return w;
	}
	
	public static double[] readDocLinksColumnPure(int numDocs, int docid, Map<Integer,Integer> linkCount) {
		double[] w = new double[numDocs];
		String folderPath = "data/mining";
		List<String> fileNames = getFileNames(folderPath);
		for(String fileName: fileNames) {
			if(fileName.contains("links")) {
				String filePath = "data/mining/" + fileName;
				try {
					BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
					String line = "";
					while ((line = inputf.readLine()) != null) {
						line = line.trim();
						String[] maps = line.split(",");
						if(Integer.parseInt(maps[1]) == docid) {	
							w[Integer.parseInt(maps[0])-1] = 1.0/linkCount.get(Integer.parseInt(maps[0]));
						}
					}
					inputf.close();			
				}catch(IOException io) {
					io.printStackTrace();
				}
			}	
		}
		return w;
	}
	
	public static double[] readDocLinksRow(int numDocs, int docid) {
		double[] w = new double[numDocs];
		String folderPath = "data/mining";
		List<String> fileNames = getFileNames(folderPath);
		boolean findDoc = false;
		int count = 0;
		for(String fileName: fileNames) {
			if(fileName.contains("links")) {
				String filePath = "data/mining/" + fileName;
				try {
					BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
					String line = "";
					while ((line = inputf.readLine()) != null) {
						line = line.trim();
						String[] maps = line.split(",");
						if(Integer.parseInt(maps[0]) == docid) {
							findDoc = true;
							count++;
							w[Integer.parseInt(maps[1])-1] = 1.0;
						}
					}
					inputf.close();			
				}catch(IOException io) {
					io.printStackTrace();
				}
			}
			if(findDoc) {
				break;
			}
		}
		if(count != 0) {
			for(int i = 0; i<numDocs; i++) {
				w[i] = w[i]/count;
			}
		}		
		return w;
	}
	
	public static double[] getColumn(int numDocs, int col, int lmb, Map<Integer,Integer> linkCount) {
		double[] column = new double[numDocs];
		double lambda = lmb*1.0/10;
		double iniValue = (1-lambda)/numDocs;	
		int quantity = 600;  // each file has 300 rows;
		int fileSuffix = col/quantity;
		int colNum = col%quantity;
		String countString = String.format("%02d", fileSuffix); 
		String filePath = "data/linkMatrix/linkMC"+"_"+countString;
		int count = 0;
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
//			BufferedReader inputf = new BufferedReader(new FileReader(filePath));

			String line = "";
			while((line = inputf.readLine()) != null) {
				if(count == colNum) {
					line = line.trim();
					String[] st = line.split(",");
					for(int i = 0; i<st.length; i++) {
						column[i] = lambda*Double.parseDouble(st[i])/linkCount.get(i+1) + iniValue;
					}
					break;
				}
				count++;
			}
			inputf.close();	
		}catch(IOException io) {
			io.printStackTrace();
		}
		return column;
	}
	
	public static List<Double> getScores(int numDocs, int lmb, Map<Integer,Integer> linkCount) {
		List<Double> scores = new ArrayList<Double>();
		List<String> fileNames = getFileNames("data/linkMatrix");
		List<Double> sumRow = sumRow(numDocs, lmb, linkCount);
		Collections.sort(fileNames);
		for(int i = 0; i<fileNames.size(); i++) {
			String rowFilePath = "data/linkMatrix/"+fileNames.get(i);
System.out.println(fileNames.get(i));
			List<double[]> rows = getRowsInFile(numDocs, rowFilePath, lmb);
			for(int j = 0; j<rows.size(); j++) {
				double[] row = rows.get(j);
				double score = 0.0;
				for(int k = 0; k<numDocs; k++) {
					score += row[k]*sumRow.get(k);
				}
				scores.add(score);
System.out.println(score);
			}
		}
		return scores;
	}
	
	private static List<Double> sumRow(int numDocs, int lmb, Map<Integer,Integer> linkCount) {
		double lambda = lmb*1.0/10;
		double iniValue = (1-lambda)/numDocs;	
		List<Double> sumRows = new ArrayList<Double>();
		List<String> fileNames = getFileNames("data/linkMatrix");
		Collections.sort(fileNames);
		for(int i = 0; i<fileNames.size(); i++) {
			String filePath = "data/linkMatrix/"+fileNames.get(i);
System.out.println(fileNames.get(i));

			try {
				BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
				String line = "";
				while((line = inputf.readLine()) != null) {
					String[] st = line.trim().split(",");
					double sum = 0.0;
					for(int j = 0; j<numDocs; j++) {
						sum += (lambda*Double.parseDouble(st[i]) + iniValue);
					}
					sumRows.add(sum);
				}
				inputf.close();
			}catch(IOException io) {
				io.printStackTrace();
			}
		}
		return sumRows;
	}
	
	
	
	public static List<double[]> getRowsInFile(int numDocs, String filePath, int lmb) {
		List<double[]> rows = new ArrayList<double[]>();
		double lambda = lmb*1.0/10;
		double iniValue = (1-lambda)/numDocs;	
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = "";
			while((line = inputf.readLine()) != null) {
				double[] row = new double[numDocs];
				String[] st = line.trim().split(",");
				for(int i = 0; i<numDocs; i++) {
					row[i] = (lambda*Double.parseDouble(st[i]) + iniValue);
				}
				rows.add(row);
			}
			inputf.close();
		}catch(IOException io) {
			io.printStackTrace();
		}
		return rows;
	}
	
	public static double[] getRow(int numDocs, int r, int lmb, int numNunZero) {
		double[] row = new double[numDocs];
		double lambda = lmb*1.0/10;
		double iniValue = (1-lambda)/numDocs;	
		int quantity = 600;  // each file has 300 rows;
		int fileSuffix = r/quantity;
		int rowNum = r%quantity;
		String countString = String.format("%02d", fileSuffix); 
		String filePath = "data/linkMatrix/linkMR"+"_"+countString;
		int count = 0;
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
//			BufferedReader inputf = new BufferedReader(new FileReader(filePath));

			String line = "";
			while((line = inputf.readLine()) != null) {
				if(count == rowNum) {
					line = line.trim();
					String[] st = line.split(",");
					for(int i = 0; i<st.length; i++) {
						row[i] = lambda*Double.parseDouble(st[i])/numNunZero + iniValue;
					}
					break;
				}
				count++;
			}
			inputf.close();	
		}catch(IOException io) {
			io.printStackTrace();
		}
		return row;
	}
	
	
	
	public static Map<Integer,Integer> getLinkCount(int numDocs) {
		Map<Integer,Integer> linkCount = new HashMap<Integer,Integer>();
		for(int i = 0; i<numDocs; i++) {
			linkCount.put(i+1, 1);
		}
		String folderPath = "data/mining";
		List<String> fileNames = getFileNames(folderPath);
		int preLeft = 0;
		int currLeft = 0;
		int count = 0;
		for(String fileName: fileNames) {
			if(fileName.contains("links")) {
				String filePath = "data/mining/" + fileName;
				try {
					BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
					String line = "";
					while ((line = inputf.readLine()) != null) {
						line = line.trim();
						String[] maps = line.split(",");
						currLeft = Integer.parseInt(maps[0]);
						if(currLeft != preLeft && preLeft != 0) {							
							linkCount.put(preLeft, count);
						}
						else {
							count++;
						}	
						preLeft = currLeft;
					}
					linkCount.put(preLeft, count);     // the last pair
					inputf.close();			
				}catch(IOException io) {
					io.printStackTrace();
				}
			}
		}
		return linkCount;
	}
	
	
	public static Map<String,Double> getPageRank(double lambda, String iteration) {
		int tmp = (int)(lambda*10);
		Map<String,Double> pageRank = new HashMap<String,Double>();
		String docDicFile = "docDic";
		Map<Integer,String> docDictionary = readRevDocDictionary(docDicFile);
		String fileName = iteration+"_"+tmp;
		String filePath = "data/pageRank/" + fileName;
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = "";
			while ((line = inputf.readLine()) != null) {
				line = line.trim();
				String[] maps = line.split(",");
				pageRank.put(docDictionary.get(Integer.parseInt(maps[0])), Double.parseDouble(maps[1]));
			}
			inputf.close();			
		}catch(IOException io) {
			io.printStackTrace();
		}
		return pageRank;
	}
	
	public static void writePageRankFile() throws IOException {
		Map<String,Double> oneIterPageRank1 = FileManager.getPageRank(0.10, "oneIter"); // one iteration, lambda=0.1
		Map<String,Double> oneIterPageRank9 = FileManager.getPageRank(0.90, "oneIter"); // one iteration, lambda=0.9
		Map<String,Double> twoIterPageRank1 = FileManager.getPageRank(0.10, "twoIter");
		Map<String,Double> twoIterPageRank9 = FileManager.getPageRank(0.90, "twoIter");
		String str1 = mapToString(oneIterPageRank1);
		saveFile("data/pageRank/pageRankOneIter_1",str1);
		String str2 = mapToString(oneIterPageRank9);
		saveFile("data/pageRank/pageRankOneIter_9",str2);
		String str3 = mapToString(twoIterPageRank1);
		saveFile("data/pageRank/pageRankTwoIter_1",str3);
		String str4 = mapToString(twoIterPageRank9);
		saveFile("data/pageRank/pageRankTwoIter_9",str4);
		
	}
	
	private static String mapToString(Map<String,Double> map) {
		Set<String> keySet = map.keySet();
		StringBuffer sb = new StringBuffer();
		for(String key : keySet) {
			sb.append(key).append(";;").append(map.get(key)).append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param content
	 * @return
	 */
	public static String cleanHTML(String content) {
		String res = content.replaceAll("<[\\s]*?head[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?head[\\s]*?>", " ");	// head		
		res = res.replaceAll("<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>", " ");	// css
		res = res.replaceAll("<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>", " ");	// script
		res = res.replaceAll("<!-- [\\s]*?jumpto[^>]*? -->[\\s\\S]*?<!-- [\\s]*?\\/[\\s]*?jumpto[\\s]*?-->", " ");		
		res = res.replaceAll("&#160;", " ");
		res = res.replaceAll("#160;", " ");
		res = res.replaceAll("&#62;", " ");
		res = res.replaceAll("&#60;", " ");
		res = res.replaceAll("<[^>]+>", " ");
		res = res.replaceAll("&amp;", " ");
		res = res.replaceAll("'", "");
		res = res.replaceAll("\\.", "");
		res = res.replaceAll("[^A-Za-z0-9]", " ");   // remove all non-alphanumeric characters
		return res;
	}
	
	public static String cleanString(String s) {
		String res = s.replaceAll("'", "");
		res = res.replaceAll("\\.", "");
		res = res.replaceAll("[^A-Za-z0-9]", " ");   // remove all non-alphanumeric characters
		return res;
	}
	
	static String deleteMultiSpace(String st) {
		boolean containSpace = st.matches(".*\\s+.*");
		StringBuffer sb = new StringBuffer();
		if(containSpace) {
			String[] starray = st.split(" ");
			for(String s: starray) {
				if(s.matches(".*\\w.*")) {
					sb.append(s.trim()+" ");
				}
			}
			return sb.toString();
		}
		else {
			return st;
		}		
	}
	
	/**
	 * @param documents 
	 * @param outpustream OutputStream
	 * @throws Exception
	 */
	public static void saveFile (String filePath, String content) throws IOException {
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath));
			out.write(content);
			out.close();
		} catch (Exception ex) {}
	}
	
	public static void saveFile2 (String filePath, String content) throws Exception {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
			out.write(content);
			out.close();
		} catch (Exception ex) {}
	}
	
	public static void saveFile2(String filePath, Map<Integer, DocumentIndexed> documents) throws Exception {
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filePath));
			StringBuffer sb = new StringBuffer();
			for(DocumentIndexed doc: documents.values()) {
				sb.append(doc.toString());
			}
			sb.append("\n");
			out.write(sb.toString());
			out.close();
		} catch (Exception ex) {}
	}
	
	public static Map<Integer, DocumentIndexed> translateDocs(String filePath, Indexer indexer) {
		Map<Integer, DocumentIndexed> documents = new HashMap<Integer, DocumentIndexed>();
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = null;
			while ((line = inputf.readLine()) != null) {				
				String[] lines = line.trim().split(" ");
				for(String s: lines) {
					String[] ss = s.trim().split(";;");
					int did = Integer.parseInt(ss[0]);
					DocumentIndexed doc = new DocumentIndexed(did, indexer);
					doc.setUrl(ss[1]);
					doc.setNumTerms(Integer.parseInt(ss[2]));
//					String pageRankPath = "data/pageRank/pageRankTwoIter_1";
//					float pageRank = getPageRankOrNumview(pageRankPath,ss[1]);
//					String numviewsPath = "data/numviews/numviews";
//					int numviews = (int)getPageRankOrNumview(numviewsPath,ss[1]);
//					doc.setPageRank(pageRank);
//					doc.setNumViews(numviews);
					documents.put(did, doc);
				}
			}			
			inputf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return documents;
	}
	
//	private static float getPageRankOrNumview(String filePath,String title) throws IOException {
//		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
//		String line = null;
//		while ((line = br.readLine()) != null) {				
//			String[] lines = line.trim().split(";;");
//			if(lines.length != 2) {
//				System.out.println("wrong file format for page rank");
//			}
//			else if(lines[0].equals(title)){
//				br.close();
//				return Float.parseFloat(lines[1]);
//			}
//		}
//		br.close();
//		return 0;
//	}
	public static Map<String,ArrayList<Pair<Integer,Integer>>> translateTermOccurrences(String filePath) {
		Map<String,ArrayList<Pair<Integer,Integer>>> termOccurrences = new HashMap<String,ArrayList<Pair<Integer,Integer>>>();
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = null;
			while ((line = inputf.readLine()) != null) {				
				line = line.trim();
				String[] lineArray = line.split(":");
				String term = lineArray[0];
				String[] pairs = lineArray[1].trim().split(" ");
				ArrayList<Pair<Integer,Integer>> pairls = new ArrayList<Pair<Integer,Integer>>();
				for(String s: pairs) {
					s = s.replaceAll("[^A-Za-z0-9]", " ");   // remove all non-alphanumeric characters
					s = s.trim();
					String[] p = s.split(" ");
					Pair<Integer,Integer> pair = new Pair<Integer,Integer>(Integer.parseInt(p[0]),Integer.parseInt(p[1]));
					pairls.add(pair);
				}
				termOccurrences.put(term, pairls);		
			}
			inputf.close();
		} catch (IOException io) {}
		return termOccurrences;
	}
	
	public static Map<String,ArrayList<Pair<Integer,Integer>>> extractTermOccurrences(String targetTerm, String filePath) {
		Map<String,ArrayList<Pair<Integer,Integer>>> termOccurrences = new HashMap<String,ArrayList<Pair<Integer,Integer>>>();
		try {
			BufferedReader inputf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
			String line = null;
			while ((line = inputf.readLine()) != null) {				
				line = line.trim();
				String[] lineArray = line.split(":");
				String term = lineArray[0];
				if(term.equals(targetTerm)) {
					String[] pairs = lineArray[1].trim().split(" ");
					ArrayList<Pair<Integer,Integer>> pairls = new ArrayList<Pair<Integer,Integer>>();
					for(String s: pairs) {
						s = s.replaceAll("[^A-Za-z0-9]", " ");   // remove all non-alphanumeric characters
						s = s.trim();
						String[] p = s.split(" ");
						Pair<Integer,Integer> pair = new Pair<Integer,Integer>(Integer.parseInt(p[0]),Integer.parseInt(p[1]));
						pairls.add(pair);
					}
					termOccurrences.put(term, pairls);
				}						
			}
			inputf.close();
		} catch (IOException io) {}
		return termOccurrences;
	}
	
	

	public static void main(String[] args) {
		 String[] res = readFile("0_(year)");
//		 System.out.println(res[1]);
//		 Stemmer stemmer = new Stemmer();
//		 System.out.println(stemmer.stem("buddhist"));
		String folderPath = "data/wiki";
		List<String> fileNames = getFileNames(folderPath);
		for(int i = 0; i<fileNames.size(); i++) {
			
		}
	}
}

package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
	private static Map<String,Integer> docDictionary = new HashMap<String,Integer>();
	private List<Pair<Integer,Integer>> linkPairs = new ArrayList<Pair<Integer,Integer>>();
	public Map<String,Double> pageRank = new HashMap<String,Double>();
	private int _numDocs = 0;
	public CorpusAnalyzerPagerank(Options options) {
		super(options);
	}

	/**
	 * This function processes the corpus as specified inside {@link _options}
	 * and extracts the "internal" graph structure from the pages inside the
	 * corpus. Internal means we only store links between two pages that are
	 * both inside the corpus.
	 * 
	 * Note that you will not be implementing a real crawler. Instead, the
	 * corpus you are processing can be simply read from the disk. All you need
	 * to do is reading the files one by one, parsing them, extracting the links
	 * for them, and computing the graph composed of all and only links that
	 * connect two pages that are both in the corpus.
	 * 
	 * Note that you will need to design the data structure for storing the
	 * resulting graph, which will be used by the {@link compute} function.
	 * Since the graph may be large, it may be necessary to store partial graphs
	 * to disk before producing the final graph.
	 * 
	 * @throws IOException
	 */
	@Override
	public void prepare() throws IOException {
		String s = "Æ";
		System.out.println("Preparing " + this.getClass().getName());
		String folderPath = "data/wiki";
		List<String> fileNames = FileManager.getFileNames(folderPath);
		int count = 0;
		int quantity = 2000;
		while(count*quantity<fileNames.size()) {
			for(int i = 0; i<quantity; i++) {
				if(count*quantity+i < fileNames.size()) {
					String fileName = fileNames.get(count*quantity+i);
					System.out.println(fileName);
					if(!docDictionary.containsKey(fileName)) {
						_numDocs++;
						docDictionary.put(fileName, _numDocs);
					}					
					String filePath = "data/wiki/"+fileName;
					File file = new File(filePath);
					CorpusAnalyzer.HeuristicLinkExtractor extractor = new CorpusAnalyzer. HeuristicLinkExtractor(file);	
					String nextInCorpusLinkTarget = extractor.getNextInCorpusLinkTarget();
					System.out.println(nextInCorpusLinkTarget);
					while(nextInCorpusLinkTarget!= null ) {
						if(super.isValidDocument(new File(nextInCorpusLinkTarget)) && fileNames.contains(nextInCorpusLinkTarget)) {
							if(!docDictionary.containsKey(nextInCorpusLinkTarget)) {
								_numDocs++;
								docDictionary.put(nextInCorpusLinkTarget, _numDocs);
							}	
							Pair<Integer,Integer> pair = new Pair<Integer,Integer>(docDictionary.get(fileName),docDictionary.get(nextInCorpusLinkTarget));
							if(!linkPairs.contains(pair)) {
								linkPairs.add(pair);
							}		
						}
						nextInCorpusLinkTarget = extractor.getNextInCorpusLinkTarget();
					}					
				}
			}
			String countString = String.format("%02d", count); 
			String linkToFile =  "data/mining/links"+countString;
			StringBuffer sb = new StringBuffer();
			for(Pair<Integer,Integer> pair: linkPairs) {
				sb.append(pair.toString()+"\n");
			}
			try {
				FileManager.saveFile(linkToFile, sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			count++;
			linkPairs.clear();
		}
		printDic();
	}
	
	


	/**
	 * This function computes the PageRank based on the internal graph generated
	 * by the {@link prepare} function, and stores the PageRank to be used for
	 * ranking.
	 * 
	 * Note that you will have to store the computed PageRank with each document
	 * the same way you do the indexing for HW2. I.e., the PageRank information
	 * becomes part of the index and can be used for ranking in serve mode.
	 * Thus, you should store the whatever is needed inside the same directory
	 * as specified by _indexPrefix inside {@link _options}.
	 * 
	 * @throws IOException
	 */
	@Override
	public void compute() throws IOException {
		System.out.println("Computing using " + this.getClass().getName());
		String docDicFile = "docDic";
		docDictionary = FileManager.readDocDictionary(docDicFile);
		System.out.println("docDictionary loaded, size="+docDictionary.size());
		int numDocs = docDictionary.size();
		Map<Integer,Integer> linkCount = FileManager.getLinkCount(numDocs);
		
		double lambda1 = 0.10;
		double lambda2 = 0.90;
		
		System.out.println("\n one iteration");
//		computeOneIteration(lambda1, linkCount, numDocs);

//		computeOneIteration(lambda2, linkCount, numDocs);

//		saveRowFiles(numDocs, linkCount);

		System.out.println("\n two iterations");	
		computeTwoIterations(lambda1, linkCount, numDocs);

		computeTwoIterations(lambda2, linkCount, numDocs);
		//write <Stirng,Double> to file
		FileManager.writePageRankFile();

	}

	/**
	 * During indexing mode, this function loads the PageRank values computed
	 * during mining mode to be used by the indexer.
	 * 
	 * @throws IOException
	 */
	@Override
	public Object load() throws IOException {
		System.out.println("Loading using " + this.getClass().getName());
		double lambda1 = 0.10;
		double lambda2 = 0.90;
//		Map<String,Double> oneIterPageRank1 = FileManager.getPageRank(lambda1, "oneIter"); // one iteration, lambda=0.1
//		Map<String,Double> oneIterPageRank9 = FileManager.getPageRank(lambda2, "oneIter"); // one iteration, lambda=0.9
//		Map<String,Double> twoIterPageRank1 = FileManager.getPageRank(lambda1, "twoIter");
		Map<String,Double> twoIterPageRank9 = FileManager.getPageRank(lambda2, "twoIter");
		/////====choose one page rank. discuss the result and chose one kind page rank====
		this.pageRank = twoIterPageRank9;
		return this;
	}
	
	private void computeOneIteration(double lambda, Map<Integer,Integer> linkCount, int numDocs) {
		Map<Integer,Double> oneIterPageRank = new HashMap<Integer,Double>();
		int tmp = (int)(lambda*10);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<numDocs; i++) {
			double[] column = FileManager.readDocLinksColumn(numDocs, i+1, lambda, linkCount);
			double score = 0.0;
			for(double k: column) {
				score += k;
			}
			oneIterPageRank.put(i+1, score);
			if((i+1)%100 == 0) {
				System.out.println(i+1+" "+score);
			}
		}
		String oneIterFile = "data/pageRank/oneIter_"+tmp;
		for(int docid = 1; docid<numDocs+1; docid++) {
			sb.append(docid+","+oneIterPageRank.get(docid)+"\n");
		}
		try {
			FileManager.saveFile(oneIterFile, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private void computeTwoIterations(double lambda, Map<Integer,Integer> linkCount, int numDocs) {
		int tmp = (int)(lambda*10);
		StringBuffer sb = new StringBuffer();
		List<Double> scores = FileManager.getScores(numDocs, tmp, linkCount); 
		String twoIterFile = "data/pageRank/twoIter_"+tmp;
		for(int docid = 1; docid<numDocs+1; docid++) {
			sb.append(docid+","+scores.get(docid-1)+"\n");
		}
		try {
			FileManager.saveFile(twoIterFile, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
	
	private void saveRowFiles(int numDocs, Map<Integer,Integer> linkCount) {
		List<double[]> rows = new ArrayList<double[]>();
		int count = 0;
		int quantity = 600;
		while(count*quantity<numDocs) {
			for(int i = 0; i<quantity; i++) {
				if(count*quantity+i < numDocs) {
					double[] row = FileManager.readDocLinksRow(numDocs, count*quantity+i+1);
					rows.add(row);
if((count*quantity+i+1)%100 == 0) {
	System.out.println((count*quantity+i+1)+" row");
}
				}
			}
			String countString = String.format("%02d", count); 			
			String linkMFile = "data/linkMatrix/linkMR"+"_"+countString;
			StringBuffer sb = new StringBuffer();
			for(double[] r: rows) {				
				for(double b: r) {					
					sb.append(b+",");
				}
				sb.append("\n");
			}
			try {
				FileManager.saveFile(linkMFile, sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			count++;
			sb = new StringBuffer();
			rows.clear();
		}
		System.out.println("\n link matrix for rows finished");
	}
	
	private void printDic() {
		String docDicFile = "data/pageRank/docDic";
		StringBuffer sb = new StringBuffer();
		for(String docTitle: docDictionary.keySet()) {
			sb.append(docTitle+";;"+docDictionary.get(docTitle)+"\n");
		}
		try {
			FileManager.saveFile(docDicFile, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

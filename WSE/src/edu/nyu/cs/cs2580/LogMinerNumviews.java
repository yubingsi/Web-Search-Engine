package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class LogMinerNumviews extends LogMiner {

	private List<String> fileNames;
	//private Map<String,String> numviews;
	public Map<String,Integer> numviews;
	public LogMinerNumviews(Options options) {
		super(options);
		fileNames = FileManager.getFileNames("data/wiki"/*_options._corpusPrefix*/);
		numviews = new HashMap<String,Integer>();
		for(String title : fileNames) {
			numviews.put(title,0);
		}
	}

	/**
	 * This function processes the logs within the log directory as specified by
	 * the {@link _options}. The logs are obtained from Wikipedia dumps and have
	 * the following format per line: [language]<space>[article]<space>[#views].
	 * Those view information are to be extracted for documents in our corpus
	 * and stored somewhere to be used during indexing.
	 * 
	 * Note that the log contains view information for all articles in Wikipedia
	 * and it is necessary to locate the information about articles within our
	 * corpus.
	 * 
	 * @throws IOException
	 */
	@Override
	@SuppressWarnings("resource")
	public void compute() throws IOException {
		System.out.println("Computing using " + this.getClass().getName());

		System.out.println("computing...");
		BufferedReader br = new BufferedReader(new FileReader("data/log" + "/20130301-160000.log"/*_options._logPrefix*/));
		String line = br.readLine();
		
		while (line != null) {
			String[] logInfo = line.trim().split(" ");
			if(logInfo.length != 3) {
				line = br.readLine();
				continue;
			}
			int val = 0;
			try{
				val = Integer.parseInt(logInfo[2]);
			} catch (Exception e) {
				line = br.readLine();
				continue;
				//System.out.println("debug " + line);
				
			}
			if(fileNames == null) {
				throw new IOException("corpus is empty");
			}
			String title = StringEscapeUtils.unescapeHtml(logInfo[1]);
			if(fileNames.contains(title)) {
				numviews.put(title, val);
			}
			
			line = br.readLine();
		}
		br.close();
		writeToDisk();
		return;
	}

	private void writeToDisk() {
		String filePath = "data/numviews/numviews";
		StringBuffer sb = new StringBuffer();
		Set<String> keySet = numviews.keySet();
		for(String key : keySet) {
			sb.append(key).append(";;").append(numviews.get(key)).append("\n");
		}
		//System.out.println(sb.toString());
		try {
			FileManager.saveFile(filePath, sb.toString());
		} catch (Exception e) {
			System.out.println("file operation failed in menthod writeToDisk");
		}
		  
	}
//	public static void main(String[]  args) {
//		  LogMinerNumviews nm = new LogMinerNumviews(null);
//		  try {
//			  nm.compute();
//			//nm.load();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		  
////		  Set<String> keySet = nm.numviews.keySet();
////			for(String key : keySet) {
////				System.out.println("(" + key + "," + nm.numviews.get(key) + ")");
////			}
//	  }
	
	/**
	 * During indexing mode, this function loads the NumViews values computed
	 * during mining mode to be used by the indexer.
	 * 
	 * @throws IOException
	 */
	@Override
	public Object load() throws IOException {
		System.out.println("Loading using " + this.getClass().getName());
		
		String filePath = "data/numviews/numviews";
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		while(line != null) {
			String[] logInfo = line.trim().split(";;");
			if(logInfo.length != 2){
				System.out.println(line);
			}
			this.numviews.put(logInfo[0], Integer.parseInt(logInfo[1]));
			line = br.readLine();
		}
		br.close();
		return null;
	}
}

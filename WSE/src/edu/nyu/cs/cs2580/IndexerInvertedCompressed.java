package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import javax.swing.text.Position;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 * This class should perform occurrences indexing but with the resulting posting lists compressed.
 */
public class IndexerInvertedCompressed extends Indexer {
//	private static Map<String, Pair<Integer,BitSet>> compressedIndex = new HashMap<String, Pair<Integer,BitSet>>();
	public HashMap<String,Vector<byte[]>> compressedIndex = new HashMap<String,Vector<byte[]>>();
	private Map<String,ArrayList<Pair<Integer,Integer>>> termOccurrences = new HashMap<String,ArrayList<Pair<Integer,Integer>>>();
	private Map<String,ArrayList<Integer>> termOffset = new HashMap<String, ArrayList<Integer>>();
	private Stemmer stemmer = new Stemmer();
	public Map<Integer, DocumentIndexed> documents = new HashMap<Integer, DocumentIndexed>();
	
	public IndexerInvertedCompressed(Options options) {
		super(options);
		System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	}

  	@Override
  	public void constructIndex() throws IOException {	  
  		String folderPath = "data/wiki";
  		List<String> fileNames = FileManager.getFileNames(folderPath);
  		int count = 0;
  		int quantity = 1000;
  		while(count*quantity<fileNames.size()) {
  			for(int i = 0; i<quantity; i++) {				
  				if(count*quantity+i < fileNames.size()) {
//  		for(int i = 0; i<300; i++) {
  					_numDocs++;
  					String[] rawFile = FileManager.readFile(fileNames.get(quantity*count+i));
//  					String[] rawFile = FileManager.readFile(fileNames.get(i));
  					processDocument(rawFile,fileNames.get(quantity*count+i));
//  					processDocument(rawFile,fileNames.get(i));
  					
  				//	System.out.println(_numDocs+" "+fileNames.get(quantity*count+i));
//  					System.out.println(_numDocs+" "+fileNames.get(i));
  				}				
  			}
  			String countString = String.format("%02d", count); 
 			String indexFile = _options._indexPrefix + "/compressed_"+countString+".idx";
//  			String indexFile = "index/compressed.idx";
  			String termCompressedMap = indexToString();			
  			try {
  				FileManager.saveFile(indexFile, termCompressedMap);
  			} catch (Exception e) {
  				e.printStackTrace();
  			}	
  			count++;
  			compressedIndex.clear();
 		}
  		try {
  			FileManager.saveFile2(_options._indexPrefix + "/Documents", documents);
  		} catch (Exception e) {
  			e.printStackTrace();
  		}		
  	}
  
  private String indexToString() {
		StringBuffer sb = new StringBuffer();
		for(String key :compressedIndex.keySet()) {
			sb.append(key).append(":");
			Vector<byte[]> entry = compressedIndex.get(key);
			for(byte[] bytesForANum : entry) {
				sb.append(transferByteValuesToHex(bytesForANum));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
  
  	private void processDocument(String[] rawFile, String filePath) {
  		String title = rawFile[0];  // Do not process right now.
		String content = rawFile[1];
		int count = 0;
		DocumentIndexed doc = new DocumentIndexed(_numDocs, this);
		doc.setTitle(title);
		doc.setUrl(filePath);
		Stemmer stemmer = new Stemmer();
		String[] contentArray = content.split(" ");
		for(int i = 0; i<contentArray.length; i++) {
			_totalTermFrequency++;			
			String term = contentArray[i];
			if(!term.equals("")) {
				count++;
				term = stemmer.stem(term);
				if (!termOccurrences.containsKey(term)) {
					termOccurrences.put(term, new ArrayList<Pair<Integer,Integer>>());
				}
				List<Pair<Integer,Integer>> occurrences = termOccurrences.get(term);
				Pair<Integer,Integer> pair = new Pair<Integer,Integer>(doc._docid, i+1);
				occurrences.add(pair);	
			}					
		}
		doc.setNumTerms(count);
		documents.put(_numDocs, doc);
		processOcurrences();
		encode();
		termOccurrences.clear();
		termOffset.clear();
	}
  	
  	private void processOcurrences() {  
  		for(String term: termOccurrences.keySet()) {
  			List<Pair<Integer,Integer>> occurrences = termOccurrences.get(term);
  			occurrences.add(new Pair<Integer,Integer>(-2,0));
  			int predocid = -1;
  			int preposition = -1;
  			List<Integer> positions = new ArrayList<Integer>();
  			List<Integer> tmpls = new ArrayList<Integer>();
  			for(int i = 0; i<occurrences.size(); i++) {
  				Pair<Integer,Integer> pair = occurrences.get(i); 
  				if(pair.getLeft() != predocid) {   // appear in a new doc
  					if(predocid !=-1) {
  						tmpls.add(predocid);
  	  					tmpls.add(positions.size());
  	  					tmpls.addAll(positions);  // record the docid, count, offsets
  					}  
  					if(pair.getLeft() != -2) {
  						positions.clear();
  	  					positions.add(pair.getRight());  	  					 
  	  					predocid = pair.getLeft();   // update the previous docid
  	  					preposition = pair.getRight();
  					}  					
  				}
  				else {
  					positions.add(pair.getRight() - preposition);  
  					preposition = pair.getRight();
  				}  				
  			}

  			if(!termOffset.containsKey(term)) {
  				termOffset.put(term, new ArrayList<Integer>());
  			}
  			List<Integer> tmp = termOffset.get(term);
  			tmp.addAll(tmpls);
  		}
  	}
  	
  	private void encode() {  		
  		for(String term: termOffset.keySet()) {
  			List<Integer> offsets = termOffset.get(term);
  			Vector<byte[]> byteCode = computeVByteCode(offsets);
  			if(!compressedIndex.containsKey(term)) {
  				compressedIndex.put(term, new Vector<byte[]>());
  			}
  			Vector<byte[]> tmp = compressedIndex.get(term);
  			tmp.addAll(byteCode);
  		}
  	}
  	
  //compute v byte binary code for(docid, count, offset1, offset2..) ==>101010
  	private Vector<byte[]> computeVByteCode(List<Integer> countAndOffsets) {
  		Vector<byte[]> byteCode = new Vector<byte[]>();
  		for(Integer i : countAndOffsets) {
 			
  			byteCode.add(this.toBytesNum(i));
  			
  		}
  		return byteCode;
  	}
  	
 // transfer input num to binary bits and split to Vector<Byte>
 	private byte[] toBytesNum (int num) {
 		byte[] bytes = new byte[]{};
 		// the number will uses how many bytes
 		int byteNum = 0;
 		if(num < Math.pow(2,7) && num > 0) {
 			byteNum = 1;
 		} else if (num < Math.pow(2,14) && num >= Math.pow(2,7)) {
 			byteNum = 2;
 		} else if(num < Math.pow(2,21) && num >= Math.pow(2,14)) {
 			byteNum = 3;
 		} else if(num < Math.pow(2,28) && num >= Math.pow(2,21)) {
 			byteNum = 4;
 		} else if (num < Math.pow(2,32) && num >= Math.pow(2,28)) {
 			 byteNum = 5;
 		} else {
 			System.out.println("error");
 		}
 		String binaryString = Integer.toBinaryString(num);
 		binaryString = this.toBinaryNum(num, byteNum * 7);
 		
 		// non-last byte
 		for(int i=0; i<byteNum -1; i++) {
 			String s = binaryString.substring(i*7, i*7+7);
 			bytes = ArrayUtils.add(bytes, Byte.valueOf(s, 2));
 		}
 		// last byte
 		String s = binaryString.substring((byteNum -1)*7);
 		bytes = ArrayUtils.add(bytes, (byte)-Byte.valueOf(s, 2));
 		return bytes;
 	}
 	
 // transfer input num to binary num filled to D bits
 	private String toBinaryNum(int num, int D) {
 //System.out.println("in toBinaryNum");
		if(num < 0) {
			num = -num;
		}
		String binaryString = Integer.toBinaryString(num);
		int length = binaryString.length();
		int moreBits = D - length;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < moreBits; i++) {
			sb.append(0);
		}
		String result = sb.append(binaryString).toString();
//System.out.println("binary " + result);
		return result;
	}

	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
		documents = FileManager.translateDocs(_options._indexPrefix + "/Documents", this);
		CorpusAnalyzerPagerank corpusAnalyzerPagerank = new CorpusAnalyzerPagerank(_options);
		corpusAnalyzerPagerank.load();
		Map<String,Double> pageRank = corpusAnalyzerPagerank.pageRank;
		LogMinerNumviews logN = new LogMinerNumviews(_options);
		logN.load();
		Map<String,Integer> numviews = logN.numviews;
		for(Document d : documents.values()) {
			d.setPageRank(pageRank.get(d.getUrl()) == null? 0 : pageRank.get(d.getUrl()));
			d.setNumViews(numviews.get(d.getUrl()) == null? 0 : numviews.get(d.getUrl()));
		}
		this._numDocs = documents.size();
		for(DocumentIndexed d : documents.values()){
			this._totalTermFrequency += d.getNumTerms(); 
		}
		
	}

	@Override
	public Document getDoc(int docid) {
		return null;
	}
	
	private String transferByteValuesToHex(byte[] bytes) {
		int count = 0;
		StringBuffer sbForHex = new StringBuffer();
		for(byte b : bytes) {
			StringBuffer sb = new StringBuffer();
			count ++;
			if(count == bytes.length) { // last byte
				sb.append(1);
			} else {
				sb.append(0);
			}
			sb.append(toBinaryNum(b,7));
			String  byteBinary = sb.toString();
			for(int i=0; i<2; i++) {
				String s = byteBinary.substring(i*4, i*4+4);
				sbForHex.append(transferStringToHex(s));
			}
			sbForHex.append(" ");
		}
		return sbForHex.toString();
	}
	
	private String transferStringToHex(String s) {
		int temNum = 0;
		String hex = "";
		for(int i=0; i<4; i++) {
			if(s.charAt(i) == '1') {
				temNum = temNum *2 + 1;
			} else {
				temNum = temNum  * 2;
			}
		}
		
		switch(temNum) {
			case 10 : hex = "A"; break;
			case 11 : hex = "B"; break;
			case 12 : hex = "C"; break;
			case 13 : hex = "D"; break;
			case 14 : hex = "E"; break;
			case 15 : hex = "F"; break;
			default : hex = temNum + ""; break;
		}
		return hex;
	}

	/**
	 * In HW2, you should be using {@link DocumentIndexed}.
	 */
	@Override
	public DocumentIndexed nextDoc(Query query, int docid) {
		Vector<String> tokens = query._tokens;
		Vector<String> singleTerms = new Vector<String>();
		Vector<String> phrases = new Vector<String>();
		for(String s: tokens) {
			if(s.contains(" ")) {
				phrases.add(s.trim());
			}
			else {
				singleTerms.add(s.trim());
			}
		}
		int nextDocid1 = 0;
		List<Integer> docids = new ArrayList<Integer>();
		if(singleTerms.size()>0) {
			if(singleTerms.size() == 1) {
				nextDocid1 = nextTerm(singleTerms.get(0), docid);
			}
			else {
				
				List<String> terms = new ArrayList<String>();
				terms.addAll(singleTerms);
				nextDocid1 = nextTermls(terms, docid);
			}
			
			
			docids.add(nextDocid1);
			if(phrases.size()==0) {
				return documents.get(nextDocid1);
			}
		}
		else {
			for(String s: phrases) {
				String[] phraseArray = s.split(" ");
				List<String> termsInPhrase = new ArrayList<String>();
				for(String ss: phraseArray) {
					String term = stemmer.stem(ss.trim());
					termsInPhrase.add(term);
				}
				int nextDocid = nextPhrase(termsInPhrase, docid); 
				System.out.println("for phrase add "+ nextDocid);
				if(nextDocid == Integer.MAX_VALUE) {
					return null;
				}
				docids.add(nextDocid);
			}
		}
		
		if(isEqualArray(docids)) {
			return documents.get(docids.get(0));
		}
		Collections.sort(docids);
		int newDocid = docids.get(docids.size()-1);	
		System.out.println(newDocid);
		return nextDoc(query, newDocid-1);
	}
	
	// check if all the elements in an array equals each other
	private boolean isEqualArray(List<Integer> docids) {
		for(int i = 1; i<docids.size(); i++) {
			if(docids.get(i) != docids.get(i-1)) {
				return false;
			}
		}
		return true;
	}
	
	// get the next docid with a specific term
	private int nextTerm(String term, int docid) {		
		term = stemmer.stem(term);
		if(compressedIndex.containsKey(term)) {
			if(compressedIndex.get(term).size()>0) {
				Vector<byte[]> termls = compressedIndex.get(term);
				int currentDocid = byteToInt(termls.get(0));
				int currentDocidPos = 0;
				if( currentDocid>docid) {
					return currentDocid;
				}
				while(currentDocidPos < termls.size()) {			
					currentDocid = byteToInt(termls.get(currentDocidPos));
					if(currentDocid > docid) {
						return currentDocid;
					}
					currentDocidPos = currentDocidPos + 2 + byteToInt(termls.get(currentDocidPos+1));
				}
			}			
		}		
		return Integer.MAX_VALUE;
	}
	
	private int byteToInt(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< bytes.length; i++) {
			byte b = bytes[i];
			if(b < 0){
				b = (byte)-b;
			}
			sb.append(this.toBinaryNum(b,7));
		}

		String binary = sb.toString();
		int intValue = Integer.valueOf(binary, 2);
		return intValue;

	}

	private int nextPhrase(List<String> termsInPhrase, int docid) {
		Map<String,ArrayList<Pair<Integer,Integer>>> occu = transOffset(compressedIndex);
		int nextDoc = nextTermls(termsInPhrase, docid);
		if(nextDoc == Integer.MAX_VALUE) {
			return nextDoc;
		}
		String firstTerm = termsInPhrase.get(0);
		ArrayList<Pair<Integer,Integer>> firstList = occu.get(firstTerm);
		List<int[]> expectedPositions = new ArrayList<int[]>();
		for(Pair<Integer,Integer> pair: firstList) {
			if(pair.getLeft() == nextDoc) {
				int[] expectedPosition = new int[termsInPhrase.size()];
				expectedPosition[0] = pair.getRight();
				for(int i = 1; i<termsInPhrase.size(); i++) {
					expectedPosition[i] = expectedPosition[i-1] + 1;
				}
				expectedPositions.add(expectedPosition);
			}
		}
		
		for(int[] expectedPosition: expectedPositions) {
			int count = 1;
			for(int i = 1; i<termsInPhrase.size(); i++) {
				ArrayList<Pair<Integer,Integer>> pairList = occu.get(termsInPhrase.get(i));
				for(Pair<Integer,Integer> pair: pairList) {
					if(nextDoc == 68){
					printArray(expectedPosition);
					}
					if(pair.getLeft() == nextDoc && pair.getRight() == expectedPosition[i]) {
						printArray(expectedPosition);
						count++;
						break;
					}
				}
			}
			if(count == termsInPhrase.size()) {
				return nextDoc;
			}
		}
		return nextPhrase(termsInPhrase, nextDoc);
	}
	
	private void printArray(int[] expectedPosition) {	
		for(int i: expectedPosition) {
			System.out.print(i+",");
		}
	}
	private int nextTermls(List<String> termsInPhrase, int docid) {
		List<Integer> docids = new ArrayList<Integer>();
		for(String term: termsInPhrase) {
			int tmpId = nextTerm(term, docid);
			if(tmpId == Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			}
			docids.add(tmpId);
		}
		if(equalList(docids)) {
			return docids.get(0);
		}
		Collections.sort(docids);
		int newId = docids.get(docids.size()-1);
		return nextTermls(termsInPhrase, newId);
	}
	
	private boolean equalList(List<Integer> docids) {
		if(docids.size() == 0) {
			return false;
		}
		if(docids.size() == 1) {
			return true;
		}
		else {
			for(int i = 1; i<docids.size(); i++) {
				if(docids.get(i) != docids.get(0)) {
					return false;
				}
			}
			return true;
		}		
	}

	private Map<String,ArrayList<Pair<Integer,Integer>>> transOffset(HashMap<String,Vector<byte[]>> byteOffsets) {
		Map<String,ArrayList<Pair<Integer,Integer>>> occu = new HashMap<String,ArrayList<Pair<Integer,Integer>>>();
		for (String term: byteOffsets.keySet()) {
			ArrayList<Pair<Integer,Integer>> pairs = new ArrayList<Pair<Integer,Integer>>();
			Vector<byte[]> boff = byteOffsets.get(term); // docid num off1 off2 off..docid num off
			int docidPos = 0;
			int docid = 0;
			while(docidPos < boff.size()) {
				docid = byteToInt(boff.get(docidPos));
				int num = byteToInt(boff.get(docidPos+1));
				List<byte[]> offsets = new ArrayList<byte[]>();
				if(docidPos + 2 + num <boff.size()) {
					offsets= boff.subList(docidPos+2, docidPos + 2 + num);
				}
				else {
					offsets = boff.subList(docidPos+2, boff.size());
					docidPos = docidPos + 2 + num;
					continue;
				}
				ArrayList<Pair<Integer,Integer>> pairsInOneDoc = new ArrayList<Pair<Integer,Integer>>();
				int basePos = byteToInt(offsets.get(0));
				pairsInOneDoc.add(new Pair<Integer, Integer>(docid,basePos));
				for(int i = 1; i<offsets.size(); i++) {
					byte[] offset = offsets.get(i);
					int tmp = pairsInOneDoc.get(i-1).getRight();
					int intOffset = byteToInt(offset) + tmp;
					pairsInOneDoc.add(new Pair<Integer, Integer>(docid,intOffset));
				}
				pairs.addAll(pairsInOneDoc);
				docidPos = docidPos + 2 + num;
			}
			occu.put(term, pairs);
		}
		return occu;
	}
	
	
	// check if an array is serial, like {2,3,4}
	private boolean isSerialArray(int[] position) {
		int firstPosition = position[0];
		for(int i = 0; i<position.length; i++) {
			if(position[i] != firstPosition+i) {
				return false;
			}
		}
		return true;
	}
	
	public void loadCompressedIndex(Query query) {
		String _query = query._query.trim().toLowerCase();		
		_query = FileManager.cleanString(_query);		
		_query = FileManager.deleteMultiSpace(_query);

		String[] tokens = _query.split(" ");
		List<String> tokenls = new ArrayList<String>();
		for(String s: tokens) {
			if(s != "") {
				tokenls.add(s);
			}
		}
		String folderPath = "data/index";
		List<String> fileNames = FileManager.getFileNames(folderPath);
		
		for(String term: tokenls) {
			term = stemmer.stem(term);
			for(String fileName: fileNames) {
				if(!fileName.equals("Documents") && fileName.contains("compressed")) {
					String filePath = _options._indexPrefix + "/" + fileName;
					Map<String, Vector<byte[]>> tmpMap = this.extractCompress(term, filePath);
					if (!compressedIndex.containsKey(term)) {
						compressedIndex.put(term, new Vector<byte[]>());
					}
					Vector<byte[]> compress = compressedIndex.get(term);
					if(tmpMap.containsKey(term)){
						compress.addAll(tmpMap.get(term));
					}
					System.out.println("Load index from: " + fileName);
				} 
			}
		}			
	}
	
	private Map<String,Vector<byte[]>> extractCompress(String targetTerm, String filePath) {
		Map<String,Vector<byte[]>> compressedIndex = new HashMap<String,Vector<byte[]>>();
		try {
			BufferedReader inputf = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = inputf.readLine()) != null) {				
				line = line.trim();
				String[] lineArray = line.split(":");
				String term = lineArray[0];
				String entry = lineArray[1];
				if(term.equals(targetTerm)) {
					this.loadAnIndexEntry(line,compressedIndex);
					break;
				}
			}
			inputf.close();
		} catch (IOException io) {
		}
		return compressedIndex;
	}
	
	private void loadAnIndexEntry(String entry,Map<String,Vector<byte[]>> compressedIndex) {
		Vector<byte[]> indexByteValues = new Vector<byte[]>();
		
		String[] indexEntry = entry.trim().split(":");
		String term = indexEntry[0].trim();
		String hexCode = indexEntry[1];
		byte temp;
		Vector<Byte> tempBytes = new Vector<Byte>();
		
		Scanner scanner = new Scanner(hexCode);
		while(scanner.hasNext()) {
			String hex = scanner.next();
//			System.out.println("hex " + hex);
			temp = transferHexToByteValue(hex);
//			System.out.println("temp " + temp);
			if(temp > 0) { // not the last one
				tempBytes.add(temp);
			} else if(temp < 0) {
				tempBytes.add(temp);
				int byteArrayLength = tempBytes.size();
				byte[] bArray = new byte[byteArrayLength];
				int i=0;
				for(Byte b : tempBytes) {
//					System.out.println("b " + b);	
					bArray[i] = b;
//					System.out.println("i " +i + " " +bArray[i]);
					i++;
				}
				indexByteValues.add(bArray);
				tempBytes.clear();
			}
		}
		compressedIndex.put(term,indexByteValues);
	}
	
	private byte transferHexToByteValue(String hexCode) {
		StringBuffer sb = new StringBuffer();
		byte byteValue;
		char num1 = hexCode.charAt(0);
		char num2 = hexCode.charAt(1);
		sb.append(matchBinary(num1));
		sb.append(matchBinary(num2));
		String byteBits = sb.toString();
		String last7Bits = byteBits.substring(1);
		
		if(byteBits.charAt(0) == '1') {
			byteValue = (byte)-Byte.valueOf(last7Bits, 2);
		} else {
			byteValue = Byte.valueOf(last7Bits, 2);
		}
		return byteValue;
	}
	
	private String matchBinary(char hex){
		String binary = "";
		switch(hex) {
			case '0': binary = "0000"; break;
			case '1': binary = "0001"; break;
			case '2': binary = "0010"; break;
			case '3': binary = "0011"; break;
			case '4': binary = "0100"; break;
			case '5': binary = "0101"; break;
			case '6': binary = "0110"; break;
			case '7': binary = "0111"; break;
			case '8': binary = "1000"; break;
			case '9': binary = "1001"; break;
			case 'A': binary = "1010"; break;
			case 'B': binary = "1011"; break;
			case 'C': binary = "1100"; break;
			case 'D': binary = "1101"; break;
			case 'E': binary = "1110"; break;
			case 'F': binary = "1111"; break;
		}
		return binary;
	}



	@Override
	// Number of documents in which {@code term} appeared, over the full corpus.
	public int corpusDocFrequencyByTerm(String term) {
//		loadCompressedIndex(new Query(term));
		int count = 0;
		if(compressedIndex.containsKey(term)) {
			Vector<byte[]> termls = compressedIndex.get(term);
			int currentDocidPos = 0;			
			while(currentDocidPos<termls.size()) {			
				count++;
				currentDocidPos = currentDocidPos + 2 + byteToInt(termls.get(currentDocidPos+1));
			}
		}	
		return count;
	}

	@Override
	// Number of times {@code term} appeared in corpus. 
	public int corpusTermFrequency(String term) {
//		loadCompressedIndex(new Query(term));
		int count = 0;
		if(compressedIndex.containsKey(term)) {
			Vector<byte[]> termls = compressedIndex.get(term);
			int currentCountPos = 1;			
			while(currentCountPos<termls.size()) {			
				count += byteToInt(termls.get(currentCountPos));
				currentCountPos = currentCountPos + 2 + byteToInt(termls.get(currentCountPos));
			}
		}
		return count;
	}

	/**
	 * @CS2580: Implement this for bonus points.
	 */
	@Override
	// Number of times {@code term} appeared in the document {@code url}.
	public int documentTermFrequency(String term, String url) {
//		loadCompressedIndex(new Query(term));
		term = new Stemmer().stem(term);
		int docid = -1;
		for(Document doc: documents.values()) {
			if(doc.getUrl().equals(url)) {
				docid = doc._docid;
				break;
			}
		}
		if(docid == -1) {
			try {
				throw new Exception("No such document exists.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		int count = 0;
		if(compressedIndex.containsKey(term)) {
			Vector<byte[]> termls = compressedIndex.get(term);
			int currentDocidPos = 0;			
			while(currentDocidPos<termls.size()) {					
				if(byteToInt(termls.get(currentDocidPos)) == docid) {
					count = byteToInt(termls.get(currentDocidPos+1));
					break;
				}
				currentDocidPos = currentDocidPos + 2 + byteToInt(termls.get(currentDocidPos+1));
			}
		}
		return count;
	}

	
	public int documentPhraseFrequency(String phrase, String url) {
//		String targetPhrase = "\"" + phrase + "\"";
	//	loadCompressedIndex(new Query(targetPhrase));
		int docid = -1;
		for(Document doc: documents.values()) {
			if(doc.getUrl().equals(url)) {
				docid = doc._docid;
				break;
			}
		}
		if(docid == -1) {
			try {
				throw new Exception("No such document exists.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String[] phraseArray = phrase.trim().split(" ");
		List<String> termsInPhrase = new ArrayList<String>();
		for(String ss: phraseArray) {
			String term = stemmer.stem(ss.trim());
			if(!compressedIndex.containsKey(term)) {  // the index contains no such phrase
				return 0;
			}
//			if(documentTermFrequency(ss, url) < 1) {   // if the documents does not contain this phrase
//				return 0;
//			}			
			termsInPhrase.add(term);
		}
		
		HashMap<String,ArrayList<Integer>> termAt = new HashMap<String, ArrayList<Integer>>();
		for(String term: termsInPhrase) {
			Vector<byte[]> termls = compressedIndex.get(term);	
			int currentDocid = byteToInt(termls.get(0));
			int currentDocidPos = 0;
			int base = 0;
			List<byte[]> offsets = new ArrayList<byte[]>();
			while(currentDocidPos < termls.size()) {			
				currentDocid = byteToInt(termls.get(currentDocidPos));
				if(currentDocid == docid) {
					offsets = termls.subList(currentDocidPos+2, currentDocidPos + 2 + byteToInt(termls.get(currentDocidPos+1)));

					base = byteToInt(offsets.get(0));
					break;
				}
				currentDocidPos = currentDocidPos + 2 + byteToInt(termls.get(currentDocidPos+1));
			}
			ArrayList<Integer> positions = new ArrayList<Integer>();
			int posit = base;
			positions.add(base);
			for(int i = 1; i<offsets.size(); i++) {
				posit  += byteToInt(offsets.get(i));
				positions.add(posit);
			}
			termAt.put(term, positions);
		}
		List<Integer> firstTermPosit = termAt.get(termsInPhrase.get(0));
		List<ArrayList<Integer>> expectTermPosit = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i<firstTermPosit.size(); i++) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			tmp.add(firstTermPosit.get(i));
			for(int j = 1; j<termsInPhrase.size(); j++) {
				tmp.add(firstTermPosit.get(i) + j);
			}
			expectTermPosit.add(tmp);
		}
		int count = firstTermPosit.size();
		for(ArrayList<Integer> tmp: expectTermPosit) {
			for(int k = 1; k<tmp.size(); k++) {
				if (!termAt.get(termsInPhrase.get(k)).contains(tmp.get(k))) {
					count--;
				}
			}
		}		
		return count;
	}
}

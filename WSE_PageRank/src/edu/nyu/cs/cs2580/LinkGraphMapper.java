package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

/**
 * LinkGraphMapper used to generate Link Graph.
 * @author Yubing & Qian
 */
public class LinkGraphMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
        private Text pageName = new Text();
        private Text linkName = new Text();     
        
        private List<String> getLinksList(String line) {
        	List<String> linkslist = new ArrayList<String>();
                
        	Pattern patternOut1 = Pattern.compile("<[a|A].*?href=\"([^ /#]*)\".*?>");
        	Matcher matcherOut1 = patternOut1.matcher(line);
        	while(matcherOut1.find()) {
        		String link = matcherOut1.group(1);
        		if(link != null) {
        			linkslist.add(link);
        		}
        	} 
        	return linkslist;
        }
        
        private String getPageName(String line) {
        	String name = "";
    		Pattern patternName = Pattern.compile("<title>(.*?)</title>");
    		Matcher matcherName = patternName.matcher(line);
    		if(matcherName.find()) {
    			name = matcherName.group(1);	
    			if(name != null && name.contains("<")){
        			int index = name.indexOf("<");
        			name = name.substring(0, index);
        		}
    		}
            return name;
        }
        
        /**
         * input key: offset of the file
         * input value: a line of text
         * output key: current page name
         * output value: out link
         */
        @Override
        public void map(LongWritable key, Text value,
        		OutputCollector<Text, Text> output, Reporter reporter) throws IOException {            
        	String line = value.toString();
                
        	String outputKey = getPageName(line);            
        	pageName.set(outputKey);
                
        	List<String> linkslist = getLinksList(line);
        	for (int i = 0; i < linkslist.size(); i++) {
        		linkName.set(linkslist.get(i));
        		output.collect(pageName, linkName);
        	}
        }
}
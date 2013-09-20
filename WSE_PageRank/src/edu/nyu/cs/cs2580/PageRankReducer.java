package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

/**
 * Page Rank Reducer
 * @author Yubing & Qian
 */
public class PageRankReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
	private double dampFactor; 
	private Text outputValue = new Text();
    private String separator = "\t";
    
    public void configure(JobConf job) {
    	dampFactor = Double.valueOf(job.get("pagerank.dampfactor"));
    }
        
    /**
     * input key: page name
     * input value: PageRank, outlink1, outlink2, ..., outlinkN 
     * output key: page name
     * output value: new PageRank, outlink1, outlink2, ..., outlinkN 
     */
    @Override
    public void reduce(Text key, Iterator<Text> values,
    		OutputCollector<Text, Text> output, Reporter reporter) throws IOException { 
    	double sum = 0;
    	String outlinks = "";
                
    	while (values.hasNext()) {
    		String temp = values.next().toString();
            if (temp.indexOf("\t\t") == 0) {
            	// it has the outlinks
            	outlinks = temp.substring(temp.indexOf("\t\t")+1, temp.length());
            } else {
            	// it has the inlink
            	String[] array = temp.split("\t");
            	sum += Double.valueOf(array[0])/Double.valueOf(array[2]);
            }
    	}
                
    	sum = dampFactor * sum + (1 - dampFactor);
    	outputValue.set(String.valueOf(sum) + separator + outlinks);
        output.collect(key, outputValue);
    }
}


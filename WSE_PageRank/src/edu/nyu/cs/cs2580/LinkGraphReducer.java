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
 * @author Yubing & Qian
 */
public class LinkGraphReducer extends MapReduceBase implements
        Reducer<Text, Text, Text, Text> {
        private double dampFactor; 
        private Text outputValue = new Text();
        private String separator = "\t";
        
        public void configure(JobConf job) {
        	dampFactor = Double.valueOf(job.get("pagerank.dampfactor"));
        }
        
        /**
         * input key: page name
         * input value: out link
         * output key: page name
         * output value: initial pageRank, outlink1, outlink2, ..., outlinkN 
         */
        @Override
        public void reduce(Text key, Iterator<Text> values,
        		OutputCollector<Text, Text> output, Reporter reporter) throws IOException { 
        	StringBuilder sb = new StringBuilder();
        	sb.append(String.valueOf(1-dampFactor) + separator);
        	while (values.hasNext()) {
        		sb.append(values.next().toString() + separator);
        	}    
        	outputValue.set(sb.toString());
        	output.collect(key, outputValue);
        }
}


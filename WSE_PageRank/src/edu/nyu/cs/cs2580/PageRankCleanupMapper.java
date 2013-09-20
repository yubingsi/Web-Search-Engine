package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;

/**
 * Generate the first fifty high page rank value pairs (page rank, page name).
 * And delete the intermediate output files.
 * @author Yubing & Qian
 */
public class PageRankCleanupMapper {
	
	/**
	 * Cleanup Mapper
	 */
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, DoubleWritable, Text>{
		private String[] getLinksList(String line) {
			return line.split("\t");
		}
    
		/**
		 * input key: offset
         * input value: PageRank, outlink1, ..., outlinkN
         * output key: page rank value
         * output value: page name
		 */
		public void map(LongWritable key, Text value,OutputCollector<DoubleWritable, Text> output, Reporter reporter) 
				throws IOException {
	    	String line = value.toString();
	        int separateIndex = line.indexOf("\t");
	        String current_link = line.substring(0, separateIndex);
	        String rest = line.substring(separateIndex, line.length()); // get the rest of the line
	        String[] valuesList = getLinksList(rest); // the array contains the PageRank and the outlinks
	        String pagerank = valuesList[1]; // the first element is empty
	        output.collect(new DoubleWritable(Double.parseDouble(pagerank)*10000), new Text(current_link));   
		}
    }
    
	/**
	 * Clean up Reducer
	 */
    public static class Reduce extends MapReduceBase implements Reducer<DoubleWritable, Text,DoubleWritable, Text> {
    	private int num = 0;
    	
    	 /**
    	  * input key: page rank value
    	  * input value: page name
    	  * output key: page rank value
    	  * output value: page name
    	  */
	      public void reduce(DoubleWritable pagerank,  Iterator<Text> values, OutputCollector<DoubleWritable, Text> output, Reporter reporter) throws IOException {
	    	  while(values.hasNext() && num<50) {
	    		  Text text = values.next();
	    		  Text empty = new Text();
	    		  empty.set("");
	    		  if(!text.equals(empty)){
	    			  output.collect(pagerank, text);
	    			  num++;
	    		  }
	    	  }
	      }
    }
    
    /**
     * Set configuration.
     * Delete intermediate files.
     * @param arg output path
     * @throws IOException
     */
    public static void cleanupMain(String arg) throws IOException {
    	JobConf conf = new JobConf(PageRankCleanupMapper.class);
	    conf.setJobName("pagerankcleanup");
	
	    conf.setOutputKeyClass(DoubleWritable.class);
	    conf.setOutputValueClass(Text.class);
	
	    conf.setMapperClass(Map.class);
	    conf.setCombinerClass(Reduce.class);
	    conf.setReducerClass(Reduce.class);
	
	    conf.setOutputFormat(TextOutputFormat.class);
	    conf.setOutputKeyComparatorClass(LongWritable.DecreasingComparator.class);
	
	    FileInputFormat.setInputPaths(conf, new Path("temp4"));
	    FileOutputFormat.setOutputPath(conf, new Path(arg));
	    JobClient.runJob(conf);  
	    for(int i=0; i<=4; i++) {
	    	File dir = new File("temp" + i);
	    	deleteDir(dir);
	    }
	    File linkgraph = new File("linkGraph");
	    deleteDir(linkgraph);
    }
    
    /**
     * Delete file and directory.
     * @param dir the will deleted file.
     * @throws IOException
     */
    public static void deleteDir(File dir) throws IOException {
        if (!dir.isDirectory()) {
                return ;
        }

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
        	File file = files[i];
            if (file.isDirectory()) {
            	deleteDir(file);
            } else {
                boolean deleted = file.delete();
                if (!deleted) {
                	throw new IOException("Unable to delete file" + file);
                }
            }
        }
        dir.delete();
    }
}

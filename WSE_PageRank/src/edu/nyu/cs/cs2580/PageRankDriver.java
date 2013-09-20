package edu.nyu.cs.cs2580;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

public class PageRankDriver {

	/**
	 * Set configuration for page rank.
	 * Calculate PageRank for 5 iterations
	 * @throws IOException
	 */
	public static void pageRankmain() throws IOException {
		JobClient client = new JobClient();

		JobConf conf = new JobConf(PageRankDriver.class);
		conf.setMapperClass(PageRankMapper.class);
		conf.setReducerClass(PageRankReducer.class);
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		//conf.set("io.sort.mb", "10");
		conf.set("pagerank.dampfactor", String.valueOf(0.85));
		FileInputFormat.addInputPath(conf, new Path("linkGraph"));
		FileOutputFormat.setOutputPath(conf, new Path("temp0"));
                
		client.setConf(conf);
		JobClient.runJob(conf);
                
		for(int i=0; i<5-1;i++){
			conf = new JobConf(PageRankDriver.class);
            conf.setMapperClass(PageRankMapper.class);
            conf.setReducerClass(PageRankReducer.class);
            conf.setOutputKeyClass(Text.class);
            conf.setOutputValueClass(Text.class);
            conf.set("pagerank.dampfactor", String.valueOf(0.85));

            FileInputFormat.addInputPath(conf, new Path("temp"+ i));
            FileOutputFormat.setOutputPath(conf, new Path("temp"+(i+1)));
            client.setConf(conf);
            JobClient.runJob(conf);
       }
    }
}

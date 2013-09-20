package edu.nyu.cs.cs2580;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

/**
 * Driver for LinkGraphMapper and LinkGraphReducer.
 * @author Yubing & Qian
 */
public class LinkGraphDriver {

        /**
         * Set job configuration.
         * @param args
         * @throws IOException 
         */
        public static void linkGraphmain(String arg) throws IOException {
                JobClient client = new JobClient();
                JobConf conf = new JobConf(LinkGraphDriver.class);
                conf.setMapperClass(LinkGraphMapper.class);
                conf.setReducerClass(LinkGraphReducer.class);
                conf.setOutputKeyClass(Text.class);
                conf.setOutputValueClass(Text.class);
                //conf.set("io.sort.mb", "10");
                conf.set("pagerank.dampfactor", String.valueOf(0.85));
                FileInputFormat.addInputPath(conf, new Path(arg));
                FileOutputFormat.setOutputPath(conf, new Path("linkGraph"));
                client.setConf(conf);
                JobClient.runJob(conf);
        }
}


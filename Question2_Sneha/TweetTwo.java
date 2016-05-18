import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.json.*;
//import java.io.*;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.HashMap;


public class TweetTwo {

  //public static class FreqMap extends Mapper<LongWritable, Text, Text, LongWritable> {
  public static class TweetClass extends Mapper<Object, Text, Text, LongWritable>
  {

    public void map(Object key, Text value, Context context)
        throws IOException, InterruptedException 
    	{
    String[] tweets = value.toString().split("\\n"); 
      for( String tweet : tweets){
    	  JSONObject obj=new JSONObject(tweet);
    	  String scrname = obj.getJSONObject("user").getString("screen_name"); 
    	  //String name = obj.getJSONObject("user").getString("name");
    	  if((scrname.equalsIgnoreCase("PrezOno")))
  	    {
  	    	String date =obj.getString("created_at");
  	      String subs = date.substring(0, 3); 
        context.write(new Text(subs), new LongWritable(1));
  	    }
    }
    }
  }

  public static class FreqReduce extends Reducer<Text, LongWritable, Text, LongWritable> {

    public void reduce(Text key, Iterable<LongWritable> values, Context context)
        throws IOException, InterruptedException {
      
      long sum = 0;
      for (LongWritable value : values) {
        sum = sum + value.get();
      }
      context.write(key, new LongWritable(sum));
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Usage: TweetTwo <input path> <output path>");
      System.exit(-1);
    }
    
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Tweet count");
    job.setJarByClass(TweetTwo.class);
    job.setJobName("TweetTwo");
    job.setNumReduceTasks(3);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    job.setMapperClass(TweetClass.class);
    job.setReducerClass(FreqReduce.class);
    job.setCombinerClass(FreqReduce.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

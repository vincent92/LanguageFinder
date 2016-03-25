package nl.hu.hadoop.languagefinder;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

public class LanguageFinder {

	public static void main(String[] args) throws Exception {
		Job job = new Job();
		job.setJarByClass(LanguageFinder.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.setMapperClass(PredictorMapper.class);
		job.setReducerClass(PredictorReducer.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// job.setOutputValueClass(IntWritable.class);

		job.waitForCompletion(true);
	}
}

class PredictorMapper extends Mapper<LongWritable, Text, Text, Text> {
	public void map(LongWritable Key, Text value, Context context) throws IOException, InterruptedException {

	    //convert line to clear and readable characters
        String line = normalizeSentence(value);
        
        // retrieve the words from the line
        String[] words = line.split("\\s");

        for (String word : words) {
            context.write(new Text(line), new Text(word));
        }
	}

	private String normalizeSentence(Text value) {
		String line = value.toString().trim().replaceAll(" +", " ");
        line = Normalizer.normalize(line, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        line = line.toLowerCase().replaceAll("[^A-Za-z ]", "").trim().replaceAll(" +", " ");
		return line;
	}
}

class PredictorReducer extends Reducer<Text, Text, Text, Text> {

	final static String ENGLISH_DICTIONARY = "/home/vincent/hadoop/hadoop-2.7.2/english_dictionary";
	private LanguagePredictor predictor = new LanguagePredictor(new Dictionary(ENGLISH_DICTIONARY));

	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

		int wordsSize = 0;
		double sumWordsPrediction = 0;

		Iterator<Text> words = values.iterator();
		while (words.hasNext()) {
			String word = words.next().toString();
			
			double prediction = predictor.calculatePrediction(word);
			
			sumWordsPrediction += prediction;
			wordsSize++;
		}
		
		double sentencePrediction = (sumWordsPrediction / wordsSize);
		
		//round of to two decimals behind comma
		sentencePrediction = (double) Math.round(sentencePrediction * 100) / 100;

        context.write(key, new Text(sentencePrediction + "%"));
	}

}

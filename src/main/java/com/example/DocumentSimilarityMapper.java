package com.example.controller;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

public class DocumentSimilarityMapper extends Mapper<LongWritable, Text, Text, Text> {

    private Text documentID = new Text();
    private Text wordText = new Text();

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.isEmpty()) return;

        String docID = ((FileSplit) context.getInputSplit()).getPath().getName();
        documentID.set(docID);

        HashSet<String> words = new HashSet<>();
        StringTokenizer tokenizer = new StringTokenizer(line);

        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase().replaceAll("[^a-zA-Z]", ""); // Remove punctuation
            if (!word.isEmpty()) {
                words.add(word);
            }
        }

        for (String word : words) {
            wordText.set(word);
            context.write(wordText, documentID);
        }
    }
}


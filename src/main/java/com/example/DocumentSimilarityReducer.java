package com.example.controller;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, Text> {

    private HashMap<String, HashSet<String>> documentWords = new HashMap<>();

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        List<String> docList = new ArrayList<>();

        for (Text docID : values) {
            String doc = docID.toString();
            if (!docList.contains(doc)) {
                docList.add(doc);
            }
        }

        for (String doc : docList) {
            documentWords.putIfAbsent(doc, new HashSet<>());
            documentWords.get(doc).add(key.toString());
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        List<String> docs = new ArrayList<>(documentWords.keySet());

        for (int i = 0; i < docs.size(); i++) {
            for (int j = i + 1; j < docs.size(); j++) {
                String doc1 = docs.get(i);
                String doc2 = docs.get(j);

                double similarity = computeJaccardSimilarity(doc1, doc2);
                if (similarity > 0.0) {
                    String pair = doc1 + ", " + doc2;
                    context.write(new Text(pair), new Text(String.format("%.2f", similarity)));
                }
            }
        }
    }

    private double computeJaccardSimilarity(String doc1, String doc2) {
        HashSet<String> set1 = documentWords.get(doc1);
        HashSet<String> set2 = documentWords.get(doc2);

        HashSet<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        HashSet<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
}

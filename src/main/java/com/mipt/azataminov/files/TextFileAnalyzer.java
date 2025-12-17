package com.mipt.azataminov.files;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TextFileAnalyzer {

    public static class AnalysisResult {
        private final long lineCount;
        private final long wordCount;
        private final long charCount;
        private final Map<Character, Integer> charFrequency;

        public AnalysisResult(long lineCount, long wordCount, long charCount, Map<Character, Integer> charFrequency) {
            this.lineCount = lineCount;
            this.wordCount = wordCount;
            this.charCount = charCount;
            this.charFrequency = new HashMap<>(charFrequency);
        }

        public long getLineCount() { return lineCount; }
        public long getWordCount() { return wordCount; }
        public long getCharCount() { return charCount; }
        public Map<Character, Integer> getCharFrequency() { return new HashMap<>(charFrequency); }

        @Override
        public String toString() {
            return String.format("Lines: %d, Words: %d, Characters: %d", lineCount, wordCount, charCount);
        }
    }

    public AnalysisResult analyzeFile(String filePath) throws IOException {
        long lineCount = 0;
        long wordCount = 0;
        long charCount = 0;
        Map<Character, Integer> charFrequency = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                charCount += line.length();

                String[] words = line.split("\\s+");
                for (String word : words) {
                    if (!word.trim().isEmpty()) {
                        wordCount++;
                    }
                }

                for (char c : line.toCharArray()) {
                    charFrequency.put(c, charFrequency.getOrDefault(c, 0) + 1);
                }
            }
        }

        return new AnalysisResult(lineCount, wordCount, charCount, charFrequency);
    }

    public void saveAnalysisResult(AnalysisResult result, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("File Analysis Result\n");
            writer.write("====================\n");
            writer.write("Total lines: " + result.getLineCount() + "\n");
            writer.write("Total words: " + result.getWordCount() + "\n");
            writer.write("Total characters: " + result.getCharCount() + "\n");
            writer.write("\nCharacter Frequency:\n");

            for (Map.Entry<Character, Integer> entry : result.getCharFrequency().entrySet()) {
                writer.write(String.format("'%c': %d\n", entry.getKey(), entry.getValue()));
            }
        }
    }
}
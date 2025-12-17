package com.mipt.azataminov.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

class TextFileAnalyzerTest {

    @Test
    void testAnalyzeFile() throws IOException {
        TextFileAnalyzer analyzer = new TextFileAnalyzer();

        Path testFile = Files.createTempFile("test", ".txt");
        List<String> content = Arrays.asList("Hello world!", "This is test.");
        Files.write(testFile, content);

        TextFileAnalyzer.AnalysisResult result = analyzer.analyzeFile(testFile.toString());

        assertEquals(2, result.getLineCount());
        assertEquals(5, result.getWordCount());
        assertEquals(27, result.getCharCount());
        assertTrue(result.getCharFrequency().containsKey('H'));
    }

    @Test
    void testSaveAnalysisResult() throws IOException {
        TextFileAnalyzer analyzer = new TextFileAnalyzer();

        TextFileAnalyzer.AnalysisResult result = new TextFileAnalyzer.AnalysisResult(2, 5, 27, new java.util.HashMap<>());

        Path outputFile = Files.createTempFile("analysis", ".txt");
        analyzer.saveAnalysisResult(result, outputFile.toString());

        assertTrue(Files.size(outputFile) > 0);

        List<String> lines = Files.readAllLines(outputFile);
        assertTrue(lines.stream().anyMatch(line -> line.contains("Total lines: 2")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Total words: 5")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Total characters: 27")));
    }
}
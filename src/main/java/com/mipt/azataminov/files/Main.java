package com.mipt.azataminov.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("=== Демонстрация работы с файлами ===\n");

        TextFileAnalyzer analyzer = new TextFileAnalyzer();

        Path testFile = Files.createTempFile("test", ".txt");
        Files.write(testFile, List.of("Hello world!", "Java NIO is powerful.", "Testing file operations."));

        System.out.println("Анализ файла:");
        TextFileAnalyzer.AnalysisResult result = analyzer.analyzeFile(testFile.toString());
        System.out.println(result);

        Path resultFile = Files.createTempFile("result", ".txt");
        analyzer.saveAnalysisResult(result, resultFile.toString());
        System.out.println("Результаты сохранены в: " + resultFile);

        System.out.println("\nРазбиение и объединение файлов:");
        FileProcessor processor = new FileProcessor();

        Path dataFile = Files.createTempFile("data", ".dat");
        byte[] data = new byte[2048];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        Files.write(dataFile, data);

        String partsDir = Files.createTempDirectory("parts").toString();
        List<Path> parts = processor.splitFile(dataFile.toString(), partsDir, 512);
        System.out.println("Файл разбит на " + parts.size() + " частей");

        Path mergedFile = Files.createTempFile("merged", ".dat");
        processor.mergeFiles(parts, mergedFile.toString());
        System.out.println("Файл успешно объединен");
    }
}
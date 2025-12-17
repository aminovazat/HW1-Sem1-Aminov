package com.mipt.azataminov.files;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class FileProcessor {

    public List<Path> splitFile(String sourcePath, String outputDir, int partSize) throws IOException {
        List<Path> partPaths = new ArrayList<>();
        Path sourceFile = Paths.get(sourcePath);
        String fileName = sourceFile.getFileName().toString();

        try (FileChannel sourceChannel = FileChannel.open(sourceFile, StandardOpenOption.READ)) {
            long fileSize = sourceChannel.size();
            int partNumber = 1;
            long position = 0;

            ByteBuffer buffer = ByteBuffer.allocate(partSize);

            while (position < fileSize) {
                String partName = String.format("%s.part%d", fileName, partNumber);
                Path partPath = Paths.get(outputDir, partName);

                try (FileChannel partChannel = FileChannel.open(partPath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE)) {

                    buffer.clear();
                    int bytesRead = sourceChannel.read(buffer, position);
                    if (bytesRead == -1) break;

                    buffer.flip();
                    partChannel.write(buffer);

                    partPaths.add(partPath);
                    position += bytesRead;
                    partNumber++;
                }
            }
        }

        return partPaths;
    }

    public void mergeFiles(List<Path> partPaths, String outputPath) throws IOException {
        try (FileChannel outputChannel = FileChannel.open(Paths.get(outputPath),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE)) {

            for (Path partPath : partPaths) {
                if (!Files.exists(partPath)) {
                    throw new FileNotFoundException("Part file not found: " + partPath);
                }

                try (FileChannel partChannel = FileChannel.open(partPath, StandardOpenOption.READ)) {
                    ByteBuffer buffer = ByteBuffer.allocate(8192);

                    while (partChannel.read(buffer) > 0) {
                        buffer.flip();
                        outputChannel.write(buffer);
                        buffer.clear();
                    }
                }
            }
        }
    }
}
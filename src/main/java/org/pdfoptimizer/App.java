package org.pdfoptimizer;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfCompressionLevel;
import com.spire.pdf.PdfDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App
{
    public static void main( String[] args ) {
        if(args.length < 2) {
            System.out.println("Invalid number of arguments");

            return;
        }

        String inputDirectory = args[0];
        String outputDirectory = args[1];

        createOutputFolder(outputDirectory);
        optimizeFiles(inputDirectory, outputDirectory);
    }

    private static void optimizeFiles(String inputPath, String outputPath) {
        Path path = Paths.get(inputPath);

        try(Stream<Path> paths = Files.walk(path)) {
            String parentDirectory = path.toAbsolutePath().getFileName().toString();

            for(Path p : paths.collect(Collectors.toList())) {
                if(!Files.isRegularFile(p)) {
                    continue;
                }

                String parentFolder = p.toAbsolutePath().getParent().getFileName().toString();

                PdfDocument document = new PdfDocument();

                document.loadFromFile(p.toAbsolutePath().toString());

                document.getFileInfo().setIncrementalUpdate(false);
                document.setCompressionLevel(PdfCompressionLevel.Best);

                if (parentDirectory.equals(parentFolder)) {
                    document.saveToFile(String.format("%s/%s", outputPath, p.getFileName()), FileFormat.PDF);
                } else {
                    document.saveToFile(String.format("%s/%s/%s", outputPath, parentFolder, p.getFileName()), FileFormat.PDF);
                }
            }
        } catch (Exception err) {
            System.out.println("Something went wrong");
        }
    }

    private static void createOutputFolder(String path) {
        try {
            Path outputPath = Paths.get(path);

            deleteFilesRecursively(outputPath);
            Files.createDirectory(outputPath);
        } catch (IOException err) {
            System.out.println("Something went wrong while creating the output folder");
        }
    }

    private static void deleteFilesRecursively(Path path)  {
        try(Stream<Path> paths = Files.walk(path)) {
            paths
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        System.err.printf("Unable to delete this path : %s%n%s", path, e);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

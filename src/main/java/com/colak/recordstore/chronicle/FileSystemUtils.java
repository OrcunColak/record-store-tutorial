package com.colak.recordstore.chronicle;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
public class FileSystemUtils {

    public static void deleteRecursively(Path path) {
        if (!Files.exists(path)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(path)) {
            walk
                    .sorted(Comparator.reverseOrder()) // delete children first
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + p, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to delete path {}", path, e);
        }
    }
}

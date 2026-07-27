package com.periodtracker;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PeriodTrackerApplication {

    public static void main(String[] args) {
        ensureDataDirectory();
        SpringApplication.run(PeriodTrackerApplication.class, args);
    }

    private static void ensureDataDirectory() {
        try {
            Files.createDirectories(Path.of("data"));
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not create ./data directory for SQLite", ex);
        }
    }
}

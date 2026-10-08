package com.lyxor.fulfillment.service;

import com.lyxor.fulfillment.model.AuditEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

public class AuditLogExporter {

    public void appendEntries(Path filePath, List<AuditEntry> entries) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            for (AuditEntry entry : entries) {
                writer.write(entry.toString());
                writer.newLine();
            }
        }
    }

    public long countActionsForEntity(Path filePath, String entityType, String action) throws IOException {
        Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8);
        return lines
                .filter(line -> line.contains(entityType) && line.contains(action))
                .count();
    }
}

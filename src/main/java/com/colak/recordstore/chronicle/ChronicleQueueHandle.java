package com.colak.recordstore.chronicle;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;

import java.nio.file.Path;
import java.util.UUID;

final class ChronicleQueueHandle {
    private final UUID id;
    private final Path path;
    private final ChronicleQueue queue;
    private final ExcerptAppender appender;

    public ChronicleQueueHandle(UUID id, Path path, ChronicleQueue queue) {
        this.id = id;
        this.path = path;
        this.queue = queue;
        this.appender = queue.createAppender();
    }

    public UUID id() {
        return id;
    }

    public Path path() {
        return path;
    }

    public ExcerptAppender appender() {
        return appender;
    }

    public ExcerptTailer createTailer() {
        return queue.createTailer();
    }

    void close() {
        appender.close();
        queue.close();
    }
}

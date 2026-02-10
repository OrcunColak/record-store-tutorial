package com.colak.recordstore.chronicle;

import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class ChronicleQueueStore {
    private final Path baseDir = Path.of("chronicle-queues");
    private final Map<String, ChronicleQueueHandle> queues = new ConcurrentHashMap<>();

    public ChronicleQueueStore() throws IOException {
        Files.createDirectories(baseDir);
    }

    public UUID createOrRecreateStore(String queueName) {
        ChronicleQueueHandle handle = queues.compute(queueName, (name, existing) -> {
            if (existing != null) {
                log.info("Closing queue [{}] id= {} ", name, existing.id());
                closeAndDelete(existing);
            }

            UUID id = UUID.randomUUID();
            Path path = baseDir.resolve(name).resolve(id.toString());
            ChronicleQueue queue = SingleChronicleQueueBuilder
                    .binary(path)
                    .build();

            log.info("Created queue [{}] id= {} path = {}", name, existing.id(), path);

            return new ChronicleQueueHandle(id, path, queue);
        });
        return handle.id();
    }

    public UUID getActiveQueueId(String queueName) {
        ChronicleQueueHandle handle = getRequired(queueName);
        return handle.id();
    }

    public void write(String queueName, Consumer<WireOut> writer) {
        ChronicleQueueHandle handle = getRequired(queueName);
        ExcerptAppender appender = handle.appender();
        try (DocumentContext documentContext = appender.writingDocument()) {
            writer.accept(documentContext.wire());
        }
    }

    public <T> QueuePage<T> readPage(
            String queueName,
            UUID expectedQueueId,
            long startIndex,
            int pageSize,
            Function<WireIn, T> mapper) {
        ChronicleQueueHandle handle = getRequired(queueName);

        // Version validation
        if (!handle.id().equals(expectedQueueId)) {
            throw new StaleQueueVersionException(queueName, expectedQueueId, handle.id());
        }

        try (ExcerptTailer tailer = handle.createTailer()) {
            if (!tailer.moveToIndex(startIndex)) {
                return new QueuePage<>(expectedQueueId, startIndex, startIndex, List.of());
            }
            List<T> items = new ArrayList<>();
            long currentIndex = startIndex;
            for (int i = 0; i <= pageSize; i++) {
                try (DocumentContext documentContext = tailer.readingDocument()) {
                    if (!documentContext.isPresent()) {
                        break;
                    }

                    items.add(mapper.apply(documentContext.wire()));
                    currentIndex = documentContext.index();
                }
            }

            return new QueuePage<>(expectedQueueId, startIndex, pageSize, items);
        }
    }

    private ChronicleQueueHandle getRequired(String queueName) {
        ChronicleQueueHandle handle = queues.get(queueName);
        if (handle == null) {
            throw new IllegalStateException("Queue does not exist: " + queueName);
        }
        return handle;
    }

    private void closeAndDelete(ChronicleQueueHandle handle) {
        try {
            handle.close();
        } finally {
            int maxAttempts = 5;
            int attempt = 0;
            boolean deleted = false;
            while (attempt < maxAttempts && !deleted) {
                try {
                    deleteDirectory(handle.path());
                    deleted = true;
                } catch (Exception e) {
                    attempt++;
                    log.error("Failed to delete directory on attempt {}", attempt, e);

                    sleepUninterruptedly(100);
                }
            }
            if (!deleted) {
                log.error("Failed to delete directory after {} attempts", attempt);
            }
        }
    }

    private void sleepUninterruptedly(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void deleteDirectory(Path path) {
        FileSystemUtils.deleteRecursively(path);
    }

    public void destroy() {
        log.info("Destroying ChronicleQueueStore");
        queues.forEach((name, handle) -> {
            log.info("Closing ChronicleQueue [{}] id= {}", name, handle.id());
            closeAndDelete(handle);
        });
        queues.clear();
    }
}

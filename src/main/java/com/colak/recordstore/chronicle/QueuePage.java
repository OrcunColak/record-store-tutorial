package com.colak.recordstore.chronicle;

import java.util.List;
import java.util.UUID;

public record QueuePage<T>(
        UUID queueId,
        long fromIndex,
        long nextIndex,
        List<T> items
) {

}

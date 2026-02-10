package com.colak.recordstore.chronicle;

import java.util.UUID;

public class StaleQueueVersionException extends RuntimeException {

    public StaleQueueVersionException(String queueName, UUID requested, UUID active) {
        super("Queue [" + queueName + "] has version " + active + " but requested " + requested);
    }
}

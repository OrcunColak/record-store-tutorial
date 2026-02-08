package com.colak.recordstore.fixedlength;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BinaryPagedStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void writesRowsAndReadsFirstPage() throws Exception {
        Path file = tempDir.resolve("store.bin");

        int pageSize = 10;

        byte[][] writtenRows = new byte[pageSize][];
        for (int i = 0; i < pageSize; i++) {
            writtenRows[i] = rowWithValue(i);
        }

        // write
        try (BinaryPagedStore store = new BinaryPagedStoreBuilder()
                .path(file)
                .pageSize(pageSize)
                .build()) {

            for (byte[] row : writtenRows) {
                store.appendRow(row);
            }
        }

        // read
        try (BinaryPagedStore store = new BinaryPagedStoreBuilder()
                .path(file)
                .pageSize(pageSize)
                .build()) {

            List<byte[]> page0 = store.readPage(0);

            assertEquals(pageSize, page0.size());

            for (int i = 0; i < pageSize; i++) {
                assertArrayEquals(writtenRows[i], page0.get(i));
            }
        }
    }

    private static byte[] rowWithValue(int value) {
        byte[] row = new byte[BinaryPagedStoreConstants.ROW_SIZE];

        // simple deterministic content
        row[0] = (byte) value;
        row[1] = (byte) (value >>> 8);
        row[2] = (byte) (value >>> 16);
        row[3] = (byte) (value >>> 24);

        return row;
    }
}

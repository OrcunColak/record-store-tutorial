package com.colak.recordstore.fixedlength;

import java.io.IOException;
import java.util.List;

public interface BinaryPagedStore extends AutoCloseable {

    void appendRow(byte[] row) throws IOException;

    List<byte[]> readPage(long page) throws IOException;

    @Override
    void close() throws IOException;
}


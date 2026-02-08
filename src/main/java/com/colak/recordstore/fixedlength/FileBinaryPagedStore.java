package com.colak.recordstore.fixedlength;

import com.colak.recordstore.fixedlength.reader.PagedReader;
import com.colak.recordstore.fixedlength.writer.BlockWriter;

import java.io.IOException;
import java.util.List;

final class FileBinaryPagedStore implements BinaryPagedStore {
    private final BlockWriter writer;
    private final PagedReader reader;
    private final int pageSize;

    FileBinaryPagedStore(BlockWriter writer, PagedReader reader, int pageSize) {
        this.writer = writer;
        this.reader = reader;
        this.pageSize = pageSize;
    }

    @Override
    public void appendRow(byte[] row) throws IOException {
        writer.appendRow(row);
    }

    @Override
    public List<byte[]> readPage(long page) throws IOException {
        return reader.readPage(page, pageSize);
    }

    @Override
    public void close() throws IOException {
        // important: writer first, to flush last block
        writer.close();
        reader.close();
    }
}

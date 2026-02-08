package com.colak.recordstore.fixedlength;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PagedReader implements AutoCloseable {

    private final BlockReader blockReader;

    public PagedReader(Path path) throws IOException {
        this.blockReader = new BlockReader(path);
    }

    public List<byte[]> readPage(long page, int pageSize) throws IOException {
        long firstRow = page * pageSize;

        long blockIndex = firstRow / BinaryPagedStore.ROWS_PER_BLOCK;
        int offsetInBlock = (int) (firstRow % BinaryPagedStore.ROWS_PER_BLOCK);

        List<byte[]> result = new ArrayList<>(pageSize);

        while (result.size() < pageSize) {
            DataBlock block = blockReader.readBlock(blockIndex);
            if (block == null) break;

            int row = offsetInBlock;
            offsetInBlock = 0;

            while (row < block.rowCount() && result.size() < pageSize) {
                result.add(block.readRow(row));
                row++;
            }
            blockIndex++;
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        blockReader.close();
    }
}

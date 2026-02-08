package com.colak.recordstore.fixedlength.reader;

import com.colak.recordstore.fixedlength.BinaryPagedStoreConstants;
import com.colak.recordstore.fixedlength.datablock.DataBlock;
import com.colak.recordstore.fixedlength.datablock.deserialize.DataBlockDeserializer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PagedReader implements AutoCloseable {
    private final BlockReader blockReader;

    public PagedReader(Path path, DataBlockDeserializer deserializer) throws IOException {
        this.blockReader = new BlockReader(path, deserializer);
    }

    public List<byte[]> readPage(long page, int pageSize) throws IOException {
        long firstRow = page * pageSize;

        long blockIndex = firstRow / BinaryPagedStoreConstants.ROWS_PER_BLOCK;
        int offsetInBlock = (int) (firstRow % BinaryPagedStoreConstants.ROWS_PER_BLOCK);

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

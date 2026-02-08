package com.colak.recordstore.fixedlength.writer;


import com.colak.recordstore.fixedlength.BinaryPagedStoreConstants;
import com.colak.recordstore.fixedlength.datablock.serialize.DataBlockSerializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BlockWriter implements AutoCloseable {
    private final FileChannel channel;
    private final ByteBuffer rawBuffer;
    private final DataBlockSerializer serializer;

    public BlockWriter(Path path, DataBlockSerializer serializer) throws IOException {
        this.channel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND);

        this.rawBuffer = ByteBuffer.allocate(
                BinaryPagedStoreConstants.BLOCK_SIZE - BinaryPagedStoreConstants.HEADER_SIZE);

        this.serializer = serializer;
    }

    public void appendRow(byte[] row) throws IOException {
        if (row.length != BinaryPagedStoreConstants.ROW_SIZE) {
            throw new IllegalArgumentException("Row must be " + BinaryPagedStoreConstants.ROW_SIZE + " bytes");
        }
        if (rawBuffer.remaining() < BinaryPagedStoreConstants.ROW_SIZE) {
            flushBlock();
        }
        rawBuffer.put(row);
    }

    private void flushBlock() throws IOException {
        if (rawBuffer.position() == 0) {
            return;
        }
        ByteBuffer block = serializer.serialize(rawBuffer);
        channel.write(block);
        rawBuffer.clear();
    }

    @Override
    public void close() throws IOException {
        flushBlock();
        channel.close();
    }
}

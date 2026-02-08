package com.colak.recordstore.fixedlength.reader;

import com.colak.recordstore.fixedlength.BinaryPagedStoreConstants;
import com.colak.recordstore.fixedlength.datablock.DataBlock;
import com.colak.recordstore.fixedlength.datablock.deserialize.DataBlockDeserializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BlockReader implements AutoCloseable {
    private final FileChannel channel;
    private final DataBlockDeserializer deserializer;

    public BlockReader(Path path, DataBlockDeserializer deserializer) throws IOException {
        this.channel = FileChannel.open(path, StandardOpenOption.READ);
        this.deserializer = deserializer;
    }

    public DataBlock readBlock(long blockIndex) throws IOException {
        ByteBuffer block = ByteBuffer.allocate(BinaryPagedStoreConstants.BLOCK_SIZE);
        channel.position(blockIndex * BinaryPagedStoreConstants.BLOCK_SIZE);

        int read = channel.read(block);
        if (read <= 0) {
            return null;
        }

        block.flip();
        return deserializer.deserialize(blockIndex, block);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
package com.colak.recordstore.fixedlength;


import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BlockWriter implements AutoCloseable {
    private final FileChannel channel;
    private final ByteBuffer rawBuffer;

    private final LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

    public BlockWriter(Path path) throws IOException {
        this.channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        this.rawBuffer = ByteBuffer.allocate(BinaryPagedStore.BLOCK_SIZE - BinaryPagedStore.HEADER_SIZE);
    }

    public void appendRow(byte[] row) throws IOException {
        if (row.length != BinaryPagedStore.ROW_SIZE) {
            throw new IllegalArgumentException("Row must be " + BinaryPagedStore.ROW_SIZE + " bytes");
        }
        if (rawBuffer.remaining() < BinaryPagedStore.ROW_SIZE) {
            flushBlock();
        }
        rawBuffer.put(row);
    }

    private void flushBlock() throws IOException {
        if (rawBuffer.position() == 0) {
            return;
        }

        rawBuffer.flip();

        byte[] compressed = new byte[compressor.maxCompressedLength(rawBuffer.remaining())];

        int compressedSize = compressor.compress(rawBuffer.array(), 0, rawBuffer.remaining(), compressed, 0);

        ByteBuffer block = ByteBuffer.allocate(BinaryPagedStore.BLOCK_SIZE);

        block.putInt(BinaryPagedStore.MAGIC);
        block.putShort((short) 1); // version
        block.putShort((short) 1); // flags (LZ4)
        block.putInt(compressedSize);
        block.putInt(rawBuffer.remaining());
        block.put(compressed, 0, compressedSize);

        block.flip();
        channel.write(block);

        rawBuffer.clear();
    }

    @Override
    public void close() throws IOException {
        flushBlock();
        channel.close();
    }
}

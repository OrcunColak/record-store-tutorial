package com.colak.recordstore.fixedlength;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BlockReader implements AutoCloseable {
    private final FileChannel channel;
    private final LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();

    public BlockReader(Path path) throws IOException {
        this.channel = FileChannel.open(path, StandardOpenOption.READ);
    }

    public DataBlock readBlock(long blockIndex) throws IOException {
        ByteBuffer block = ByteBuffer.allocate(BinaryPagedStore.BLOCK_SIZE);
        channel.position(blockIndex * BinaryPagedStore.BLOCK_SIZE);

        int read = channel.read(block);
        if (read <= 0) {
            return null;
        }

        block.flip();

        int magic = block.getInt();
        if (magic != BinaryPagedStore.MAGIC) {
            throw new IllegalStateException("Invalid block at index " + blockIndex);
        }

        block.getShort(); // version
        block.getShort(); // flags
        int compressedSize = block.getInt();
        int uncompressedSize = block.getInt();

        byte[] compressed = new byte[compressedSize];
        block.get(compressed);

        byte[] raw = new byte[uncompressedSize];
        decompressor.decompress(compressed, raw);

        int rowCount = uncompressedSize / BinaryPagedStore.ROW_SIZE;
        return new DataBlock(blockIndex, rowCount, raw);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}

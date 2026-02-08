package com.colak.recordstore.fixedlength.datablock.serialize;

import com.colak.recordstore.fixedlength.BinaryPagedStoreConstants;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import java.nio.ByteBuffer;

public final class Lz4DataBlockSerializer implements DataBlockSerializer {

    private final LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();

    @Override
    public ByteBuffer serialize(ByteBuffer rawRows) {
        // Creates a *read-only VIEW* of the same underlying buffer.
        rawRows = rawRows.asReadOnlyBuffer();
        // converts the buffer into read mode:
        rawRows.flip();

        // Allocate a byte[] exactly large enough to hold all remaining bytes
        byte[] src = new byte[rawRows.remaining()];
        // Copies bytes from the ByteBuffer view into the array.
        rawRows.get(src);

        // Allocate a buffer large enough for the worst-case LZ4 compressed size.
        byte[] compressed = new byte[compressor.maxCompressedLength(src.length)];

        // Compress the raw row bytes.
        int compressedSize = compressor.compress(src, 0, src.length, compressed, 0);

        // Allocate a fixed-size block buffer.
        ByteBuffer block = ByteBuffer.allocate(BinaryPagedStoreConstants.BLOCK_SIZE);

        // Write block header fields (fixed layout).
        block.putInt(BinaryPagedStoreConstants.MAGIC);
        block.putShort((short) 1); // version
        block.putShort((short) 1); // flags
        block.putInt(compressedSize);
        block.putInt(src.length);

        // Write compressed bytes.
        block.put(compressed, 0, compressedSize);

        // Switch block buffer to read mode so it can be written to FileChannel.
        block.flip();
        return block;
    }
}
package com.colak.recordstore.fixedlength.datablock.deserialize;

import com.colak.recordstore.fixedlength.BinaryPagedStoreConstants;
import com.colak.recordstore.fixedlength.datablock.DataBlock;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.ByteBuffer;

public final class Lz4DataBlockDeserializer implements DataBlockDeserializer {
    private final LZ4FastDecompressor decompressor;

    public Lz4DataBlockDeserializer(LZ4FastDecompressor decompressor) {
        this.decompressor = decompressor;
    }

    @Override
    public DataBlock deserialize(long blockIndex, ByteBuffer block) {
        int magic = block.getInt();
        if (magic != BinaryPagedStoreConstants.MAGIC) {
            throw new IllegalStateException("Invalid block at index " + blockIndex);
        }

        short version = block.getShort();
        short flags = block.getShort();

        int compressedSize = block.getInt();
        int uncompressedSize = block.getInt();

        byte[] compressed = new byte[compressedSize];
        block.get(compressed);

        byte[] raw = new byte[uncompressedSize];
        decompressor.decompress(compressed, raw);

        int rowCount = uncompressedSize / BinaryPagedStoreConstants.ROW_SIZE;
        return new DataBlock(blockIndex, rowCount, raw);
    }
}


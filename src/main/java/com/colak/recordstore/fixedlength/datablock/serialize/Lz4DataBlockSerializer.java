package com.colak.recordstore.fixedlength.datablock.serialize;

import com.colak.recordstore.fixedlength.BinaryPagedStoreConstants;
import net.jpountz.lz4.LZ4Compressor;

import java.nio.ByteBuffer;

public final class Lz4DataBlockSerializer implements DataBlockSerializer {

    private final LZ4Compressor compressor;

    public Lz4DataBlockSerializer(LZ4Compressor compressor) {
        this.compressor = compressor;
    }

    @Override
    public ByteBuffer serialize(ByteBuffer rawRows) {
        rawRows = rawRows.asReadOnlyBuffer();
        rawRows.flip();

        byte[] src = new byte[rawRows.remaining()];
        rawRows.get(src);

        byte[] compressed = new byte[compressor.maxCompressedLength(src.length)];
        int compressedSize = compressor.compress(src, 0, src.length, compressed, 0);

        ByteBuffer block = ByteBuffer.allocate(BinaryPagedStoreConstants.BLOCK_SIZE);

        block.putInt(BinaryPagedStoreConstants.MAGIC);
        block.putShort((short) 1); // version
        block.putShort((short) 1); // flags
        block.putInt(compressedSize);
        block.putInt(src.length);
        block.put(compressed, 0, compressedSize);

        block.flip();
        return block;
    }
}


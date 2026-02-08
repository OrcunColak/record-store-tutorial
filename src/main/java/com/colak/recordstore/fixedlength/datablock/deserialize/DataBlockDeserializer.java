package com.colak.recordstore.fixedlength.datablock.deserialize;

import com.colak.recordstore.fixedlength.datablock.DataBlock;

import java.nio.ByteBuffer;

public interface DataBlockDeserializer {
    DataBlock deserialize(long blockIndex, ByteBuffer block);
}


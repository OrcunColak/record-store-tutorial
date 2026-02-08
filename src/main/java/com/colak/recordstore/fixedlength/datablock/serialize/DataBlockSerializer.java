package com.colak.recordstore.fixedlength.datablock.serialize;

import java.nio.ByteBuffer;

public interface DataBlockSerializer {

    ByteBuffer serialize(ByteBuffer rawRows);
}


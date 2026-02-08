package com.colak.recordstore.fixedlength;

import com.colak.recordstore.fixedlength.datablock.deserialize.DataBlockDeserializer;
import com.colak.recordstore.fixedlength.datablock.deserialize.Lz4DataBlockDeserializer;
import com.colak.recordstore.fixedlength.datablock.serialize.DataBlockSerializer;
import com.colak.recordstore.fixedlength.datablock.serialize.Lz4DataBlockSerializer;
import com.colak.recordstore.fixedlength.reader.PagedReader;
import com.colak.recordstore.fixedlength.writer.BlockWriter;

import java.io.IOException;
import java.nio.file.Path;

public final class BinaryPagedStoreBuilder {
    private Path path;

    // overridable, but defaults exist
    private DataBlockSerializer serializer;
    private DataBlockDeserializer deserializer;

    private int pageSize = BinaryPagedStoreConstants.ROWS_PER_BLOCK;

    public BinaryPagedStoreBuilder path(Path path) {
        this.path = path;
        return this;
    }

    public BinaryPagedStoreBuilder serializer(DataBlockSerializer serializer) {
        this.serializer = serializer;
        return this;
    }

    public BinaryPagedStoreBuilder deserializer(DataBlockDeserializer deserializer) {
        this.deserializer = deserializer;
        return this;
    }

    public BinaryPagedStoreBuilder pageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }
        this.pageSize = pageSize;
        return this;
    }

    public BinaryPagedStore build() throws IOException {
        if (path == null) {
            throw new IllegalStateException("path not set");
        }

        // create defaults if not provided
        DataBlockSerializer serializerToUse = serializer != null ? serializer : new Lz4DataBlockSerializer();
        DataBlockDeserializer deserializerToUse = deserializer != null ? deserializer : new Lz4DataBlockDeserializer();

        BlockWriter writer = new BlockWriter(path, serializerToUse);

        PagedReader reader = new PagedReader(path, deserializerToUse);

        return new FileBinaryPagedStore(writer, reader, pageSize);
    }
}

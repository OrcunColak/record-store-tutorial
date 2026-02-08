package com.colak.recordstore.fixedlength;

public final class BinaryPagedStoreConstants {

    /// Size of a single logical row (fixed-length).
    public static final int ROW_SIZE = 1024;

    /// Number of rows stored in a single block.
    public static final int ROWS_PER_BLOCK = 512;

    /// Header stored at the beginning of every block.
    /// Layout (current):
    /// - magic            (4 bytes)
    /// - version          (2 bytes)
    /// - flags            (2 bytes)
    /// - compressedSize   (4 bytes)
    /// - uncompressedSize (4 bytes)
    ///
    /// Extra space is intentionally reserved for future use (checksum, encryption IV, sequence number, etc.).
    public static final int HEADER_SIZE = 32;

    /// Total on-disk block size.
    public static final int BLOCK_SIZE = HEADER_SIZE + (ROW_SIZE * ROWS_PER_BLOCK);

    /// Magic number used to detect corruption and format mismatch.
    /// ASCII: "BLK1"
    public static final int MAGIC = 0x424C4B31;

    // ---- invariants ----
    static {
        // Ensure blocks always contain an exact number of rows
        if ((BLOCK_SIZE - HEADER_SIZE) % ROW_SIZE != 0) {
            throw new IllegalStateException("BLOCK_SIZE must align exactly to ROW_SIZE");
        }
    }

    private BinaryPagedStoreConstants() {
        // no instances
    }
}




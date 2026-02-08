package com.colak.recordstore.fixedlength;

/**
 * Binary block-based store with:
 * - Fixed-size blocks
 * - LZ4 compression
 * - Fixed-size rows
 * - Stateless paging
 */
public final class BinaryPagedStore {
    public static final int BLOCK_SIZE = 16 * 1024; // 16 KB
    public static final int HEADER_SIZE = 16;
    public static final int ROW_SIZE = 64;

    public static final int ROWS_PER_BLOCK =
            (BLOCK_SIZE - HEADER_SIZE) / ROW_SIZE;

    public static final int MAGIC = 0x424C4B31; // "BLK1"

}


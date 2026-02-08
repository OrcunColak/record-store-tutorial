package com.colak.recordstore.fixedlength.datablock;

import com.colak.recordstore.fixedlength.BinaryPagedStoreConstants;

public class DataBlock {
    private final long blockIndex;
    private final int rowCount;
    private final byte[] data;

    public DataBlock(long blockIndex, int rowCount, byte[] data) {
        this.blockIndex = blockIndex;
        this.rowCount = rowCount;
        this.data = data;
    }

    public int rowCount() {
        return rowCount;
    }

    public byte[] readRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rowCount) {
            throw new IndexOutOfBoundsException();
        }
        byte[] row = new byte[BinaryPagedStoreConstants.ROW_SIZE];
        System.arraycopy(data, rowIndex * BinaryPagedStoreConstants.ROW_SIZE, row, 0, BinaryPagedStoreConstants.ROW_SIZE);
        return row;
    }
}

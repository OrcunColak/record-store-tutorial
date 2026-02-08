package com.colak.recordstore.fixedlength;

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
        byte[] row = new byte[BinaryPagedStore.ROW_SIZE];
        System.arraycopy(data, rowIndex * BinaryPagedStore.ROW_SIZE, row, 0, BinaryPagedStore.ROW_SIZE);
        return row;
    }
}

package com.trader.network;

import java.io.Serializable;

public class FileChunk implements Serializable {
    private String transferId;
    private byte[] data;
    private int bytesRead;
    private long totalBytesSent;
    
    public FileChunk(String transferId, byte[] data, int bytesRead, long totalBytesSent) {
        this.transferId = transferId;
        this.data = data;
        this.bytesRead = bytesRead;
        this.totalBytesSent = totalBytesSent;
    }
    
    // Getters
    public String getTransferId() { return transferId; }
    public byte[] getData() { return data; }
    public int getBytesRead() { return bytesRead; }
    public long getTotalBytesSent() { return totalBytesSent; }
}
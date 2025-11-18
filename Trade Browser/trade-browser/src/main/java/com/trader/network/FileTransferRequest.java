package com.trader.network;

import java.io.Serializable;

public class FileTransferRequest implements Serializable {
    private String fileName;
    private long fileSize;
    private String transferId;
    
    public FileTransferRequest(String fileName, long fileSize, String transferId) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.transferId = transferId;
    }
    
    // Getters
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public String getTransferId() { return transferId; }
}
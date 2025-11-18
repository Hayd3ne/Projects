package com.trader.network;

import java.io.Serializable;

public class TradeFile implements Serializable {
    private String fileName;
    private long fileSize;
    private String filePath;
    
    public TradeFile(String fileName, long fileSize, String filePath) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
    }
    
    // Getters
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public String getFilePath() { return filePath; }
}
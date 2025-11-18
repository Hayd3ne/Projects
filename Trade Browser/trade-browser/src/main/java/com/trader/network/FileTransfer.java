package com.trader.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTransfer {
    private String fileName;
    private String filePath;
    private long totalSize;
    private long bytesReceived;
    private String transferId;
    private FileOutputStream fileOutputStream;
    private boolean initialized;
    
    public FileTransfer(String fileName, String filePath, long totalSize, String transferId) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.totalSize = totalSize;
        this.transferId = transferId;
        this.bytesReceived = 0;
        this.initialized = false;
        this.fileOutputStream = null;
    }
    
    public synchronized void writeChunk(byte[] data) throws IOException {
        if (!initialized) {
            // Ensure directory exists
            File outputFile = new File(filePath);
            File parentDir = outputFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            this.fileOutputStream = new FileOutputStream(outputFile);
            this.initialized = true;
        }
        
        if (fileOutputStream != null) {
            fileOutputStream.write(data);
            fileOutputStream.flush(); // Ensure data is written to disk
            bytesReceived += data.length;
        }
    }
    
    public boolean isComplete() {
        return bytesReceived >= totalSize;
    }
    
    public void close() {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing file stream: " + e.getMessage());
        }
    }
    
    // Getters
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public long getTotalSize() { return totalSize; }
    public long getBytesReceived() { return bytesReceived; }
    public String getTransferId() { return transferId; }
}
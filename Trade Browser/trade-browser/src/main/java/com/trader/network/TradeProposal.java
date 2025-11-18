package com.trader.network;

import java.io.Serializable;
import java.util.List;

public class TradeProposal implements Serializable {
    private List<TradeFile> files;
    private long totalSize;
    private String proposalId;
    private long timestamp;
    
    public TradeProposal(List<TradeFile> files, long totalSize, String proposalId) {
        this.files = files;
        this.totalSize = totalSize;
        this.proposalId = proposalId;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public List<TradeFile> getFiles() { return files; }
    public long getTotalSize() { return totalSize; }
    public String getProposalId() { return proposalId; }
    public long getTimestamp() { return timestamp; }
}
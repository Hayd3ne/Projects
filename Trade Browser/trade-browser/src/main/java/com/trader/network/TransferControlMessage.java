package com.trader.network;

import java.io.Serializable;

public class TransferControlMessage implements Serializable {
    public enum Type {
        ACCEPT, REJECT, COMPLETE, CANCEL
    }
    
    private Type type;
    private String transferId;
    
    public TransferControlMessage(Type type, String transferId) {
        this.type = type;
        this.transferId = transferId;
    }
    
    // Getters
    public Type getType() { return type; }
    public String getTransferId() { return transferId; }
}
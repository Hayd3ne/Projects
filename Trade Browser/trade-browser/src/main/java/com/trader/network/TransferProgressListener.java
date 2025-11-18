package com.trader.network;

public interface TransferProgressListener extends ConnectionListener {
    void onTransferProgress(String transferId, String fileName, long bytesTransferred, long totalBytes, boolean isUpload);
}
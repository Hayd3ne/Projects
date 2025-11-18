package com.trader.network;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class NetworkManager {
    private boolean isConnected = false;
    private boolean isHosting = false;
    private String connectedPeer = null;
    private int port = 8080;
    
    // Socket components
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    
    // Thread management
    private ExecutorService connectionExecutor;
    private Thread serverThread;
    
    // Connection state listeners
    private List<ConnectionListener> connectionListeners;
    
    // File transfer tracking
    private Map<String, FileTransfer> activeTransfers;
    private String downloadDirectory;
    
    // Trade management
    private List<TradeProposal> incomingTrades;

    private boolean autoAcceptTransfers = false;
    private String currentTradeId;
    
    public NetworkManager() {
        this.connectionListeners = new CopyOnWriteArrayList<>();
        this.activeTransfers = new ConcurrentHashMap<>();
        this.incomingTrades = new CopyOnWriteArrayList<>();
        this.connectionExecutor = Executors.newCachedThreadPool();
        this.downloadDirectory = createDownloadDirectory();
    }
    
    private String createDownloadDirectory() {
        String userHome = System.getProperty("user.home");
        File downloadDir = new File(userHome, "FileTraderDownloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        return downloadDir.getAbsolutePath();
    }
    
    // Connection methods (unchanged from previous)
    public boolean connectToPeer(String ipAddress, int port) {
    if (isConnected) {
        notifyConnectionListeners("Already connected to " + connectedPeer);
        return false;
    }
    
    try {
        String actualAddress = resolveHostname(ipAddress.trim());
        notifyConnectionListeners("Connecting to " + actualAddress + ":" + port + "...");
        
        clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(actualAddress, port), 5000);
        
        System.out.println("DEBUG [CONNECT]: Connection established, initializing streams...");
        
        // CRITICAL FIX: Create output stream first and flush the header
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        outputStream.flush(); // This sends the stream header
        System.out.println("DEBUG [CONNECT]: Output stream created and flushed");
        
        // Then create input stream
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        System.out.println("DEBUG [CONNECT]: Input stream created");
        
        isConnected = true;
        connectedPeer = actualAddress;
        this.port = port;
        
        // Start listening for incoming messages
        startMessageListener();
        
        notifyConnectionListeners("Successfully connected to " + actualAddress + ":" + port);
        return true;
        
    } catch (IOException e) {
        notifyConnectionListeners("Failed to connect to " + ipAddress + ":" + port + " - " + e.getMessage());
        disconnect();
        return false;
    }
}
    
    public boolean hostSession(int port) {
        if (isHosting || isConnected) {
            notifyConnectionListeners("Already hosting or connected");
            return false;
        }
        
        try {
            this.port = port;
            serverSocket = new ServerSocket(port);
            isHosting = true;
            
            notifyConnectionListeners("Hosting session on port " + port + "...");
            
            serverThread = new Thread(this::acceptConnections);
            serverThread.setDaemon(true);
            serverThread.start();
            
            return true;
            
        } catch (IOException e) {
            notifyConnectionListeners("Failed to host on port " + port + " - " + e.getMessage());
            isHosting = false;
            return false;
        }
    }
    
    private void acceptConnections() {
        while (isHosting && !serverSocket.isClosed()) {
            try {
                notifyConnectionListeners("Waiting for incoming connections...");
                Socket incomingSocket = serverSocket.accept();
                String clientAddress = incomingSocket.getInetAddress().getHostAddress();
                notifyConnectionListeners("Incoming connection from " + clientAddress);
                handleIncomingConnection(incomingSocket);
                
            } catch (IOException e) {
                if (isHosting && !serverSocket.isClosed()) {
                    notifyConnectionListeners("Error accepting connection: " + e.getMessage());
                }
                break;
            }
        }
    }
    
    private void handleIncomingConnection(Socket socket) {
    connectionExecutor.submit(() -> {
        try {
            if (isConnected) {
                notifyConnectionListeners("Rejecting connection - already connected to " + connectedPeer);
                socket.close();
                return;
            }
            
            clientSocket = socket;
            System.out.println("DEBUG [INCOMING]: Setting up streams for incoming connection");
            
            // CRITICAL FIX: Same order as connectToPeer - output first, then input
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush(); // Send stream header
            System.out.println("DEBUG [INCOMING]: Output stream created and flushed");
            
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("DEBUG [INCOMING]: Input stream created");
            
            isConnected = true;
            connectedPeer = socket.getInetAddress().getHostAddress();
            isHosting = false;
            
            notifyConnectionListeners("Accepted connection from " + connectedPeer);
            startMessageListener();
            
        } catch (IOException e) {
            System.out.println("DEBUG [INCOMING ERROR]: Error handling incoming connection: " + e.getMessage());
            notifyConnectionListeners("Error handling incoming connection: " + e.getMessage());
            disconnect();
        }
    });
}

public void sendHeartbeat() {
    if (!isConnected) {
        return;
    }
    
    try {
        String heartbeat = "HEARTBEAT_" + System.currentTimeMillis();
        outputStream.writeObject(heartbeat);
        outputStream.flush();
        System.out.println("DEBUG [HEARTBEAT]: Sent: " + heartbeat);
    } catch (IOException e) {
        System.out.println("DEBUG [HEARTBEAT ERROR]: " + e.getMessage());
        disconnect();
    }
}
    
    private void startMessageListener() {
    connectionExecutor.submit(() -> {
        System.out.println("DEBUG [LISTENER]: Message listener started");
        try {
            while (isConnected && clientSocket != null && !clientSocket.isClosed()) {
                Object message = inputStream.readObject();
                handleIncomingMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("DEBUG [LISTENER]: Connection closed by peer (EOF)");
            notifyConnectionListeners("Connection closed by peer");
        } catch (SocketException e) {
            if (isConnected) { // Only log if we thought we were connected
                System.out.println("DEBUG [LISTENER]: Socket error: " + e.getMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            if (isConnected) {
                System.out.println("DEBUG [LISTENER]: Error: " + e.getMessage());
            }
        } finally {
            System.out.println("DEBUG [LISTENER]: Message listener exiting");
            disconnect();
        }
    });
}
    
    private void handleIncomingMessage(Object message) {
        System.out.println("DEBUG [RECEIVE]: Received message of type: " + message.getClass().getSimpleName());
    
    if (message instanceof String) {
        String stringMessage = (String) message;
        System.out.println("DEBUG [RECEIVE]: String message: " + stringMessage);
        
        if (stringMessage.startsWith("TRADE_ACCEPTED:")) {
            System.out.println("DEBUG [RECEIVE]: Trade acceptance detected: " + stringMessage);
            handleTradeAccepted(stringMessage);
        } else if (stringMessage.startsWith("HEARTBEAT_")) {
            System.out.println("DEBUG [RECEIVE]: Heartbeat received: " + stringMessage);
            // Just acknowledge receipt, no need to do anything else
        } else {
            notifyConnectionListeners("Message from " + connectedPeer + ": " + stringMessage);
        }
        return;
    }
    
    if (message instanceof FileTransferRequest) {
        System.out.println("DEBUG [RECEIVE]: FileTransferRequest for: " + ((FileTransferRequest) message).getFileName());
        handleFileTransferRequest((FileTransferRequest) message);
    } else if (message instanceof FileChunk) {
        System.out.println("DEBUG [RECEIVE]: FileChunk for transfer: " + ((FileChunk) message).getTransferId());
        handleFileChunk((FileChunk) message);
    } else if (message instanceof TradeProposal) {
        System.out.println("DEBUG [RECEIVE]: TradeProposal with " + ((TradeProposal) message).getFiles().size() + " files");
        handleTradeProposal((TradeProposal) message);
    } else if (message instanceof TransferControlMessage) {
        System.out.println("DEBUG [RECEIVE]: TransferControlMessage type: " + ((TransferControlMessage) message).getType());
        handleTransferControl((TransferControlMessage) message);
    } else if (message instanceof String) {
        String stringMessage = (String) message;
        System.out.println("DEBUG [RECEIVE]: String message: " + stringMessage);
        
        if (stringMessage.startsWith("TRADE_ACCEPTED:")) {
            System.out.println("DEBUG [RECEIVE]: Trade acceptance detected: " + stringMessage);
            handleTradeAccepted(stringMessage);
        } else {
            notifyConnectionListeners("Message from " + connectedPeer + ": " + stringMessage);
        }
    } else {
        System.out.println("DEBUG [RECEIVE]: Unknown message type: " + message.getClass().getSimpleName());
    }
}

private void handleTradeAccepted(String message) {
    // Extract trade ID from message
    String tradeId = message.substring("TRADE_ACCEPTED:".length());
    notifyConnectionListeners("Trade accepted by peer: " + tradeId);
    
    // Notify trade listeners about the acceptance
    for (ConnectionListener listener : connectionListeners) {
        if (listener instanceof TradeListener) {
            ((TradeListener) listener).onTradeAccepted(tradeId);
        }
    }
}
    
    // FILE TRANSFER METHODS
    public void sendFile(File file) {
        if (!isConnected) {
            notifyConnectionListeners("Not connected to any peer");
            return;
        }
        
        if (!file.exists() || !file.isFile()) {
            notifyConnectionListeners("File does not exist or is not a regular file: " + file.getName());
            return;
        }
        
        connectionExecutor.submit(() -> {
            String transferId = UUID.randomUUID().toString();
            try {
                // Send file transfer request
                FileTransferRequest request = new FileTransferRequest(
                    file.getName(), 
                    file.length(),
                    transferId
                );
                
                outputStream.writeObject(request);
                outputStream.flush();
                
                notifyConnectionListeners("Sending file: " + file.getName() + " (" + 
                    formatFileSize(file.length()) + ")");
                
                // Wait for acceptance
                Object response = inputStream.readObject();
                if (response instanceof TransferControlMessage) {
                    TransferControlMessage control = (TransferControlMessage) response;
                    if (control.getType() == TransferControlMessage.Type.ACCEPT) {
                        // Start sending file
                        sendFileInChunks(file, transferId);
                    } else {
                        notifyConnectionListeners("File transfer rejected by peer: " + file.getName());
                    }
                }
                
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionListeners("Error sending file " + file.getName() + ": " + e.getMessage());
            }
        });
    }
    
    private void sendFileInChunks(File file, String transferId) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[8192]; // 8KB chunks
            int bytesRead;
            long totalBytesSent = 0;
            
            while ((bytesRead = fileInputStream.read(buffer)) != -1 && isConnected) {
                FileChunk chunk = new FileChunk(transferId, Arrays.copyOf(buffer, bytesRead), bytesRead, totalBytesSent);
                outputStream.writeObject(chunk);
                outputStream.flush();
                
                totalBytesSent += bytesRead;
                
                // Notify progress
                notifyTransferProgress(transferId, file.getName(), totalBytesSent, file.length(), true);
                
                // Small delay to prevent overwhelming the network
                Thread.sleep(1);
            }
            
            if (isConnected) {
                // Send transfer complete message
                TransferControlMessage complete = new TransferControlMessage(
                    TransferControlMessage.Type.COMPLETE, transferId);
                outputStream.writeObject(complete);
                outputStream.flush();
                
                notifyConnectionListeners("File sent successfully: " + file.getName());
                notifyTransferProgress(transferId, file.getName(), file.length(), file.length(), true);
            }
            
        } catch (IOException | InterruptedException e) {
            notifyConnectionListeners("Error sending file chunks for " + file.getName() + ": " + e.getMessage());
        }
    }
    
     private void handleFileTransferRequest(FileTransferRequest request) {
    System.out.println("DEBUG: Received file transfer request for: " + request.getFileName());
    System.out.println("DEBUG: Auto-accept enabled: " + autoAcceptTransfers);
    System.out.println("DEBUG: Current trade ID: " + currentTradeId);
    System.out.println("DEBUG: Transfer ID: " + request.getTransferId());
    
    // Auto-accept if this is part of an accepted trade, otherwise show dialog
    if (autoAcceptTransfers && request.getTransferId().startsWith(currentTradeId)) {
        System.out.println("DEBUG: Auto-accepting file transfer");
        try {
            acceptFileTransfer(request);
        } catch (IOException e) {
            notifyConnectionListeners("Error auto-accepting file transfer: " + e.getMessage());
        }
    } else {
        System.out.println("DEBUG: Showing file transfer dialog");
        // Show confirmation dialog for direct file transfers
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(null,
                "Accept incoming file?\n" +
                "File: " + request.getFileName() + "\n" +
                "Size: " + formatFileSize(request.getFileSize()) + "\n\n" +
                "File will be saved to: " + downloadDirectory,
                "Incoming File Transfer",
                JOptionPane.YES_NO_OPTION);
            
            try {
                if (result == JOptionPane.YES_OPTION) {
                    acceptFileTransfer(request);
                } else {
                    rejectFileTransfer(request);
                }
            } catch (IOException e) {
                notifyConnectionListeners("Error responding to file transfer: " + e.getMessage());
            }
        });
    }
}

    private void acceptFileTransfer(FileTransferRequest request) throws IOException {
        // Create file transfer tracking
        File outputFile = new File(downloadDirectory, request.getFileName());
        // Handle duplicate filenames
        int counter = 1;
        String baseName = request.getFileName();
        while (outputFile.exists()) {
            int dotIndex = baseName.lastIndexOf('.');
            if (dotIndex > 0) {
                String name = baseName.substring(0, dotIndex);
                String extension = baseName.substring(dotIndex);
                outputFile = new File(downloadDirectory, name + "_" + counter + extension);
            } else {
                outputFile = new File(downloadDirectory, baseName + "_" + counter);
            }
            counter++;
        }
        
        FileTransfer transfer = new FileTransfer(
            request.getFileName(), 
            outputFile.getAbsolutePath(), 
            request.getFileSize(),
            request.getTransferId()
        );
        activeTransfers.put(request.getTransferId(), transfer);
        
        // Send acceptance
        TransferControlMessage accept = new TransferControlMessage(
            TransferControlMessage.Type.ACCEPT, request.getTransferId());
        outputStream.writeObject(accept);
        outputStream.flush();
        
        notifyConnectionListeners("Accepting file: " + request.getFileName());
    }
    
    private void rejectFileTransfer(FileTransferRequest request) throws IOException {
        // Send rejection
        TransferControlMessage reject = new TransferControlMessage(
            TransferControlMessage.Type.REJECT, request.getTransferId());
        outputStream.writeObject(reject);
        outputStream.flush();
        
        notifyConnectionListeners("Rejected file: " + request.getFileName());
    }
    
    private void handleFileChunk(FileChunk chunk) {
        FileTransfer transfer = activeTransfers.get(chunk.getTransferId());
        if (transfer != null && !transfer.isComplete()) {
            try {
                // Write chunk to file
                transfer.writeChunk(chunk.getData());
                
                // Update progress
                notifyTransferProgress(
                    chunk.getTransferId(), 
                    transfer.getFileName(),
                    transfer.getBytesReceived(), 
                    transfer.getTotalSize(),
                    false
                );
                
                // Check if transfer is complete
                if (transfer.isComplete()) {
                    transfer.close();
                    notifyConnectionListeners("File received successfully: " + transfer.getFileName());
                    notifyTransferProgress(
                        chunk.getTransferId(),
                        transfer.getFileName(),
                        transfer.getTotalSize(),
                        transfer.getTotalSize(),
                        false
                    );
                    activeTransfers.remove(chunk.getTransferId());
                    
                    // Show completion dialog
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                            "File received successfully!\n" +
                            "File: " + transfer.getFileName() + "\n" +
                            "Saved to: " + transfer.getFilePath(),
                            "Download Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                    });
                }
                
            } catch (IOException e) {
                notifyConnectionListeners("Error writing file chunk for " + transfer.getFileName() + ": " + e.getMessage());
                transfer.close();
                activeTransfers.remove(chunk.getTransferId());
            }
        }
    }

    public void sendTradeFiles(List<File> files, String tradeId) {
    if (!isConnected) {
        notifyConnectionListeners("Not connected to any peer");
        return;
    }
    
    connectionExecutor.submit(() -> {
        for (File file : files) {
            if (!file.exists() || !file.isFile()) {
                notifyConnectionListeners("File does not exist or is not a regular file: " + file.getName());
                continue;
            }
            
            String transferId = tradeId + "_" + file.getName();
            try {
                // Send file transfer request
                FileTransferRequest request = new FileTransferRequest(
                    file.getName(), 
                    file.length(),
                    transferId
                );
                
                outputStream.writeObject(request);
                outputStream.flush();
                
                notifyConnectionListeners("Sending trade file: " + file.getName() + " (" + 
                    formatFileSize(file.length()) + ")");
                
                // Wait for acceptance with timeout
                boolean accepted = waitForTransferAcceptance(transferId);
                
                if (accepted) {
                    // Start sending file
                    sendFileInChunks(file, transferId);
                } else {
                    notifyConnectionListeners("File transfer rejected or timed out: " + file.getName());
                }
                
            } catch (IOException e) {
                notifyConnectionListeners("Error sending trade file " + file.getName() + ": " + e.getMessage());
            }
        }
    });
}

private boolean waitForTransferAcceptance(String transferId) {
    try {
        // Set a timeout for acceptance
        clientSocket.setSoTimeout(10000); // 10 second timeout
        
        Object response = inputStream.readObject();
        clientSocket.setSoTimeout(0); // Reset timeout
        
        if (response instanceof TransferControlMessage) {
            TransferControlMessage control = (TransferControlMessage) response;
            return control.getType() == TransferControlMessage.Type.ACCEPT;
        }
    } catch (SocketTimeoutException e) {
        notifyConnectionListeners("Timeout waiting for file transfer acceptance: " + transferId);
    } catch (IOException | ClassNotFoundException e) {
        notifyConnectionListeners("Error waiting for transfer acceptance: " + e.getMessage());
    }
    return false;
}

     public void setAutoAcceptForTrade(String tradeId) {
        this.autoAcceptTransfers = true;
        this.currentTradeId = tradeId;
        
        // Auto-disable after 30 seconds in case something goes wrong
        Timer autoAcceptTimer = new Timer();
        autoAcceptTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                autoAcceptTransfers = false;
                currentTradeId = null;
            }
        }, 30000);
    }
    
    private void handleTransferControl(TransferControlMessage control) {
        // Handle transfer control messages (used for synchronization)
        if (control.getType() == TransferControlMessage.Type.COMPLETE) {
            notifyConnectionListeners("File transfer completed: " + control.getTransferId());
        }
    }
    
    // Trade methods (unchanged)
    public void sendTradeProposal(List<File> files) {
        if (!isConnected) {
            notifyConnectionListeners("Not connected to any peer");
            return;
        }
        
        try {
            List<TradeFile> tradeFiles = new ArrayList<>();
            long totalSize = 0;
            
            for (File file : files) {
                if (file.exists() && file.isFile()) {
                    tradeFiles.add(new TradeFile(file.getName(), file.length(), file.getAbsolutePath()));
                    totalSize += file.length();
                }
            }
            
            TradeProposal proposal = new TradeProposal(tradeFiles, totalSize, UUID.randomUUID().toString());
            outputStream.writeObject(proposal);
            outputStream.flush();
            
            notifyConnectionListeners("Sent trade proposal with " + tradeFiles.size() + " files");
            
        } catch (IOException e) {
            notifyConnectionListeners("Error sending trade proposal: " + e.getMessage());
        }
    }
    
    private void handleTradeProposal(TradeProposal proposal) {
        incomingTrades.add(proposal);
        notifyTradeListeners(proposal);
        notifyConnectionListeners("Incoming trade proposal with " + 
            proposal.getFiles().size() + " files from " + connectedPeer);
    }
    
    // Utility methods
    private String resolveHostname(String hostname) {
        try {
            if ("localhost".equalsIgnoreCase(hostname)) {
                return "127.0.0.1";
            }
            InetAddress address = InetAddress.getByName(hostname);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return hostname;
        }
    }
    
    public void disconnect() {
        isConnected = false;
        isHosting = false;
        connectedPeer = null;
        
        // Clear incoming trades and active transfers
        incomingTrades.clear();
        activeTransfers.clear();
        
        closeResource(clientSocket);
        closeResource(serverSocket);
        closeResource(outputStream);
        closeResource(inputStream);
        
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
        
        if (!connectionExecutor.isShutdown()) {
            connectionExecutor.shutdownNow();
        }
        
        notifyConnectionListeners("Disconnected");
    }
    
    private void closeResource(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    private void closeResource(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    private void closeResource(ServerSocket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    // In NetworkManager.java - update the sendMessage method with debug logging
public void sendMessage(String message) {
    if (!isConnected) {
        notifyConnectionListeners("Not connected to any peer");
        return;
    }
    
    try {
        System.out.println("DEBUG [SEND]: Sending message: " + message);
        outputStream.writeObject(message);
        outputStream.flush();
        System.out.println("DEBUG [SEND]: Message sent successfully: " + message);
    } catch (IOException e) {
        System.out.println("DEBUG [SEND ERROR]: Error sending message: " + e.getMessage());
        notifyConnectionListeners("Error sending message: " + e.getMessage());
        disconnect();
    }
}
    
    // Listener management
    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }
    
    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }
    
    public void addTradeListener(TradeListener listener) {
        if (!connectionListeners.contains(listener)) {
            connectionListeners.add(listener);
        }
    }
    
    private void notifyConnectionListeners(String message) {
        for (ConnectionListener listener : connectionListeners) {
            listener.onConnectionEvent(message);
        }
    }
    
    private void notifyTradeListeners(TradeProposal proposal) {
        for (ConnectionListener listener : connectionListeners) {
            if (listener instanceof TradeListener) {
                ((TradeListener) listener).onIncomingTrade(proposal);
            }
        }
    }
    
    private void notifyTransferProgress(String transferId, String fileName, long bytesTransferred, long totalBytes, boolean isUpload) {
        for (ConnectionListener listener : connectionListeners) {
            if (listener instanceof TransferProgressListener) {
                ((TransferProgressListener) listener).onTransferProgress(
                    transferId, fileName, bytesTransferred, totalBytes, isUpload);
            }
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    // Getters
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isHosting() {
        return isHosting;
    }
    
    public String getConnectedPeer() {
        return connectedPeer;
    }
    
    public int getPort() {
        return port;
    }
    
    public List<TradeProposal> getIncomingTrades() {
        return new ArrayList<>(incomingTrades);
    }
    
    public String getDownloadDirectory() {
        return downloadDirectory;
    }
}
package com.trader.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

import com.trader.network.NetworkManager;
import com.trader.network.TradeFile;
import com.trader.network.TradeListener;
import com.trader.network.TradeProposal;

public class TradePanel extends JPanel implements TradeListener {
    private NetworkManager networkManager;
    private JTable tradeTable;
    private DefaultTableModel tableModel;
    private JButton removeButton;
    private JButton clearButton;
    private JButton confirmTradeButton;
    private List<File> tradeFiles;
    
    // Incoming trades
    private List<TradeProposal> incomingTrades;
    private JButton viewIncomingTradesButton;

    private Map<String, List<File>> pendingTrades; // Track trades we've sent
private String currentTradeId;
    
    public TradePanel(NetworkManager networkManager) {
    this.networkManager = networkManager;
    this.tradeFiles = new ArrayList<>();
    this.incomingTrades = new ArrayList<>();
    this.pendingTrades = new HashMap<>();
    this.networkManager.addTradeListener(this);
    initializeUI();
    setupDragAndDrop();
}
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create trade table
        setupTradeTable();
        
        // Create control panel - RESTORED BUTTONS
        setupControlPanel();
        
        // Add components with proper layout
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(tradeTable), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add incoming trades button at the top
        JPanel incomingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        viewIncomingTradesButton = new JButton("View Incoming Trades (0)");
        viewIncomingTradesButton.setEnabled(false);
        viewIncomingTradesButton.addActionListener(e -> showIncomingTradesDialog());
        
        incomingPanel.add(viewIncomingTradesButton);
        headerPanel.add(incomingPanel, BorderLayout.NORTH);
        
        return headerPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        
        // Drop hint at the very bottom
        JLabel dropHint = new JLabel("   Drop files here to add to trade", JLabel.LEFT);
        dropHint.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        dropHint.setFont(new Font("Arial", Font.ITALIC, 12));
        dropHint.setForeground(Color.GRAY);
        
        // Control buttons above the drop hint
        JPanel controlPanel = setupControlPanel();
        
        footerPanel.add(controlPanel, BorderLayout.NORTH);
        footerPanel.add(dropHint, BorderLayout.SOUTH);
        
        return footerPanel;
    }
    
    private JPanel setupControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        removeButton = new JButton("Remove Selected");
        clearButton = new JButton("Clear All");
        confirmTradeButton = new JButton("Confirm Trade");
        
        removeButton.setEnabled(false);
        
        removeButton.addActionListener(e -> removeSelectedFile());
        clearButton.addActionListener(e -> clearAllFiles());
        confirmTradeButton.addActionListener(e -> confirmTrade());
        
        controlPanel.add(removeButton);
        controlPanel.add(clearButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(confirmTradeButton);
        
        return controlPanel;
    }
    
    private void setupDragAndDrop() {
        setTransferHandler(new TradePanelTransferHandler());
        tradeTable.setDropMode(DropMode.ON);
        tradeTable.setTransferHandler(new TradePanelTransferHandler());
    }
    
    private void setupTradeTable() {
        String[] columns = {"File Name", "Size", "Status", "Path"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        
        tradeTable = new JTable(tableModel);
        tradeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tradeTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = tradeTable.getSelectedRow() != -1;
            removeButton.setEnabled(hasSelection);
        });
        
        tradeTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        tradeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        tradeTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        tradeTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        
        tradeTable.setToolTipText("Files pending for trade - drag files here or drop from file explorer");
    }
    
    private void removeSelectedFile() {
        int selectedRow = tradeTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            tradeFiles.remove(selectedRow);
            updateTradeStatus();
        }
    }
    
    private void clearAllFiles() {
        if (tableModel.getRowCount() > 0) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Remove all " + tableModel.getRowCount() + " file(s) from trade?",
                "Clear All Files",
                JOptionPane.YES_NO_OPTION);
                
            if (result == JOptionPane.YES_OPTION) {
                tableModel.setRowCount(0);
                tradeFiles.clear();
                updateTradeStatus();
            }
        }
    }
    
    private void confirmTrade() {
    if (tableModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, 
            "No files added to trade! Please add files first.", 
            "Empty Trade", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    if (!networkManager.isConnected()) {
        JOptionPane.showMessageDialog(this, 
            "Not connected to any peer! Please connect first.", 
            "No Connection", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    StringBuilder summary = new StringBuilder();
    summary.append("Ready to trade ").append(tableModel.getRowCount()).append(" file(s):\n\n");
    
    long totalSize = 0;
    for (int i = 0; i < tableModel.getRowCount(); i++) {
        String fileName = (String) tableModel.getValueAt(i, 0);
        String fileSize = (String) tableModel.getValueAt(i, 1);
        summary.append("• ").append(fileName).append(" (").append(fileSize).append(")\n");
        
        if (i < tradeFiles.size()) {
            totalSize += tradeFiles.get(i).length();
        }
    }
    
    summary.append("\nTotal size: ").append(formatFileSize(totalSize));
    summary.append("\n\nSend trade proposal to ").append(networkManager.getConnectedPeer()).append("?");
    
    int result = JOptionPane.showConfirmDialog(this, 
        summary.toString(),
        "Confirm Trade", 
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    
    if (result == JOptionPane.YES_OPTION) {
        // Generate a trade ID
        currentTradeId = "trade_" + System.currentTimeMillis();
        
        // Store the files for this trade
        pendingTrades.put(currentTradeId, new ArrayList<>(tradeFiles));
        
        // Send trade proposal
        networkManager.sendTradeProposal(tradeFiles);
        
        // Update status to show proposal sent
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("Waiting...", i, 2);
        }
        
        JOptionPane.showMessageDialog(this, 
            "Trade proposal sent to " + networkManager.getConnectedPeer() + "!\n" +
            "Waiting for them to accept the trade...",
            "Trade Proposed", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}
    
    public void addFileToTrade(File file) {
        if (file == null || file.isDirectory() || !file.exists()) {
            return;
        }
        
        for (File tradeFile : tradeFiles) {
            if (tradeFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                return;
            }
        }
        
        tradeFiles.add(file);
        
        String size = formatFileSize(file.length());
        String status = "Pending";
        String path = file.getParent();
        
        tableModel.addRow(new Object[]{file.getName(), size, status, path});
        
        int newRow = tableModel.getRowCount() - 1;
        tradeTable.scrollRectToVisible(tradeTable.getCellRect(newRow, 0, true));
        
        updateTradeStatus();
    }
    
    private void handleDroppedFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        
        int addedCount = 0;
        for (File file : files) {
            if (file.isFile() && file.exists()) {
                boolean isDuplicate = false;
                for (File tradeFile : tradeFiles) {
                    if (tradeFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                if (!isDuplicate) {
                    tradeFiles.add(file);
                    String size = formatFileSize(file.length());
                    String path = file.getParent();
                    tableModel.addRow(new Object[]{file.getName(), size, "Pending", path});
                    addedCount++;
                }
            }
        }
        
        if (addedCount > 0) {
            updateTradeStatus();
        }
    }

        private void startTradeAcceptanceListener() {
        new Thread(() -> {
            try {
                // For now, we'll just wait a bit and then send files
                // In a real implementation, you'd listen for an acceptance message
                Thread.sleep(2000);
            
                // Send the files (in a real app, this would be triggered by acceptance message)
                SwingUtilities.invokeLater(() -> {
                    // Update status to show files are being sent
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        tableModel.setValueAt("Sending...", i, 2);
                    }
                
                    // Send files
                    String tradeId = "trade_" + System.currentTimeMillis();
                    networkManager.sendTradeFiles(tradeFiles, tradeId);
                });
            
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private void updateTradeStatus() {
        int fileCount = tableModel.getRowCount();
        confirmTradeButton.setEnabled(fileCount > 0 && networkManager.isConnected());
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    // TradeListener implementation
    @Override
    public void onConnectionEvent(String message) {
        // Handle connection events if needed
    }
    
    @Override
public void onIncomingTrade(TradeProposal proposal) {
    SwingUtilities.invokeLater(() -> {
        // Add to incoming trades if not already present
        boolean alreadyExists = incomingTrades.stream()
            .anyMatch(p -> p.getProposalId().equals(proposal.getProposalId()));
        
        if (!alreadyExists) {
            incomingTrades.add(proposal);
            
            // Update incoming trades button
            updateIncomingTradesButton();
            
            // Show notification
            showIncomingTradeNotification(proposal);
        }
    });
}
    
    private void updateIncomingTradesButton() {
        int count = incomingTrades.size();
        viewIncomingTradesButton.setText("View Incoming Trades (" + count + ")");
        viewIncomingTradesButton.setEnabled(count > 0);
    }
    
    private void showIncomingTradeNotification(TradeProposal proposal) {
        // Optional: Show a non-modal notification
        System.out.println("Incoming trade received: " + proposal.getFiles().size() + " files");
        
        // You could add a subtle visual indicator instead of a dialog
        viewIncomingTradesButton.setBackground(new Color(255, 255, 200)); // Light yellow
        Timer timer = new Timer(2000, e -> viewIncomingTradesButton.setBackground(null));
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showIncomingTradesDialog() {
    if (incomingTrades.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "No incoming trades", 
            "Incoming Trades", 
            JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Incoming Trades", true);
    dialog.setLayout(new BorderLayout());
    dialog.setSize(600, 500);
    dialog.setLocationRelativeTo(this);
    
    // Create list of trades with better formatting
    DefaultListModel<String> listModel = new DefaultListModel<>();
    for (int i = 0; i < incomingTrades.size(); i++) {
        TradeProposal proposal = incomingTrades.get(i);
        String display = String.format("Trade #%d - %d files (%s) from %s", 
            (i + 1), 
            proposal.getFiles().size(), 
            formatFileSize(proposal.getTotalSize()),
            networkManager.getConnectedPeer());
        listModel.addElement(display);
    }
    
    JList<String> tradeList = new JList<>(listModel);
    tradeList.setFont(new Font("Monospaced", Font.PLAIN, 12));
    JScrollPane scrollPane = new JScrollPane(tradeList);
    
    // Details panel with better formatting
    JTextArea detailsArea = new JTextArea();
    detailsArea.setEditable(false);
    detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    detailsArea.setBackground(new Color(240, 240, 240));
    JScrollPane detailsScroll = new JScrollPane(detailsArea);
    
    // Update details when selection changes
    tradeList.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            int index = tradeList.getSelectedIndex();
            if (index >= 0 && index < incomingTrades.size()) {
                TradeProposal proposal = incomingTrades.get(index);
                StringBuilder details = new StringBuilder();
                details.append("TRADE PROPOSAL #").append(index + 1).append("\n");
                details.append("════════════════════════════════════════\n\n");
                details.append("Files to receive:\n\n");
                
                for (TradeFile file : proposal.getFiles()) {
                    details.append("  • ").append(file.getFileName())
                           .append("\n      Size: ").append(formatFileSize(file.getFileSize()))
                           .append("\n      Path: ").append(file.getFilePath())
                           .append("\n\n");
                }
                
                details.append("════════════════════════════════════════\n");
                details.append("Total: ").append(proposal.getFiles().size())
                       .append(" files, ").append(formatFileSize(proposal.getTotalSize()));
                
                detailsArea.setText(details.toString());
                detailsArea.setCaretPosition(0); // Scroll to top
            }
        }
    });
    
    // Buttons
    JPanel buttonPanel = new JPanel();
    JButton acceptButton = new JButton("Accept Trade");
    JButton rejectButton = new JButton("Reject Trade");
    JButton closeButton = new JButton("Close");
    
    acceptButton.addActionListener(e -> {
        int index = tradeList.getSelectedIndex();
        if (index >= 0 && index < incomingTrades.size()) {
            TradeProposal proposal = incomingTrades.get(index);
            acceptIncomingTrade(proposal);
            
            // Remove from both the list model and the incomingTrades list
            listModel.remove(index);
            incomingTrades.remove(proposal); // Remove by object instead of index
            
            updateIncomingTradesButton();
            if (incomingTrades.isEmpty()) {
                dialog.dispose();
            } else {
                // Select the next item if available
                if (index < listModel.size()) {
                    tradeList.setSelectedIndex(index);
                } else if (listModel.size() > 0) {
                    tradeList.setSelectedIndex(listModel.size() - 1);
                }
            }
        } else {
            JOptionPane.showMessageDialog(dialog, 
                "Please select a trade to accept.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    });
    
    rejectButton.addActionListener(e -> {
        int index = tradeList.getSelectedIndex();
        if (index >= 0 && index < incomingTrades.size()) {
            TradeProposal proposal = incomingTrades.get(index);
            
            // Remove from both the list model and the incomingTrades list
            listModel.remove(index);
            incomingTrades.remove(proposal); // Remove by object instead of index
            
            updateIncomingTradesButton();
            if (incomingTrades.isEmpty()) {
                dialog.dispose();
            } else {
                // Select the next item if available
                if (index < listModel.size()) {
                    tradeList.setSelectedIndex(index);
                } else if (listModel.size() > 0) {
                    tradeList.setSelectedIndex(listModel.size() - 1);
                }
            }
        } else {
            JOptionPane.showMessageDialog(dialog, 
                "Please select a trade to reject.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    });
    
    closeButton.addActionListener(e -> dialog.dispose());
    
    buttonPanel.add(acceptButton);
    buttonPanel.add(rejectButton);
    buttonPanel.add(closeButton);
    
    // Split pane for list and details
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, detailsScroll);
    splitPane.setResizeWeight(0.4);
    splitPane.setDividerLocation(0.4);
    
    dialog.add(splitPane, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    
    // Select first item by default and ensure details are shown
    if (!incomingTrades.isEmpty()) {
        tradeList.setSelectedIndex(0);
    }
    
    dialog.setVisible(true);
}
    
    private void acceptIncomingTrade(TradeProposal proposal) {
    if (proposal == null) {
        JOptionPane.showMessageDialog(this, 
            "Invalid trade proposal.", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    System.out.println("DEBUG [TRADE]: Accepting trade: " + proposal.getProposalId());
    
    // Set auto-accept for this trade
    networkManager.setAutoAcceptForTrade(proposal.getProposalId());
    System.out.println("DEBUG [TRADE]: Auto-accept enabled for trade: " + proposal.getProposalId());
    
    // Send acceptance message to peer
    String acceptanceMessage = "TRADE_ACCEPTED:" + proposal.getProposalId();
    System.out.println("DEBUG [TRADE]: Sending acceptance message: " + acceptanceMessage);
    networkManager.sendMessage(acceptanceMessage);
    
    StringBuilder fileList = new StringBuilder();
    fileList.append("Accepted trade! Downloading ").append(proposal.getFiles().size()).append(" file(s):\n\n");
    
    for (TradeFile tradeFile : proposal.getFiles()) {
        fileList.append("• ").append(tradeFile.getFileName())
               .append(" (").append(formatFileSize(tradeFile.getFileSize())).append(")\n");
    }
    
    fileList.append("\nFiles will be saved to: ").append(networkManager.getDownloadDirectory());
    
    JOptionPane.showMessageDialog(this, 
        fileList.toString(),
        "Trade Accepted", 
        JOptionPane.INFORMATION_MESSAGE);
    
    System.out.println("DEBUG [TRADE]: Trade acceptance UI completed");
}

@Override
public void onTradeAccepted(String tradeId) {
    SwingUtilities.invokeLater(() -> {
        // Check if we have a pending trade with this ID
        List<File> tradeFilesToSend = pendingTrades.get(tradeId);
        if (tradeFilesToSend != null && !tradeFilesToSend.isEmpty()) {
            // Update UI to show transfer starting
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt("Sending...", i, 2);
            }
            
            // Start sending files
            networkManager.sendTradeFiles(tradeFilesToSend, tradeId);
            
            JOptionPane.showMessageDialog(this, 
                "Trade accepted! Starting file transfer...",
                "Transfer Starting", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Remove from pending trades
            pendingTrades.remove(tradeId);
        } else {
            System.err.println("No pending trade found for ID: " + tradeId);
        }
    });
}
    
    // TransferHandler for drop support
    private class TradePanelTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }
        
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            
            try {
                @SuppressWarnings("unchecked")
                List<File> files = (List<File>) support.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);
                
                handleDroppedFiles(files);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    // Public methods
    public List<File> getTradeFiles() {
        return new ArrayList<>(tradeFiles);
    }
    
    public int getTradeFileCount() {
        return tradeFiles.size();
    }
    
    public long getTotalTradeSize() {
        return tradeFiles.stream().mapToLong(File::length).sum();
    }
    
    public boolean hasFiles() {
        return !tradeFiles.isEmpty();
    }
    
    public void clearTrade() {
        tableModel.setRowCount(0);
        tradeFiles.clear();
        updateTradeStatus();
    }
}
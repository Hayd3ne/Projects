package com.trader.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.trader.network.ConnectionListener;
import com.trader.network.NetworkManager;
import com.trader.network.TransferProgressListener;

public class MainFrame extends JFrame implements ConnectionListener {
    private FileExplorerPanel fileExplorerPanel;
    private TradePanel tradePanel;
    private NetworkManager networkManager;
    
    // UI Components for network status
    private JLabel statusLabel;
    private JButton connectButton;
    private JButton hostButton;
    private JButton disconnectButton;
    
    public MainFrame() {
        initializeNetwork();
        initializeUI();
    }
    
    private void initializeNetwork() {
        networkManager = new NetworkManager();
        networkManager.addConnectionListener(this);
    }
    
    private void initializeUI() {
        setTitle("File Trader - P2P File Sharing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 700));
        setMinimumSize(new Dimension(800, 500));
        
        // Create main layout
        setupLayout();
        
        // Center on screen
        pack();
        setLocationRelativeTo(null);
    }
    
    private void setupLayout() {
        // Create main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5); // Equal split
        splitPane.setDividerLocation(0.5); // Start at 50%
        splitPane.setOneTouchExpandable(true);
        
        // Create panels with cross-references
        tradePanel = new TradePanel(networkManager);
        fileExplorerPanel = new FileExplorerPanel(networkManager, tradePanel);
        
        // Add panels to split pane
        splitPane.setLeftComponent(createPanelWithTitle("Your Files", fileExplorerPanel));
        splitPane.setRightComponent(createPanelWithTitle("Trade Window", tradePanel));
        
        // Add menu bar and status bar
        setJMenuBar(createMenuBar());
        add(createStatusBar(), BorderLayout.SOUTH);
        
        // Set content
        setContentPane(splitPane);
    }
    
    private JPanel createPanelWithTitle(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            networkManager.disconnect();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        
        // Connection Menu
        JMenu connectionMenu = new JMenu("Connection");
        JMenuItem connectItem = new JMenuItem("Connect to Peer...");
        JMenuItem hostItem = new JMenuItem("Host Session...");
        JMenuItem disconnectItem = new JMenuItem("Disconnect");
        
        connectItem.addActionListener(e -> showConnectDialog());
        hostItem.addActionListener(e -> showHostDialog());
        disconnectItem.addActionListener(e -> disconnect());
        
        connectionMenu.add(connectItem);
        connectionMenu.add(hostItem);
        connectionMenu.addSeparator();
        connectionMenu.add(disconnectItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(connectionMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        
        // Status label
        statusLabel = new JLabel("Not connected");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Progress label (for file transfers)
        JLabel progressLabel = new JLabel("");
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        progressLabel.setForeground(Color.BLUE);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(progressLabel);
        
        // Connection buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        connectButton = new JButton("Connect");
        hostButton = new JButton("Host");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        
        connectButton.addActionListener(e -> showConnectDialog());
        hostButton.addActionListener(e -> showHostDialog());
        disconnectButton.addActionListener(e -> disconnect());
        
        connectButton.setFont(new Font("Arial", Font.PLAIN, 11));
        hostButton.setFont(new Font("Arial", Font.PLAIN, 11));
        disconnectButton.setFont(new Font("Arial", Font.PLAIN, 11));
        
        buttonPanel.add(connectButton);
        buttonPanel.add(hostButton);
        buttonPanel.add(disconnectButton);
        
        statusBar.add(statusPanel, BorderLayout.WEST);
        statusBar.add(buttonPanel, BorderLayout.EAST);
        
        // Add transfer progress listener
        networkManager.addConnectionListener(new TransferProgressListener() {
            @Override
            public void onConnectionEvent(String message) {
                // Handled by main listener
            }
            
            @Override
            public void onTransferProgress(String transferId, String fileName, long bytesTransferred, long totalBytes, boolean isUpload) {
                SwingUtilities.invokeLater(() -> {
                    int percent = (int) ((bytesTransferred * 100) / totalBytes);
                    String direction = isUpload ? "Uploading" : "Downloading";
                    progressLabel.setText(String.format("%s: %s - %d%% (%s/%s)", 
                        direction, fileName, percent, 
                        formatFileSize(bytesTransferred), formatFileSize(totalBytes)));
                    
                    // Clear progress when transfer completes
                    if (bytesTransferred >= totalBytes) {
                        Timer timer = new Timer(3000, e -> progressLabel.setText(""));
                        timer.setRepeats(false);
                        timer.start();
                    }
                });
            }
        });
        
        return statusBar;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    private void showConnectDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JTextField ipField = new JTextField("localhost");
        JTextField portField = new JTextField("8080");
        
        panel.add(new JLabel("IP Address:"));
        panel.add(ipField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Connect to Peer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String ip = ipField.getText().trim();
            String portText = portField.getText().trim();
            
            if (ip.isEmpty() || portText.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter both IP address and port", 
                    "Invalid Input", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                int port = Integer.parseInt(portText);
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException("Port out of range");
                }
                
                // Disconnect first if already connected
                if (networkManager.isConnected()) {
                    networkManager.disconnect();
                }
                
                // Attempt connection
                boolean success = networkManager.connectToPeer(ip, port);
                if (!success) {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to connect to " + ip + ":" + port, 
                        "Connection Failed", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid port number (1-65535)", 
                    "Invalid Port", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void showHostDialog() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
        
        JTextField portField = new JTextField("8080");
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Host Session", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String portText = portField.getText().trim();
            
            if (portText.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a port number", 
                    "Invalid Input", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                int port = Integer.parseInt(portText);
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException("Port out of range");
                }
                
                // Disconnect first if already connected/hosting
                if (networkManager.isConnected() || networkManager.isHosting()) {
                    networkManager.disconnect();
                }
                
                // Start hosting
                boolean success = networkManager.hostSession(port);
                if (!success) {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to host on port " + port, 
                        "Hosting Failed", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid port number (1-65535)", 
                    "Invalid Port", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void disconnect() {
        networkManager.disconnect();
        updateConnectionStatus();
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, 
            "File Trader v1.0\nP2P File Sharing Application\n\n" +
            "Network Status: " + (networkManager.isConnected() ? "Connected" : "Disconnected") + 
            (networkManager.isConnected() ? "\nConnected to: " + networkManager.getConnectedPeer() : ""), 
            "About", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ConnectionListener implementation
    @Override
    public void onConnectionEvent(String message) {
        // Update UI on EDT
        SwingUtilities.invokeLater(() -> {
            System.out.println("Network: " + message); // Also log to console
            
            // Update status label
            statusLabel.setText(message);
            
            // Update connection status
            updateConnectionStatus();
            
            // Show important messages as dialogs
            if (message.contains("Successfully connected") || 
                message.contains("Accepted connection")) {
                JOptionPane.showMessageDialog(this, 
                    message, "Connection Established", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else if (message.contains("Failed") || 
                       message.contains("Error") || 
                       message.contains("closed")) {
                // Only show error dialogs for significant errors
                if (!message.contains("Connection closed by peer") && 
                    !message.contains("Waiting for incoming connections")) {
                    JOptionPane.showMessageDialog(this, 
                        message, "Network Event", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }
    
    private void updateConnectionStatus() {
        boolean connected = networkManager.isConnected();
        boolean hosting = networkManager.isHosting();
        
        // Update button states
        connectButton.setEnabled(!connected);
        hostButton.setEnabled(!connected && !hosting);
        disconnectButton.setEnabled(connected || hosting);
        
        // Update window title with connection status
        String title = "File Trader - P2P File Sharing";
        if (connected) {
            title += " - Connected to " + networkManager.getConnectedPeer();
        } else if (hosting) {
            title += " - Hosting on port " + networkManager.getPort();
        } else {
            title += " - Not connected";
        }
        setTitle(title);
        
        // Update status label color
        if (connected) {
            statusLabel.setForeground(new Color(0, 128, 0)); // Green
        } else if (hosting) {
            statusLabel.setForeground(new Color(0, 0, 128)); // Blue
        } else {
            statusLabel.setForeground(Color.RED);
        }
    }
    
    public NetworkManager getNetworkManager() {
        return networkManager;
    }
    
    // Override window closing to cleanup network resources
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            networkManager.disconnect();
        }
        super.processWindowEvent(e);
    }
}
package com.trader.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.trader.network.NetworkManager;

public class FileExplorerPanel extends JPanel {
    private NetworkManager networkManager;
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private JButton refreshButton;
    private JButton addToTradeButton;
    private JButton upButton;
    private JLabel currentPathLabel;
    private File currentDirectory;
    private Map<File, Boolean> loadedDirectories;
    private TradePanel tradePanel;
    
    // File type icons
    private Icon folderIcon;
    private Icon fileIcon;
    private Icon diskIcon;
    
    public FileExplorerPanel(NetworkManager networkManager, TradePanel tradePanel) {
        this.networkManager = networkManager;
        this.tradePanel = tradePanel;
        this.loadedDirectories = new HashMap<>();
        loadIcons();
        initializeUI();
        setupDragAndDrop();
    }
    
    private void loadIcons() {
        folderIcon = UIManager.getIcon("FileView.directoryIcon");
        fileIcon = UIManager.getIcon("FileView.fileIcon");
        diskIcon = UIManager.getIcon("FileView.hardDriveIcon");
        
        if (folderIcon == null) folderIcon = new DefaultTreeCellRenderer().getClosedIcon();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create toolbar and path display
        //setupToolbar();
        
        // Create file tree
        setupFileTree();
        
        // Add components
        add(createNorthPanel(), BorderLayout.NORTH);
        add(new JScrollPane(fileTree), BorderLayout.CENTER);
        
        // Load working directory instead of user home
        loadWorkingDirectory();
    }
    
    private void setupDragAndDrop() {
        // Enable drag from file tree
        fileTree.setDragEnabled(true);
        fileTree.setTransferHandler(new FileTreeTransferHandler());
    }
    
    private void loadWorkingDirectory() {
        File workingDir = new File(System.getProperty("user.dir"));
        setCurrentDirectory(workingDir);
        loadDirectoryIntoView(workingDir);
    }
    
    private JPanel createNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createPathPanel(), BorderLayout.NORTH);
        northPanel.add(createToolbar(), BorderLayout.CENTER);
        return northPanel;
    }
    
    private JPanel createPathPanel() {
        JPanel pathPanel = new JPanel(new BorderLayout());
        pathPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        currentPathLabel = new JLabel();
        currentPathLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        pathPanel.add(new JLabel("Current Path: "), BorderLayout.WEST);
        pathPanel.add(currentPathLabel, BorderLayout.CENTER);
        
        return pathPanel;
    }
    
    private JToolBar createToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        upButton = new JButton("Up");
        refreshButton = new JButton("Refresh");
        addToTradeButton = new JButton("Add to Trade");
        addToTradeButton.setEnabled(false);
        
        upButton.setText("↑");
        upButton.setToolTipText("Go up one directory");
        refreshButton.setText("↻");
        refreshButton.setToolTipText("Refresh current directory");
        
        upButton.addActionListener(e -> goUpDirectory());
        refreshButton.addActionListener(e -> refreshCurrentDirectory());
        addToTradeButton.addActionListener(e -> addSelectedToTrade());
        
        toolBar.add(upButton);
        toolBar.addSeparator();
        toolBar.add(refreshButton);
        toolBar.addSeparator();
        toolBar.add(addToTradeButton);
        
        return toolBar;
    }
    
    private void setupFileTree() {
        // Create root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("File System");
        treeModel = new DefaultTreeModel(root);
        fileTree = new JTree(treeModel);
        
        // Custom cell renderer for icons
        fileTree.setCellRenderer(new FileTreeCellRenderer());
        
        // Configure tree appearance
        fileTree.setShowsRootHandles(true);
        fileTree.setRootVisible(false);
        
        // Add expansion listener for lazy loading
        fileTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                loadChildren(node);
            }
            
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                // Optional: Implement if needed
            }
        });
        
        // Add selection listener
        fileTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof File) {
                File selectedFile = (File) node.getUserObject();
                addToTradeButton.setEnabled(!selectedFile.isDirectory());
                
                // Update current directory if a directory is selected
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(selectedFile);
                }
            } else {
                addToTradeButton.setEnabled(false);
            }
        });
        
        // Double-click to navigate into directories
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node.getUserObject() instanceof File) {
                            File file = (File) node.getUserObject();
                            if (file.isDirectory()) {
                                setCurrentDirectory(file);
                                loadDirectoryIntoView(file);
                            } else {
                                // Double-click on file adds it to trade
                                addFileToTrade(file);
                            }
                        }
                    }
                }
            }
        });
    }
    
    private void setCurrentDirectory(File directory) {
        this.currentDirectory = directory;
        currentPathLabel.setText(directory.getAbsolutePath());
        upButton.setEnabled(directory.getParentFile() != null);
    }
    
    private void loadDirectoryIntoView(File directory) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.removeAllChildren();
        
        // Add a node for the current directory
        DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(directory);
        root.add(dirNode);
        
        // Load the directory contents
        loadChildren(dirNode);
        
        // Expand the root to show the current directory
        fileTree.expandPath(new TreePath(root));
        fileTree.expandPath(new TreePath(new Object[]{root, dirNode}));
        
        treeModel.reload();
    }
    
    private void loadChildren(DefaultMutableTreeNode parentNode) {
        Object userObject = parentNode.getUserObject();
        if (!(userObject instanceof File)) return;
        
        File parentFile = (File) userObject;
        
        // Check if we've already loaded this directory
        if (loadedDirectories.containsKey(parentFile) && loadedDirectories.get(parentFile)) {
            return;
        }
        
        // Remove any "Loading..." placeholder
        if (parentNode.getChildCount() == 1) {
            TreeNode firstChild = parentNode.getChildAt(0);
            if (firstChild instanceof DefaultMutableTreeNode) {
                Object childObj = ((DefaultMutableTreeNode) firstChild).getUserObject();
                if ("Loading...".equals(childObj)) {
                    parentNode.remove(0);
                }
            }
        }
        
        // Load directory contents
        if (parentFile.isDirectory()) {
            File[] children = parentFile.listFiles();
            
            if (children != null) {
                // Sort: directories first, then files, both alphabetically
                java.util.Arrays.sort(children, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                });
                
                for (File child : children) {
                    // Skip hidden files
                    if (child.isHidden() && !shouldShowHiddenFiles()) continue;
                    
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
                    parentNode.add(childNode);
                    
                    // If it's a directory, add a dummy node to make it expandable
                    if (child.isDirectory()) {
                        childNode.add(new DefaultMutableTreeNode("Loading..."));
                    }
                }
            }
        }
        
        loadedDirectories.put(parentFile, true);
        treeModel.nodeStructureChanged(parentNode);
    }
    
    private boolean shouldShowHiddenFiles() {
        return false;
    }
    
    private void goUpDirectory() {
        if (currentDirectory != null && currentDirectory.getParentFile() != null) {
            setCurrentDirectory(currentDirectory.getParentFile());
            loadDirectoryIntoView(currentDirectory);
        }
    }
    
    private void refreshCurrentDirectory() {
        if (currentDirectory != null) {
            // Clear loaded state for this directory
            loadedDirectories.remove(currentDirectory);
            loadDirectoryIntoView(currentDirectory);
        }
    }
    
    private void addSelectedToTrade() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (node != null && node.getUserObject() instanceof File) {
            File selectedFile = (File) node.getUserObject();
            addFileToTrade(selectedFile);
        }
    }
    
    private void addFileToTrade(File file) {
        if (file == null || file.isDirectory()) {
            return; // Silently ignore directories and null files
        }
        
        if (!file.exists()) {
            return; // Silently ignore non-existent files
        }
        
        // Use the direct reference to tradePanel
        if (tradePanel != null) {
            tradePanel.addFileToTrade(file);
        }
    }
    
    // Custom TransferHandler for drag and drop
    private class FileTreeTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JTree) {
                JTree tree = (JTree) c;
                TreePath selectionPath = tree.getSelectionPath();
                if (selectionPath != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                    if (node.getUserObject() instanceof File) {
                        File selectedFile = (File) node.getUserObject();
                        if (!selectedFile.isDirectory()) {
                            List<File> fileList = new ArrayList<>();
                            fileList.add(selectedFile);
                            return new FileListTransferable(fileList);
                        }
                    }
                }
            }
            return null;
        }
    }
    
    // Transferable for file lists
    private static class FileListTransferable implements Transferable {
        private List<File> fileList;
        
        public FileListTransferable(List<File> fileList) {
            this.fileList = fileList;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.javaFileListFlavor };
        }
        
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return fileList;
        }
    }
    
    // Custom tree cell renderer for file icons
    private class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
                                                     boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            
            if (userObject instanceof File) {
                File file = (File) userObject;
                
                // Set icon based on file type
                if (file.isDirectory()) {
                    setIcon(folderIcon);
                } else {
                    setIcon(fileIcon);
                }
                
                // Set text with file name and additional info
                String displayText = file.getName();
                if (file.isDirectory()) {
                    displayText += " [DIR]";
                } else {
                    displayText += " (" + formatFileSize(file.length()) + ")";
                }
                
                setText(displayText);
                setToolTipText(createTooltipText(file));
            } else if ("Loading...".equals(userObject)) {
                setIcon(null);
                setText("Loading...");
            } else {
                setIcon(diskIcon);
            }
            
            return this;
        }
        
        private String createTooltipText(File file) {
            StringBuilder tooltip = new StringBuilder();
            tooltip.append("<html><b>").append(file.getName()).append("</b><br>");
            
            if (file.isDirectory()) {
                tooltip.append("Directory<br>");
            } else {
                tooltip.append("File Size: ").append(formatFileSize(file.length())).append("<br>");
            }
            
            tooltip.append("Modified: ").append(dateFormat.format(new Date(file.lastModified()))).append("<br>");
            tooltip.append("Path: ").append(file.getAbsolutePath()).append("</html>");
            
            return tooltip.toString();
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    public File getSelectedFile() {
        TreePath selectionPath = fileTree.getSelectionPath();
        if (selectionPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
            if (node.getUserObject() instanceof File) {
                return (File) node.getUserObject();
            }
        }
        return null;
    }
    
    public File getCurrentDirectory() {
        return currentDirectory;
    }
}
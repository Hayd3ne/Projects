package mao.src.main.java.com.mao;

import javax.swing.JFrame;
import java.awt.*;
import javax.swing.SwingUtilities;

public class Config {
    public Config() {
        
    }

    public static void applySettings() {
        SwingUtilities.invokeLater(() -> {
            try {
                // UI Looks to setup later
            } catch (Exception e) {
                e.printStackTrace();
            }
            SwingUtilities.updateComponentTreeUI(JFrame.getFrames()[0]);
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        });
    }
}

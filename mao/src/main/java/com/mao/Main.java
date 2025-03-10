package mao.src.main.java.com.mao;

import javax.swing.SwingUtilities;

// Dont code anything here, all this is meant to do is start the MainFrame and everything branches from there.

public class Main {
    public static void main( String[] args ) {
    SwingUtilities.invokeLater(() -> { 
            try {
                new MainFrame(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

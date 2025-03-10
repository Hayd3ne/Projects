package mao.src.main.java.com.mao;

import javax.swing.SwingUtilities;

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

package mao.src.main.java.com.mao;

import javax.swing.JFrame;

/*
 * Notes:
 * Everything should stem from this file
 */

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Mao");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);

        Config.applySettings();

        setVisible(true);
    }
}

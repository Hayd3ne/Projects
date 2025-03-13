package components;
import javax.swing.*;
import java.awt.BorderLayout;

public class CardImageTest {
    public static void main(String[] args) {
        Deck<Card> deck = new Deck<Card>(52);
        deck.shuffle();
        JFrame frame = new JFrame("Card Image Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        JPanel pane = new JPanel();
        pane.setLayout(null); //in order for cards to be able to overlap, 
                              //we have to use a null layout so we can place things manually

        // Add card images to the layered pane
        for (int i = 13; i > 0; i--) {
            Card card = deck.drawCard();
            JLabel cardLabel = new JLabel(new ImageIcon(card.getCardImage()));
            cardLabel.setBounds(i * 60-95, 315, 225, 325);
            //JLabel cardlabel2 = new JLabel(new ImageIcon("components/images/2B.png"));
            JLabel smallcardlabel = new JLabel(new ImageIcon("components/images/2Bsmall.png"));
            smallcardlabel.setBounds(i * 60-25, 0, 100, 140);
            //cardlabel2.setBounds(i * 60-95, 0, 225, 325);

            pane.add(cardLabel);
            //pane.add(cardlabel2);
            pane.add(smallcardlabel);
        }

        // Add the layered pane to the frame
        frame.add(pane, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
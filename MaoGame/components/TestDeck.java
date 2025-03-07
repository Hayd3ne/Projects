package components;

public class TestDeck {
    public static void main(String[] args) {
        Deck<Card> d = new Deck<Card>(52);
        d.shuffle();
        System.out.println(d.drawCard().getCardImage());
    }
}
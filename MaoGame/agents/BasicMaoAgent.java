package agents;
import components.Card;
import components.Deck;
import game.BasicMaoSimulator;

@SuppressWarnings("unused") 
public class BasicMaoAgent {
    private Card[] hand;
    private int id;
    private BasicMaoSimulator sim;

    public BasicMaoAgent(Card[] hand, int id) {
        this.hand = hand;
        this.id = id;
    }

    public BasicMaoAgent(int id) {
        this.hand = new Card[0];
        this.id = id;
    }

    public BasicMaoAgent(BasicMaoSimulator sim, int id) {
        this.hand = new Card[0];
        for (int i = 0; i < 5; i++) {
            drawCard(sim.getDeck());
        }
        this.sim = sim;
        this.id = id;

    }

    public Card[] getHand() {
        return hand;
    }

    public Card takeTurn(BasicMaoSimulator sim) {
        System.out.println("Player " + id + " has " + hand.length + " cards.");
        for (Card card : hand) {
            if (sim.isPlayValid(card,this)) {
                if (card.getRank() != Card.ranks.JACK) System.out.println("Player " + id + " played " + card);
                removeCard(card);
                return card;
            }
        }
        System.out.println("Player " + id + " drew a card.");
        if (!sim.getDeck().isEmpty()) drawCard(sim.getDeck());
        return null;
    }

    @SuppressWarnings("rawtypes")
    private void drawCard( Deck deck) {
        Card[] newHand = new Card[this.hand.length + 1];
        for (int i = 0; i < this.hand.length; i++) {
            newHand[i] = this.hand[i];
        }
        newHand[hand.length] = deck.drawCard();
        this.hand = newHand;
    }

    public boolean chooseSuit(BasicMaoSimulator sim, Card card) {
        // this will be done in a separate interface in the final version
        int max = 0;
        int count = 0;
        Card.suits curSuit = Card.suits.CLUBS; // default, is arbitrary
        Card.suits suit = Card.suits.CLUBS; // default
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] == card) {
                continue;
            }
            curSuit = hand[i].getSuit();
            for (int j = 0; j < hand.length; j++) {
                if (hand[j].getSuit() == curSuit) {
                    count += 1;
                }
            }
            if (count > max) {
                max = count;
                suit = curSuit;
            }
        }
        sim.setSuit(suit);
        System.out.println("Player " + id + " chose " + suit);
        return true;
    }

    public int getId() {
        return id;
    }

    public void removeCard(Card card) {
        Card[] newHand = new Card[this.hand.length - 1];
        int index = 0;
        for (int i = 0; i < this.hand.length; i++) {
            if (this.hand[i] != card) {
                newHand[index] = this.hand[i];
                index += 1;
            }
        }
        this.hand = newHand;
    }
}
package agents;
import components.MaoCard;
import game.TestProperties;

import components.Deck;

public class TestAgent extends Agent<TestProperties, MaoCard> {
    private int id;
    private MaoCard[] hand;

    public TestAgent(int id) {
        this.id = id;
        this.hand = new MaoCard[0];
    }

    public MaoCard takeTurn(TestProperties game) {
        for (int i = 0; i < hand.length; i++) {
            if (game.getRuleEngine().isPlayValid(hand[i],game,this)) {
                MaoCard temp = hand[i];
                removeCard(hand[i]);
                return temp;
            }
        }
        drawCard(game.getDeck());
        return null;
    }
    public MaoCard chooseSuit(TestProperties game, MaoCard card) {
        int max = 0;
        int count = 0;
        MaoCard.suits curSuit = MaoCard.suits.CLUBS; // default, is arbitrary
        MaoCard.suits suit = MaoCard.suits.CLUBS; // default
        for (int i = 0; i < hand.length; i++) {
            count = 0;
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
        game.setSuit(suit);
        System.out.println("Player " + id + " chose " + suit);
        for (int i = 0; i < hand.length; i++) {
            if (hand[i].getSuit() == suit) {
                return hand[i];
            }
        }
        return null;
    }

    public void drawCard(Deck<MaoCard> deck) {
        MaoCard[] newHand = new MaoCard[this.hand.length + 1];
        for (int i = 0; i < this.hand.length; i++) {
            newHand[i] = this.hand[i];
        }
        newHand[hand.length] = deck.drawCard();
        this.hand = newHand;
    }

    public void removeCard(MaoCard card) {
        MaoCard[] newHand = new MaoCard[this.hand.length - 1];
        int index = 0;
        for (int i = 0; i < this.hand.length; i++) {
            if (this.hand[i] != card) {
                newHand[index] = this.hand[i];
                index += 1;
            }
        }
        this.hand = newHand;
    }

    public int getId() {
        return this.id;
    }

    public MaoCard[] getHand() {
        return this.hand;
    }


}
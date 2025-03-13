package agents;

import components.Deck;
import components.MaoCard;
import game.TestProperties;
import rules.*;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("rawtypes")
public class SimpleMaoAgent extends MaoAgent<TestProperties<Rule>> {
    private List<MaoCard> hand;
    private List<Rule> knownRules;
    private List<List<Rule>> predictedRules;
    private int id;

    public SimpleMaoAgent(List<MaoCard> hand) {
        this.hand = hand;
    }

    public SimpleMaoAgent(int id) {
        this.knownRules = Arrays.asList(new WildJacks(), new SameRanks(), new SameSuits(), new JacksChangeSuit());
        this.hand = new java.util.ArrayList<>();
        this.id = id;
    }

    public MaoCard[] getHand() {
        return (MaoCard[]) this.hand.toArray();
    }

    public MaoCard takeTurn(TestProperties game) {
        return null;
    }

    @Override
    public void drawCard(Deck deck) {
        this.hand.add((MaoCard)deck.drawCard());
    }

    public void removeCard(MaoCard card) {
        this.hand.remove(card);
    }

    public int getId() {
        return this.id;
    } 

    public MaoCard chooseSuit(TestProperties game, MaoCard card) {
        return new MaoCard();
    }

}
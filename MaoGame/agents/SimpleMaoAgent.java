package agents;

import components.Deck;
import components.MaoCard;
import game.TestProperties;
import rules.*;

import java.util.List;

@SuppressWarnings("rawtypes")
public class SimpleMaoAgent extends MaoAgent<TestProperties<Rule>> {
    private List<MaoCard> hand;
    private List<Rule> knownRules;
    private List<List<Rule>> predictedRules = new java.util.ArrayList<>();
    private int id;

    public SimpleMaoAgent(List<MaoCard> hand) {
        this.hand = hand;
    }

    public SimpleMaoAgent(int id) {
        this.knownRules = new java.util.ArrayList<>();
        this.knownRules.add(new SameSuits());
        this.knownRules.add(new SameRanks());
        this.knownRules.add(new WildJacks());
        this.knownRules.add(new JacksChangeSuit());
        this.hand = new java.util.ArrayList<>();
        this.id = id;
    }

    public MaoCard[] getHand() {
        MaoCard[] out = new MaoCard[this.hand.size()];
        for (int i = 0; i < this.hand.size(); i++) {
            out[i] = this.hand.get(i);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public MaoCard takeTurn(TestProperties game) {

        System.out.println("Player "+ id + " knows the following rules: "+knownRules);
        System.out.println("Player " + id + " is predicting the following rules: " + predictedRules);

        MaoCard topCard = null;
        MaoCard lastCard = null;
        boolean newRule = false;
        if (game.getDiscard().size() >= 2) {
            topCard = (MaoCard)game.getDiscard().drawCard();
            //we only need to predict new rules if the last card played is not valid in our known rules
            if (!game.getRuleEngine().isPlayValid(topCard, game, this, knownRules)) {
                newRule = true;
            }
            lastCard = (MaoCard)game.getDiscard().drawCard();

        }
        //if we haven't predicted anything yet, do so now

        //if we have an 0, 1, or 11, we can predict the rule pretty easily
        for (MaoCard c : hand) {
            if (c.getProperty(MaoCard.property.RANK) == MaoCard.ranks.ZERO) {
                if (!knownRules.stream().anyMatch(obj -> obj instanceof Include0)) {
                    this.knownRules.add(new Include0());
                    //System.out.println(c.getProperties());
                }
            }
            if (c.getProperty(MaoCard.property.RANK) == MaoCard.ranks.ONE) {
                if (!knownRules.stream().anyMatch(obj -> obj instanceof Include1)) {
                    this.knownRules.add(new Include1());
                    //System.out.println(c.getProperties());
                }
            }
            if (c.getProperty(MaoCard.property.RANK) == MaoCard.ranks.ELEVEN) {
                if (!knownRules.stream().anyMatch(obj -> obj instanceof Include11)) {
                    this.knownRules.add(new Include11());
                    //System.out.println(c.getProperties());
                }                
            }
        }
        if (predictedRules == null&&newRule) {
            //we can only predict rules if it isn't the first turn of the game
            if (game.getDiscard().size() >= 2) {
                List<Rule> prediction = new java.util.ArrayList<>();
                prediction = predictRules(getProperties(lastCard, topCard));
                //System.out.println(predictedRules);
                //System.out.println(prediction);
                predictedRules.add(prediction);
                System.out.println(prediction);
            }

        }
        else if (newRule) for (List<Rule> prediction : predictedRules) {
            //refine predictions
            prediction = predictRules(prediction, lastCard, topCard);
            if (prediction.size() == 1) { //presumably, we can only add a single rule per round
                knownRules.add(prediction.get(0));
                predictedRules.remove(prediction);
            }
        }
        if (topCard != null && lastCard != null) {
            game.getDiscard().addCard(lastCard);
            game.getDiscard().addCard(topCard);
        }

        for (int i = 0; i < hand.size(); i++) {
            if (game.getRuleEngine().isPlayValid(hand.get(i),game,this,knownRules)) {
                MaoCard temp = hand.get(i);
                removeCard(hand.get(i));
                return temp;
            }
        }
        drawCard(game.getDeck());
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
        int max = 0;
        int count = 0;
        MaoCard.suits curSuit = MaoCard.suits.CLUBS; // default, is arbitrary
        MaoCard.suits suit = MaoCard.suits.CLUBS; // default
        for (int i = 0; i < hand.size(); i++) {
            count = 0;
            if (hand.get(i) == card) {
                continue;
            }
            curSuit = hand.get(i).getSuit();
            for (int j = 0; j < hand.size(); j++) {
                if (hand.get(j).getSuit() == curSuit) {
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
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getSuit() == suit) {
                return hand.get(i);
            }
        }
        return null;
    }

}
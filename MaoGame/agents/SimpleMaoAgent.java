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
    private List<List<Rule>> predictedRules = new java.util.ArrayList<>();
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
        MaoCard[] out = new MaoCard[this.hand.size()];
        for (int i = 0; i < this.hand.size(); i++) {
            out[i] = this.hand.get(i);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public MaoCard takeTurn(TestProperties game) {
        MaoCard topCard = null;
        MaoCard lastCard = null;
        if (game.getDiscard().size() >= 2) {
            topCard = (MaoCard)game.getDiscard().drawCard();
            lastCard = (MaoCard)game.getDiscard().drawCard();
        }
        //if we haven't predicted anything yet, do so now
        if (predictedRules == null) {
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
        else for (List<Rule> prediction : predictedRules) {
            //refine predictions
            prediction = predictRules(prediction, lastCard, topCard);
            if (prediction.size() <= 1) {
                knownRules.retainAll(prediction);
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
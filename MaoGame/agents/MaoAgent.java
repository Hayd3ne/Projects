package agents;

import components.MaoCard;
import components.MaoCard.property;
import game.Game;
import rules.*;

import java.util.List;

public abstract class MaoAgent <GameT extends Game<?, MaoCard>> extends Agent<GameT,MaoCard> implements Predictor {

    public List<property> getProperties (MaoCard lastCard, MaoCard card) {
        List<property> properties = new java.util.ArrayList<>();

        if (card.getRank() != lastCard.getRank() && card.getSuit() != lastCard.getSuit()) {
            properties.add(property.CHANGESUIT);
            //note, in the current rules, the only way a card can be played that is not of the same
            //suit or rank is a wild, or the suit has changed. 
            //This may change as we add more possible rules to the game
        }
        if (card.getRank() == lastCard.getRank()) {
            properties.add(property.RANKPARITY);
        }
        if (card.getSuit() == lastCard.getSuit()) {
            properties.add(property.SUITPARITY);
        }
        properties.add(property.WILD); //at the very least, any card play could be wild
        return properties;
    }

    @SuppressWarnings("rawtypes")
    public List<Rule> predictRules (List<property> prediction) {
        List<Rule> rules = new java.util.ArrayList<>();
        if (prediction.contains(property.WILD)) {
            rules.add(new WildJacks());
        }
        if (prediction.contains(property.RANKPARITY)) {
            rules.add(new SameRanks());
        }
        if (prediction.contains(property.SUITPARITY)) {
            rules.add(new SameSuits());
        }
        if (prediction.contains(property.CHANGESUIT)) {
            rules.add(new JacksChangeSuit());
        }
        return rules;
    }
    
    @SuppressWarnings("rawtypes")
    public List<Rule> predictRules (List<Rule> prediction, MaoCard lastCard, MaoCard card) {
        List<property> properties = getProperties(lastCard, card);
        List<Rule> rules = predictRules(properties);

        //find the intersection of the two lists
        rules.retainAll(prediction);

        //if the card is an 11, 0, or 1, it pretty easy to predict the rule
        if (card.getMaoRank() == MaoCard.ranks.ELEVEN) rules.add(new Include11());
        else if (card.getMaoRank() == MaoCard.ranks.ZERO) rules.add(new Include0());
        else if (card.getMaoRank() == MaoCard.ranks.ONE) rules.add(new Include1());
        return rules;
    }
}
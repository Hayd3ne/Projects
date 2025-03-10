package rules;
import components.Deck;
import components.MaoCard;
import game.Game;
import agents.Agent;
import java.util.List;

public class RuleEngine <RuleT extends Rule, AgentT extends Agent, GameT extends Game> {

    public void applyRules(RuleT[] rules, Deck<MaoCard> deck, GameT game, AgentT agent) {
        for (RuleT rule : rules) {
            for (MaoCard c : deck) {
                if (rule.isValid(c, game, agent)) {
                    rule.apply(c, game, agent);
                }
            }
        }
    }
    public void applyRules(List<RuleT> rules, Deck<MaoCard> deck, GameT game, AgentT agent) {
        for (RuleT rule : rules) {
            if (rule instanceof Include11) rule.apply(new MaoCard(), game, agent);
            else if (rule instanceof Include1) rule.apply(new MaoCard(), game, agent);
            else if (rule instanceof Include0) rule.apply(new MaoCard(), game, agent);
            else for (MaoCard c : deck) {
                if (rule.isValid(c, game, agent)) {
                    rule.apply(c, game, agent);
                }
            }
        }
    }
    public boolean isPlayValid(MaoCard card, GameT game, AgentT agent) {
        Deck<MaoCard> lastCards = (Deck<MaoCard>) game.getDiscard();
        MaoCard lastCard = lastCards.drawCard();
        //System.out.println(card);
        //System.out.println(card.getProperties());
        if (card.getProperty(MaoCard.property.WILD) != null) if (card.getProperty(MaoCard.property.WILD).equals(true)) {
            lastCards.addCard(lastCard);
            cardEffect(card, game, agent);
            return true;
        }
        if (lastCard.getProperty(MaoCard.property.RANKPARITY) != null) if (lastCard.getProperty(MaoCard.property.RANKPARITY).equals(true)) {
            if (card.getRank() == lastCard.getRank()) {
                lastCards.addCard(lastCard);
                cardEffect(card, game, agent);
                return true;
            }
        }
        if (lastCard.getProperty(MaoCard.property.SUITPARITY) != null) if (lastCard.getProperty(MaoCard.property.SUITPARITY).equals(true)) {
            if (card.getSuit() == game.getCurSuit()) {
                lastCards.addCard(lastCard);
                cardEffect(card, game, agent);
                return true;
            }
        }
        lastCards.addCard(lastCard);
        return false;
    }

    public void cardEffect(MaoCard card, GameT game, AgentT agent) {
        if (card.getProperty(MaoCard.property.CHANGESUIT) != null) if (card.getProperty(MaoCard.property.CHANGESUIT).equals(true)) {
            MaoCard c = (MaoCard) agent.chooseSuit(game, card);
            //System.out.println("Player " + agent.getId() + " Changed to " + c.getSuit());
        }
        else game.setSuit(card.getSuit());
    }
}
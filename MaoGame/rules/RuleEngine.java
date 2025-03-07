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
            for (MaoCard c : deck) {
                if (rule.isValid(c, game, agent)) {
                    rule.apply(c, game, agent);
                }
            }
        }
    }
    public boolean isPlayValid(MaoCard card, GameT game, AgentT agent) {
        Deck<MaoCard> lastCards = (Deck<MaoCard>) game.getDiscard();
        MaoCard lastCard = lastCards.drawCard();
        if (card.getProperty("isWild") != null) if (card.getProperty("isWild").equals(true)) {
            lastCards.addCard(lastCard);
            cardEffect(card, game, agent);
            return true;
        }
        if (lastCard.getProperty("rankParity") != null) if (lastCard.getProperty("rankParity").equals(true)) {
            if (card.getRank() == lastCard.getRank()) {
                lastCards.addCard(lastCard);
                cardEffect(card, game, agent);
                return true;
            }
        }
        if (lastCard.getProperty("suitParity") != null) if (lastCard.getProperty("suitParity").equals(true)) {
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
        if (card.getProperty("chooseSuit") != null) if (card.getProperty("chooseSuit").equals(true)) {
            MaoCard c = (MaoCard) agent.chooseSuit(game, card);
            //System.out.println("Player " + agent.getId() + " Changed to " + c.getSuit());
        }
        else game.setSuit(card.getSuit());
    }
}
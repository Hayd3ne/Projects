package rules;
import components.Deck;
import components.MaoCard;
import game.Game;
import agents.Agent;
import java.util.List;

@SuppressWarnings("rawtypes")
public class RuleEngine <RuleT extends Rule, AgentT extends Agent, GameT extends Game> {

    @SuppressWarnings("unchecked")
    public void applyRules(RuleT[] rules, Deck<MaoCard> deck, GameT game, AgentT agent) {
        for (RuleT rule : rules) {
            for (MaoCard c : deck) {
                if (rule.isValid(c, game, agent)) {
                    rule.apply(c, game, agent);
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    public void applyRules(List<RuleT> rules, Deck<MaoCard> deck, GameT game, AgentT agent) {
        for (RuleT rule : rules) {
            //System.out.println(rule.isApplied(null, game, agent));
            if (rule.isApplied(null, game, agent)) continue; //skip rules that have already been applied
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

    @SuppressWarnings("unchecked")
    public void applyRules(List<RuleT> rules, Deck<MaoCard> deck, GameT game, AgentT agent, boolean marker) {
        for (RuleT rule : rules) {
            //System.out.println(rule.isApplied(null, game, agent));
            if (rule instanceof Include11) {
                if (deck.stream().anyMatch(c -> c.getProperty(MaoCard.property.RANK) == MaoCard.ranks.ELEVEN)) continue;
                rule.apply(new MaoCard(), game, agent);
            }
            else if (rule instanceof Include1) 
            {
                if (deck.stream().anyMatch(c -> c.getProperty(MaoCard.property.RANK) == MaoCard.ranks.ONE)) continue;
                rule.apply(new MaoCard(), game, agent);
            }
            else if (rule instanceof Include0) {
                if (deck.stream().anyMatch(c -> c.getProperty(MaoCard.property.RANK) == MaoCard.ranks.ZERO)) continue;
                rule.apply(new MaoCard(), game, agent);
            }
            else for (MaoCard c : deck) {
                if (rule.isValid(c, game, agent)) {
                    rule.apply(c, game, agent);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean isPlayValid(MaoCard card, GameT game, AgentT agent) {
        Deck<MaoCard> lastCards = (Deck<MaoCard>) game.getDiscard();
        MaoCard lastCard = lastCards.drawCard();
        //System.out.println(card);
        //System.out.println(card.getProperties());
        if (card.getProperty(MaoCard.property.WILD) != null) if (card.getProperty(MaoCard.property.WILD).equals(true)) {
            lastCards.addCard(lastCard);
            return true;
        }
        if (lastCard.getProperty(MaoCard.property.RANKPARITY) != null) if (lastCard.getProperty(MaoCard.property.RANKPARITY).equals(true)) {
            if (card.getProperty(MaoCard.property.RANK) == lastCard.getProperty(MaoCard.property.RANK)) {
                //System.out.println("Card Rank: " + card.getProperty(MaoCard.property.RANK));
                lastCards.addCard(lastCard);
                return true;
            }
        }
        if (lastCard.getProperty(MaoCard.property.SUITPARITY) != null) if (lastCard.getProperty(MaoCard.property.SUITPARITY).equals(true)) {
            if (card.getSuit() == game.getCurSuit()) {
                //System.out.println("Card Suit: " + card.getSuit());
                lastCards.addCard(lastCard);
                return true;
            }
        }
        lastCards.addCard(lastCard);
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean isPlayValid(MaoCard card, GameT game, AgentT agent, List<RuleT> rules) {
        Deck<MaoCard> lastCards = (Deck<MaoCard>) game.getDiscard();
        MaoCard lastCard = lastCards.drawCard();
        //System.out.println(card);
        //System.out.println(card.getProperties());
        for (RuleT rule : rules) {
            if (rule instanceof Include11 || rule instanceof Include1 || rule instanceof Include0) continue;
            if (rule.isValid(card, game, agent)) {
                switch(rule.getProperty()) {
                    case WILD:
                        lastCards.addCard(lastCard);
                        return true;
                    case RANKPARITY:
                        if (card.getProperty(MaoCard.property.RANKPARITY) != null) if (card.getProperty(MaoCard.property.RANKPARITY).equals(true)) {
                            if (card.getProperty(MaoCard.property.RANK) == lastCard.getProperty(MaoCard.property.RANK)) {
                                //System.out.println("Card Rank: " + card.getProperty(MaoCard.property.RANK));
                                lastCards.addCard(lastCard);
                                return true;
                            }
                        }
                        break;
                    case SUITPARITY:
                        if (card.getProperty(MaoCard.property.SUITPARITY) != null) if (card.getProperty(MaoCard.property.SUITPARITY).equals(true)) {
                            if (card.getSuit() == game.getCurSuit()) {
                                //System.out.println("Card Suit: " + card.getSuit());
                                lastCards.addCard(lastCard);
                                return true;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        lastCards.addCard(lastCard);
        return false;
    }

    @SuppressWarnings("unchecked")
    public void cardEffect(MaoCard card, GameT game, AgentT agent) {
        if (card.getProperty(MaoCard.property.CHANGESUIT) != null) {
            if (card.getProperty(MaoCard.property.CHANGESUIT).equals(true)) {
                @SuppressWarnings("unused")
                MaoCard c = (MaoCard) agent.chooseSuit(game, card);
                //System.out.println("Player " + agent.getId() + " Changed to " + c.getSuit());
            }
        }  
        else {
            //System.out.println("Suit Changed");
            game.setSuit((MaoCard.suits)card.getProperty(MaoCard.property.SUIT));
        }
    }
}
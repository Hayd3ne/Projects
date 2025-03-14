package rules;
import components.MaoCard;
import components.Deck;
import game.Game;
import agents.Agent;

@SuppressWarnings("rawtypes")
public class Include0 <GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> 
extends Rule<GameT, AgentT> {

    public MaoCard.property getProperty() {
        return null;
    }

    @Override
    public boolean isValid(MaoCard card, GameT game, AgentT agent) {
        return true;
    }

    @Override
    public void apply(MaoCard card, GameT game, AgentT agent) {
        Deck<MaoCard> deck = game.getDeck();
        deck.addCard(new MaoCard(MaoCard.ranks.ZERO, MaoCard.suits.CLUBS,true));
        deck.addCard(new MaoCard(MaoCard.ranks.ZERO, MaoCard.suits.DIAMONDS,true));
        deck.addCard(new MaoCard(MaoCard.ranks.ZERO, MaoCard.suits.HEARTS,true));
        deck.addCard(new MaoCard(MaoCard.ranks.ZERO, MaoCard.suits.SPADES,true));
    }

    @Override
    public void undo(MaoCard card, GameT game, AgentT agent) {
        Deck<MaoCard> deck = game.getDeck();
        deck.removeCard(new MaoCard(MaoCard.ranks.ZERO, MaoCard.suits.CLUBS,true));
        deck.removeCard(new MaoCard(MaoCard.ranks.ZERO, MaoCard.suits.DIAMONDS,true));
        deck.removeCard(new MaoCard(MaoCard.ranks.ZERO, MaoCard.suits.HEARTS,true));
        deck.removeCard(new MaoCard(MaoCard.ranks.ZERO, MaoCard.suits.SPADES,true));
    }
}
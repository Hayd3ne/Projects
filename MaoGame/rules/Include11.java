package rules;
import components.MaoCard;
import components.Deck;
import game.Game;
import agents.Agent;

@SuppressWarnings("rawtypes")
public class Include11 <GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> 
extends Rule<GameT, AgentT> {


    @Override
    public boolean isValid(MaoCard card, GameT game, AgentT agent) {
        return true;
    }

    @Override
    public void apply(MaoCard card, GameT game, AgentT agent) {
        Deck<MaoCard> deck = game.getDeck();
        deck.addCard(new MaoCard(MaoCard.ranks.ELEVEN, MaoCard.suits.CLUBS,true));
        deck.addCard(new MaoCard(MaoCard.ranks.ELEVEN, MaoCard.suits.DIAMONDS,true));
        deck.addCard(new MaoCard(MaoCard.ranks.ELEVEN, MaoCard.suits.HEARTS,true));
        deck.addCard(new MaoCard(MaoCard.ranks.ELEVEN, MaoCard.suits.SPADES,true));
    }

    @Override
    public void undo(MaoCard card, GameT game, AgentT agent) {
        Deck<MaoCard> deck = game.getDeck();
        deck.removeCard(new MaoCard(MaoCard.ranks.ELEVEN, MaoCard.suits.CLUBS,true));
        deck.removeCard(new MaoCard(MaoCard.ranks.ELEVEN, MaoCard.suits.DIAMONDS,true));
        deck.removeCard(new MaoCard(MaoCard.ranks.ELEVEN, MaoCard.suits.HEARTS,true));
        deck.removeCard(new MaoCard(MaoCard.ranks.ELEVEN, MaoCard.suits.SPADES,true));
    }
}
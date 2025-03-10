package rules;
import components.MaoCard;
import components.Deck;
import game.Game;
import agents.Agent;

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
}
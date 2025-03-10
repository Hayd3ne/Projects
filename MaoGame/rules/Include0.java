package rules;
import components.MaoCard;
import components.Deck;
import game.Game;
import agents.Agent;

public class Include0 <GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> 
extends Rule<GameT, AgentT> {


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
}
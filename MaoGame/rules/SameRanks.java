package rules;
import components.MaoCard;
import game.Game;
import agents.Agent;

public class SameRanks <GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> 
extends Rule<GameT, AgentT> {

    @Override
    public boolean isValid(MaoCard card, GameT game, AgentT agent) {
        return true;
    }

    @Override
    public void apply(MaoCard card, GameT game, AgentT agent) {
        card.setProperty(MaoCard.property.RANKPARITY, true);
    }

    @Override
    public void undo(MaoCard card, GameT game, AgentT agent) {
        card.setProperty(MaoCard.property.RANKPARITY, false);
    }
}
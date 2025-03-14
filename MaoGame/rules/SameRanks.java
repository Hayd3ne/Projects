package rules;
import components.MaoCard;
import game.Game;
import agents.Agent;

@SuppressWarnings("rawtypes")
public class SameRanks <GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> 
extends Rule<GameT, AgentT> {

    public final static MaoCard.property property = MaoCard.property.RANKPARITY;

    @Override
    public MaoCard.property getProperty() {
        return property;
    }

    @Override
    public boolean isValid(MaoCard card, GameT game, AgentT agent) {
        return true;
    }

    @Override
    public void apply(MaoCard card, GameT game, AgentT agent) {
        card.setProperty(property, true);
    }

    @Override
    public void undo(MaoCard card, GameT game, AgentT agent) {
        card.setProperty(property, false);
    }
}
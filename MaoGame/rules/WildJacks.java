package rules;
import components.MaoCard;
import game.Game;
import agents.Agent;

@SuppressWarnings("rawtypes")
public class WildJacks <GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> 
extends Rule<GameT, AgentT> {

    public static final MaoCard.property property = MaoCard.property.WILD;
    public boolean applied = false;

    @Override
    public MaoCard.property getProperty() {
        return property;
    }

    @Override
    public boolean isValid(MaoCard card, GameT game, AgentT agent) {
        return card.getMaoRank() == MaoCard.ranks.JACK;
    }

    public boolean isApplied(MaoCard card, GameT game, AgentT agent) {
        return applied;
    }

    @Override
    public void apply(MaoCard card, GameT game, AgentT agent) {
        card.setProperty(property, true);
        applied = true;
    }

    @Override
    public void undo(MaoCard card, GameT game, AgentT agent) {
        card.setProperty(property, false);
        applied = false;
    }
}
package rules;
import agents.Agent;
import game.Game;
import components.MaoCard;

@SuppressWarnings("rawtypes")
public abstract class Rule<GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> {
    public static final MaoCard.property property = null;
    public abstract boolean isApplied(MaoCard card, GameT game, AgentT agent);
    public abstract MaoCard.property getProperty();
    public abstract boolean isValid(MaoCard card, GameT game, AgentT agent);
    public abstract void apply(MaoCard card, GameT game, AgentT agent);
    public abstract void undo(MaoCard card, GameT game, AgentT agent);
}
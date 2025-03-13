package rules;
import agents.Agent;
import game.Game;
import components.MaoCard;

@SuppressWarnings("rawtypes")
public abstract class Rule<GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> {
    public abstract boolean isValid(MaoCard card, GameT game, AgentT agent);
    public abstract void apply(MaoCard card, GameT game, AgentT agent);
    public abstract void undo(MaoCard card, GameT game, AgentT agent);
}
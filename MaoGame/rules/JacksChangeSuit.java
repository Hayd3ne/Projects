package rules;
import components.MaoCard;
import game.Game;
import agents.Agent;

public class JacksChangeSuit <GameT extends Game<AgentT, MaoCard>, AgentT extends Agent> 
extends Rule<GameT, AgentT> {

    @Override
    public boolean isValid(MaoCard card, GameT game, AgentT agent) {
        return card.getMaoRank() == MaoCard.ranks.JACK;
    }

    @Override
    public void apply(MaoCard card, GameT game, AgentT agent) {
        card.setProperty((Object)MaoCard.property.CHANGESUIT, true);
    }
}
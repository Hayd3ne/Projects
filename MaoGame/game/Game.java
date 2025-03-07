package game;
import components.Deck;
import agents.Agent;
import components.Card;

public abstract class Game<AgentT extends Agent, CardT extends Card> {
    public abstract AgentT[] getPlayers();
    public abstract boolean step(AgentT player);
    public abstract Deck<CardT> getDeck();
    public abstract Deck<CardT> getDiscard();
    public abstract void setSuit(Card.suits suit);
    public abstract void render();
    public abstract CardT.suits getCurSuit();
}
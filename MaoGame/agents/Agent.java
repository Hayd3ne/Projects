package agents;
import components.Card;
import components.Deck;
import game.Game;

@SuppressWarnings("rawtypes")
public abstract class Agent<GameT extends Game, CardT extends Card> {
    public abstract int getId();
    public abstract CardT[] getHand();
    public abstract CardT takeTurn(GameT game);
    public abstract void removeCard(CardT card);
    public abstract CardT chooseSuit(GameT game, CardT card);
    public abstract void drawCard(Deck<CardT> deck);
}
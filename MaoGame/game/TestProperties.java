package game;
import components.Deck;
import components.MaoCard;
import agents.TestAgent;
import rules.*;
import java.util.List;

public class TestProperties <RuleT extends Rule> extends Game<TestAgent, MaoCard> {

    private Deck<MaoCard> deck;
    private Deck<MaoCard> discard;
    private TestAgent[] players;
    private RuleEngine re;
    private List<Object> rules;
    private MaoCard.suits curSuit;


    public static void main(String[] args) {
            TestProperties game = new TestProperties();

            MaoCard firstCard = (MaoCard)game.getDeck().drawCard();
            game.setSuit(firstCard.getSuit());
            game.discard.addCard(firstCard);
            for (TestAgent player : game.getPlayers()) {
                for (int i = 0; i < 5; i++) {
                    player.drawCard(game.getDeck());
                }
            }
            
            boolean running = true;
            while (running) {
                for (TestAgent player : game.getPlayers()) {
                    running = game.step(player);
                    if (!running) {
                        break;
                    }
                }
            }
    }
    public TestProperties() {
        this.deck = new Deck<MaoCard>(52, new MaoCard());
        this.deck.shuffle();
        this.players = new TestAgent[2];
        this.players[0] = new TestAgent(1);
        this.players[1] = new TestAgent(2);
        this.discard = new Deck<MaoCard>();


        this.re = new RuleEngine();
        this.rules = new java.util.ArrayList<>();
        this.rules.add(new WildJacks());
        this.rules.add(new SameRanks());
        this.rules.add(new SameSuits()); // The round 1 rules
        this.rules.add(new JacksChangeSuit());
        this.re.applyRules(rules, deck, this, null);
    }

    public RuleEngine getRuleEngine() {
        return re;
    }

    public Deck<MaoCard> getDeck() {
        return deck;
    }

    public Deck<MaoCard> getDiscard() {
        return discard;
    }

    public TestAgent[] getPlayers() {
        return players;
    }

    public void render() {

    }

    public void setSuit(MaoCard.suits suit) {
        this.curSuit = suit;
    }

    public boolean step(TestAgent player) {

        System.out.println("\nPlayer "+player.getId()+"'s turn.");

        MaoCard top = this.discard.drawCard();
        System.out.println("Top Card: "+top);
        //System.out.println("Top Card Properties: "+top.getProperties());
        System.out.println("Current Suit: "+this.curSuit);
        this.discard.addCard(top);

        System.out.println("------------------");
        for (MaoCard c : player.getHand()) {
            System.out.println("Player Card: "+c);
            //System.out.println("Player Card Properties: "+c.getProperties());
            //System.out.println("Valid Play: "+this.re.isPlayValid(c, this, player));
        }
        System.out.println("------------------");
        MaoCard card = player.takeTurn(this);

        if (card != null) {
            System.out.println("Player "+player.getId()+" played "+card);
            discard.addCard(card);
        }
        else {
            System.out.println("Player "+player.getId()+" drew a card.");
        }

        if (player.getHand().length == 1) {
            System.out.println("Player "+player.getId()+" has 1 card left.");
        }
        if (player.getHand().length == 0) {
            System.out.println("Player "+player.getId()+" wins!");
            return false;
        }
        if (deck.isEmpty()||discard.size() == 52) {
            System.out.println("There are no more cards in the deck. Shuffling...");
            //shuffle all but the top card in the discard pile back into the deck
            MaoCard top2 = discard.drawCard();
            deck = deck.combineDecks(discard, deck);
            discard.addCard(top2);
        }
        return true;
    }

    public MaoCard.suits getCurSuit() {
        return curSuit;
    }

}

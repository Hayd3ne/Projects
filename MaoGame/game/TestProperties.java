package game;
import components.Deck;
import components.MaoCard;
import agents.SimpleMaoAgent;
import rules.*;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("rawtypes")
public class TestProperties <RuleT extends Rule> extends Game<SimpleMaoAgent, MaoCard> {

    private Deck<MaoCard> deck;
    private Deck<MaoCard> discard;
    private SimpleMaoAgent[] players;
    private RuleEngine re;
    private List<Object> rules;
    private MaoCard.suits curSuit;
    private int deckSize;


    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
            TestProperties game = new TestProperties();            

            Scanner in = new Scanner(System.in);


            @SuppressWarnings("unused")
            int gameCount = 0;
            while (true) {
                game.setup(game);
                boolean running = true;
                int numSteps = 0;
                while (running) {
                    numSteps++;
                    if (numSteps >= 200) {
                        System.out.println("No one wins.");
                        running = false;
                        break;
                    }
                    for (SimpleMaoAgent player : game.getPlayers()) {
                        running = game.step(player);
                        if (!running) {
                            break;
                        }
                    }
                }
                gameCount++;
                System.out.println("Continue? (y/n)");
                String response = in.nextLine();
                if (response.toLowerCase().equals("n")) {
                    running = false;
                    break;
                }
                /* 
                switch(gameCount) {
                    case 1:
                        game = new TestProperties(game,new Include0());
                        break;
                    case 2:
                        game = new TestProperties(game,new Include1());
                        break;
                    case 3:
                        game = new TestProperties(game,new Include11());
                        break;
                    default:
                        game = new TestProperties(game,null);
                        break;
                }
                */

                game = new TestProperties(game,null);
        }
        in.close();
    }


    @SuppressWarnings("unchecked")
    public void setup(TestProperties game) {
        MaoCard firstCard = (MaoCard)game.getDeck().drawCard();
            game.setSuit(firstCard.getSuit());
            game.getDiscard().addCard(firstCard);
            for (SimpleMaoAgent player : game.getPlayers()) {
                for (int i = 0; i < 5; i++) {
                    player.drawCard(game.getDeck());
                }
            }
    }

    @SuppressWarnings("unchecked")
    public TestProperties() {
        this.deck = new Deck<MaoCard>(52, new MaoCard());
        this.deck.shuffle();
        this.players = new SimpleMaoAgent[2];
        this.players[0] = new SimpleMaoAgent(1);
        this.players[1] = new SimpleMaoAgent(2);
        this.discard = new Deck<MaoCard>();


        this.re = new RuleEngine();
        this.rules = new java.util.ArrayList<>();
        this.rules.add(new WildJacks());
        this.rules.add(new SameRanks());
        this.rules.add(new SameSuits()); // The round 1 rules
        this.rules.add(new JacksChangeSuit());
        this.re.applyRules(rules, deck, this, null);
        this.deckSize = this.deck.size();
        this.deck.shuffle();
    }

    @SuppressWarnings("unchecked")
    public TestProperties(TestProperties lastGame, RuleT newRule) {
        //add player cards and discard cards back into the deck
        for (SimpleMaoAgent player : lastGame.getPlayers()) {
            for (MaoCard card : player.getHand()) {
                lastGame.getDeck().addCard(card);
                player.removeCard(card);
            }
        }
        while (!lastGame.getDiscard().isEmpty()) { 
            lastGame.getDeck().addCard(lastGame.getDiscard().drawCard());
        }
        this.deck = lastGame.getDeck();
        this.players = lastGame.getPlayers();
        this.discard = lastGame.getDiscard();


        this.re = lastGame.getRuleEngine();
        this.rules = lastGame.rules;
        if (newRule != null) this.rules.add(newRule);
        if (newRule instanceof Include11 || newRule instanceof Include1 || newRule instanceof Include0) {
            Deck<MaoCard> temp = new Deck(0, new MaoCard());
            System.out.println("Deck Size: "+this.deck.size());
            this.re.applyRules(this.rules, temp, this, null);
            this.deck = this.deck.combineDecks(deck, temp);
            System.out.println("Deck Size: "+this.deck.size());
        }
        this.re.applyRules(rules, deck, this, null);
        this.deckSize = this.deck.size();
        this.deck.shuffle();
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

    public SimpleMaoAgent[] getPlayers() {
        return players;
    }

    public void render() {

    }

    public void setSuit(MaoCard.suits suit) {
        this.curSuit = suit;
    }

    @SuppressWarnings("unchecked")
    public boolean step(SimpleMaoAgent player) {

        System.out.println("\nPlayer "+player.getId()+"'s turn.");

        MaoCard top = this.discard.drawCard();
        System.out.println("Top Card: "+top);
        //System.out.println("Top Card Properties: "+top.getProperties());
        System.out.println("Current Suit: "+this.curSuit);
        this.discard.addCard(top);
        System.out.println("Cards in the Deck: "+this.deck.size());
        System.out.println("Cards in the Discard: "+this.discard.size());
        int playerCards = 0;
        for (Object e : this.getPlayers()) {
            playerCards += ((SimpleMaoAgent)e).getHand().length;
        }
        System.out.println("Cards held by Players: "+playerCards);

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
            System.out.println("Card Properties: "+card.getProperties());
            this.discard.addCard(card);
            this.re.cardEffect(card, this, player);
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
        if (deck.isEmpty()||discard.size() == this.deckSize) {
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

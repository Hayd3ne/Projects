package game;
import components.Card;
import components.Deck;
import agents.BasicMaoAgent;


public class BasicMaoSimulator {
    private Deck deck;
    private Deck discard;
    private Card.suits curSuit;
    private Card.ranks curRank;
    private BasicMaoAgent[] players;

    public BasicMaoSimulator(int numPlayers) {
        this.deck = new Deck(52);
        this.discard = new Deck();
        this.players = new BasicMaoAgent[numPlayers];
    }
    
    public static void main(String[] args) {
        BasicMaoSimulator simulator = new BasicMaoSimulator(3);
        Deck deck = simulator.deck;
        Deck discard = simulator.discard;
        deck.shuffle();

        for (int i = 0; i < simulator.players.length; i++) {
            simulator.players[i] = new BasicMaoAgent(simulator, i+1);
        }

        boolean running = true;
        discard.addCard(deck.drawCard()); //have to have a starting card
        int count = 0;
        while (running) {
            for (BasicMaoAgent player : simulator.players) {
                running = simulator.step(player);
                if (!running) {
                    break;
                }
                /*count++;
                if (count == 100) {
                    System.out.println("No one wins.");
                    running = false;
                    break;
                } */
            }
        }
    }

    public boolean step(BasicMaoAgent player) {
        System.out.println("-----------");
        System.out.println("Player "+player.getId()+"'s turn.");
        Card card = player.takeTurn(this);
        if (card != null) {
            discard.addCard(card);
        }
        if (player.getHand().length == 1) {
            System.out.println("Player "+player.getId()+" has 1 card left.");
        }
        if (player.getHand().length == 0) {
            System.out.println("Player "+player.getId()+" wins!");
            return false;
        }
        System.out.println("There are " + discard.size() + " cards in the discard pile:");
        System.out.println(discard.toString());
        if (deck.isEmpty()||discard.size() == 52) {
            System.out.println("There are no more cards in the deck. Shuffling...");
            //shuffle all but the top card in the discard pile back into the deck
            Card top = discard.drawCard();
            deck = deck.combineDecks(discard, deck);
            discard.addCard(top);
        }
        if (discard.size() > 52) {
            System.out.println("Error: There are more than 52 cards in the discard pile.");
            return false;
        }
        return true;
    }

    //this will be done in a separate interface in the final version
    public boolean isPlayValid(Card card, BasicMaoAgent player) {
        Card curCard = this.discard.drawCard();
        if (discard.size()>2) if (((Card)discard.get(1)).getRank() != Card.ranks.JACK) this.curSuit = curCard.getSuit();
        this.curRank = curCard.getRank();
        //System.out.println("Current Rank: " + curRank);
        //System.out.println("Current Suit: " + curSuit);
        if (card.getRank() == Card.ranks.JACK) {
            discard.addCard(curCard);
            System.out.println("Player " + player.getId() + " played " + card);
            return player.chooseSuit(this,card);
        }
        else if (card.getRank() == this.curRank) {
            discard.addCard(curCard);
            return true;
        }
        else if (card.getSuit() == this.curSuit) {
            discard.addCard(curCard);
            return true;
        }
        else {
            discard.addCard(curCard);
            return false;
        }
    }

    public Deck getDeck() {
        return this.deck;
    }
    
    public Deck getDiscard() {
        return this.discard;
    }

    public void setSuit(Card.suits suit) {
        this.curSuit = suit;
    }

}
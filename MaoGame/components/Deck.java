package components;
import java.util.Stack;

public class Deck <CardT extends Card> extends Stack<CardT> {

    public Deck () {
        //a blank deck
    }

    //creates a deck of the given size
    public Deck (int size) {
        for (int i = 0; i < size; i++) {
            CardT c = (CardT) new Card(Card.ranks.values()[i%52%13], Card.suits.values()[i%52/13]);
            push(c);
        }
    }

    public Deck (int size, CardT c) {
        for (int i = 0; i < size; i++) {
            CardT card = createCard(Card.ranks.values()[i%52%13], Card.suits.values()[i%52/13], c);
            push(card);
        }
    }

    private CardT createCard(Card.ranks r, Card.suits s, CardT c) {
        if (c instanceof MaoCard) {
            return (CardT) new MaoCard(r, s);
        }
        else if (c instanceof Card) {
            return (CardT) new Card(r, s);
        } 
        else return null;
    }

    public Deck (CardT[] cards) {    
        for (int i = 0; i < cards.length; i++) {
            push(cards[i]);
        }
    }

    @Override
    public String toString() {
        String out = "";
        for (int i = size()-1; i >= 0; i--) {
            out += (i + ": " + get(i)) + "\n";
        }
        return out;
    }

    public void shuffle() {
        for (int i = 0; i < size(); i++) {
            int j = (int) (Math.random() * size());
            CardT temp = get(i);
            set(i, get(j));
            set(j, temp);
        }
    }

    //intended to create a new shuffled deck from two decks
    public Deck<CardT> combineDecks(Deck<CardT> d1, Deck<CardT> d2) {
        Deck<CardT> out = new Deck<CardT>();
        while (d1.size() > 0 && d2.size() > 0) {
            CardT c1 = (CardT) d1.pop();
            CardT c2 = (CardT) d2.pop();
            out.push(c1);
            out.push(c2);
        }
        //there are still cards left in the first deck
        if (d1.size() >= 0) {
            while (d1.size() > 0) {
                out.push(d1.pop());
            }
        }
        //there are still cards left in the second deck
        if (d2.size() >= 0) {
            while (d2.size() > 0) {
                out.push(d2.pop());
            }
        }
        out.shuffle();
        return out;
    }

    public CardT drawCard() {
        return (CardT) pop();
    }

    public CardT[] drawCards(int n) {
        Object[] out = new Object[n];
        for (int i = 0; i < n; i++) {
            out[i] = super.pop();
        }
        return (CardT[]) out;
    }

    public void addCard(CardT card) {
        super.push(card);
    }

}
package components;

import java.util.HashMap;

public class MaoCard extends Card {

    public static enum property {RANK, SUIT, HIGH, LOW, VALUE, WILD, SKIPS, CHANGESUIT, 
        REVERSES, RANKPARITY, SUITPARITY, ADDS, FOLLOWEDBY, JUMPS, SWAPS};

    public static enum ranks {
        ACE(Card.ranks.ACE),
        JOKER,
        ZERO,
        ONE,
        TWO(Card.ranks.TWO),
        THREE(Card.ranks.THREE),
        FOUR(Card.ranks.FOUR),
        FIVE(Card.ranks.FIVE),
        SIX(Card.ranks.SIX),
        SEVEN(Card.ranks.SEVEN),
        EIGHT(Card.ranks.EIGHT),
        NINE(Card.ranks.NINE),
        TEN(Card.ranks.TEN),
        JACK(Card.ranks.JACK),
        QUEEN(Card.ranks.QUEEN),
        KING(Card.ranks.KING),
        ELEVEN;

        ranks(Card.ranks r) {
            this.r = r;
        }
        private Card.ranks r;
        ranks() {}
    }


    private HashMap<Object, Object> properties;

    private ranks rank;
    private Card.suits MaoSuit;
    @SuppressWarnings("unused")
    private String image;

    public MaoCard(ranks r, suits s) {
        super(r.r, s);
        this.rank = (ranks)r;
        this.MaoSuit = s;
        this.properties = new HashMap<>();
        this.properties.put(property.RANK, r);
        this.properties.put(property.SUIT, s);
    }
    public MaoCard() {this(ranks.ACE, suits.SPADES);}; //a default arbitrary card
    public HashMap<Object, Object> getProperties() {
        return properties;
    }

    public MaoCard(ranks r, Card.suits s, boolean marker) {
        this.rank = r;
        this.MaoSuit = s;
        String card = "";
        if (this.rank == MaoCard.ranks.ELEVEN) card += "E";
        else if (this.rank == MaoCard.ranks.ZERO) card += "0";
        else if (this.rank == MaoCard.ranks.JOKER) card += "J";
        else if (this.rank == MaoCard.ranks.ONE) card += "0";
        card = card.charAt(0) + "" + card.charAt(card.indexOf(" ")+1);
        this.image = "components/images/"+card+"small.png";
        this.properties = new HashMap<>();
        this.properties.put(property.RANK, r);
        this.properties.put(property.SUIT, s);
        
    }

    public MaoCard(Card.ranks r, Card.suits s) {
        this(getMaoRank(r),s);
    }

    @Override
    public Card.suits getSuit() {
        if (this.MaoSuit != null) return this.MaoSuit;
        if (this.properties.get(property.SUIT) != null) return (Card.suits) this.properties.get(property.SUIT);
        return null;
    }

    public Object getProperty(Object key) {
        return properties.get(key);
    }

    public void setProperty(Object key, Object value) {
        properties.put(key, value);
    }

    public ranks getMaoRank() {
        return (ranks) properties.get(property.RANK);
    }

    private static ranks getMaoRank(Card.ranks r)  {
        switch (r) {
            case ACE:
                return ranks.ACE;
            case JACK:
                return ranks.JACK;
            case QUEEN:
                return ranks.QUEEN;
            case KING:
                return ranks.KING;
            case TEN:
                return ranks.TEN;
            case NINE:
                return ranks.NINE;
            case EIGHT:
                return ranks.EIGHT;
            case SEVEN:
                return ranks.SEVEN;
            case SIX:
                return ranks.SIX;
            case FIVE:
                return ranks.FIVE;
            case FOUR:
                return ranks.FOUR;
            case THREE:
                return ranks.THREE;
            case TWO:
                return ranks.TWO;
            default:
                return ranks.ACE;
        }
    }

    public static String toString(MaoCard[] hand) {
        String out = "";
        for (int i = 0; i < hand.length; i++) {
            out += hand[i] + "\n";
        }
        out = out.substring(0, out.length() - 2);
        return out;
    }

    @Override
    public String toString() {
        return this.rank + " of " + this.MaoSuit;
    }

}
package components;

public class Card {
    public static enum suits {CLUBS, DIAMONDS, HEARTS, SPADES};
    public static enum ranks {
        ACE(1,11), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9),
        TEN(10), JACK(10), QUEEN(10), KING(10);
        private int value;
        private int low, high;
        ranks(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            this.value = value;
        }
        public void setHigh(boolean bool) {
            if (bool) value = high;
            else value = low;
        }
        public int getLow() {
            return low;
        }
        public int getHigh() {
            return high;
        }
        ranks(int low, int high) { //this is specifically for aces, but could be used for other cards
            this.low = low;
            this.high = high;
            value = high; //this assumes the default is that aces are high
        }
    };
    public static enum colors {RED, BLACK};
    private ranks rank;
    private suits suit;
    private String image;
    public Card(ranks r, suits s) {
        rank = r;
        suit = s;
        String card = "";
        if (this.rank == Card.ranks.TEN) card += "T";
        else if (this.rank == Card.ranks.ACE) card += "A";
        else if (this.rank == Card.ranks.JACK) card += "J";
        else if (this.rank == Card.ranks.QUEEN) card += "Q";
        else if (this.rank == Card.ranks.KING) card += "K";
        else card += this.rank.getValue();
        card = card.charAt(0) + "" + card.charAt(card.indexOf(" ")+1);
        this.image = "components/images/"+card+"small.png";
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }

    public ranks getRank() {
        return this.rank;
    }

    public suits getSuit() {
        return this.suit;
    }

    public String getCardImage(Card this) {
        return this.image;
    }
}
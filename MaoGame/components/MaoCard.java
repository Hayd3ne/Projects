package components;

import java.util.HashMap;

public class MaoCard extends Card {
    private HashMap<String, Object> properties;
    public MaoCard(ranks r, suits s) {
        super(r, s);
        this.properties = new HashMap<>();
        this.properties.put("rank", r);
        this.properties.put("suit", s);
    }
    public MaoCard() {this(ranks.ACE, suits.SPADES);}; //a default arbitrary card
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public static String toString(MaoCard[] hand) {
        String out = "";
        for (int i = 0; i < hand.length; i++) {
            out += hand[i] + "\n";
        }
        out = out.substring(0, out.length() - 2);
        return out;
    }

}
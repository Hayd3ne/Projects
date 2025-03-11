package agents;

import components.MaoCard;
import components.MaoCard.property;
import rules.Rule;
import java.util.List;

public interface Predictor {
    public List<property> getProperties (MaoCard lastCard, MaoCard card);
    public List<Rule> predictRules (List<property> properties);
    public List<Rule> predictRules (List<Rule> prediction, MaoCard lastCard, MaoCard card);
}
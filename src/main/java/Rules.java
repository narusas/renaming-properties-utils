import java.util.ArrayList;

class Rules extends ArrayList<Rule> {
    static Rules of(Class ruleClass) {
        return new RulesFactory(ruleClass).result();
    }
}

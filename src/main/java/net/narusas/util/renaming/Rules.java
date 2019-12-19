package net.narusas.util.renaming;

import java.util.ArrayList;

public class Rules extends ArrayList<Rule> {
    public static Rules of(Class ruleClass) {
        return new RulesFactory(ruleClass).result();
    }


    public Rule find(String path) {
        for (Rule rule : this) {
            if (rule.getPath().equals(path)) {
                return rule;
            }
        }
        return null;
    }


}

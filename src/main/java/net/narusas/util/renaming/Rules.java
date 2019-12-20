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


    public Rules startsWith(String prefix) {
        Rules res = new Rules();
        for (Rule rule : this) {
            if (rule.getPath().startsWith(prefix)) {
                res.add(rule);

            }
        }
        return res;

    }

    public int unpackIndex(Rule targetRule) {
        if (targetRule.unpackIndex() != null) {
            return targetRule.unpackIndex();
        }

        Rules unpacks = new Rules();
        for (Rule rule : this) {
            if (targetRule.getUnpackingPrefix().equals(rule.getUnpackingPrefix())) {
                unpacks.add(rule);

            }
        }
        for (int i = 0; i < unpacks.size(); i++) {
            if (targetRule == unpacks.get(i)) {
                targetRule.unpackIndex = i;
                return i;
            }
        }
        throw new IllegalArgumentException("Not unpack target: " + targetRule);


    }
}

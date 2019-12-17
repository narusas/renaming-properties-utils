package net.narusas.util;

import java.util.ArrayList;

public class Rules extends ArrayList<Rule> {
    public static Rules of(Class ruleClass) {
        return new RulesFactory(ruleClass).result();
    }


    public Rules subRenamePaths(Rule rule) {
        Rules result = new Rules();
        for (Rule item : this) {
            if (item.getRenamePath().startsWith(rule.getRenamePath())) {
                result.add(item.splitRename(rule.getRenamePath()));
            }
        }

        return result;
    }

    public Rule find(String path) {
        for (Rule rule : this) {
            if (rule.getPath().equals(path)) {
                return rule;
            }
        }
        return null;
    }

    public <T> void copyTo(Object sourceRoot, T targetRoot, Rules targetRules) {
        for(Rule sourceRule: this) {
            Rule targetRule = targetRules.find(sourceRule.getRenamePath());
            if (targetRule == null) {
                continue;
            }
            sourceRule.copyTo(sourceRoot, targetRoot, targetRule);
        }
    }
}

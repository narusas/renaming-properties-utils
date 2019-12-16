import java.util.ArrayList;

class Rules extends ArrayList<Rule> {
    static Rules of(Class ruleClass) {
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
}

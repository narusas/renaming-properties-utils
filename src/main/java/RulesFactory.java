import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class RulesFactory {
    private static boolean useCache = false;
    private static Map<Class, Rules> cachesRules = new ConcurrentHashMap<>();
    private Rules rules;

    public static void enalbeCache() {
        RulesFactory.useCache = true;
    }

    public static void disableCache() {
        RulesFactory.useCache = false;
    }


    public RulesFactory(Class ruleClass) {
        if (useCache && cachesRules.containsKey(ruleClass)) {
            return;
        }
        Rule rootRule = new Rule();
        rules = parse(rootRule, "/", "/", ruleClass, new Rules());
        rules.sort((c1, c2) -> c1.toString().compareTo(c2.toString()));
        if (useCache) {
            cachesRules.put(ruleClass, rules);
        }
    }

    public Rules result() {
        return rules;
    }

    protected Rules parse(Rule parentRule, String parentPath, String renamePath, Class ruleClass, Rules rules) {
        Field[] fields = ruleClass.getDeclaredFields();
        for (Field field : fields) {
            new FieldParser(parentRule, parentPath, renamePath, ruleClass, rules, field).process();
        }
        return rules;
    }

    class FieldParser {

        private final Rule parentRule;
        private final String parentPath;
        private final String renamePath;
        private final Class ruleClass;
        private final Rules rules;
        private final Field field;

        private boolean isFlatten;
        private String currentPath;
        private String rename;
        private String currentRenamePath;
        private Rule nextParentRule;

        public FieldParser(Rule parentRule, String parentPath, String renamePath, Class ruleClass, Rules rules, Field field) {
            this.parentRule = parentRule;
            this.parentPath = parentPath;
            this.renamePath = renamePath;
            this.ruleClass = ruleClass;
            this.rules = rules;
            this.field = field;
            isFlatten = readFlatten();
            currentPath = parentPath + field.getName() + "/";
            rename = readRename();
            nextParentRule = parentRule;
            currentRenamePath = renamePath;
        }

        void process() {
            adjustRenamePath();

            if (isBasicType()) {
                rules.add(new Rule(parentRule, ruleClass, field, parentPath, field.getName(), currentRenamePath, rename));
            } else {
                if (isFlatten == false) {
                    nextParentRule = new Rule(parentRule, ruleClass, field, parentPath, field.getName(), currentRenamePath, rename);
                    rules.add(nextParentRule);
                }
                adjustFlatRenamePath();
                parse(nextParentRule, currentPath, currentRenamePath, field.getType(), rules);
            }
        }

        private void adjustFlatRenamePath() {
            currentRenamePath = currentRenamePath + rename + "/";
            if (currentRenamePath.startsWith("//")){
                currentRenamePath = currentRenamePath.substring(1);
            }
        }

        private void adjustRenamePath() {

            if (rename.startsWith("/")) {
                currentRenamePath = rename.substring(0, rename.lastIndexOf("/"));
                rename = rename.substring(rename.lastIndexOf("/"));
            }

        }

        protected String currentRenamePath() {
            if (rename.startsWith("/")) {
                rename = rename.substring(1);
                return "/";
            }

            return isFlatten ? renamePath : renamePath + rename + "/";
        }

        protected boolean readFlatten() {
            Rename renameTag = field.getAnnotation(Rename.class);
            return renameTag == null ? false : renameTag.flatten();
        }

        protected String readRename() {
            Rename renameTag = field.getAnnotation(Rename.class);
            return renameTag == null ? field.getName() : renameTag.value();
        }

        protected boolean isBasicType() {
            Class<?> targetType = field.getType();
            return Byte.class.equals(targetType)
                    || Short.class.equals(targetType)
                    || Integer.class.equals(targetType)
                    || Long.class.equals(targetType)

                    || Float.class.equals(targetType)
                    || Double.class.equals(targetType)
                    || Boolean.class.equals(targetType)

                    || Character.class.equals(targetType)

                    || String.class.equals(targetType);
        }

    }


}

package net.narusas.util;

import java.lang.reflect.Field;
import java.util.Collection;
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

        rules = parse(new Rule(), "/", "/", ruleClass, new Rules());
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
            } else if (collectionType()) {
                handleCollection();
            } else {
                if (isFlatten == false) {
                    nextParentRule = new Rule(parentRule, ruleClass, field, parentPath, field.getName(), currentRenamePath, rename);
                    rules.add(nextParentRule);
                }
                adjustFlatRenamePath();
                parse(nextParentRule, currentPath, currentRenamePath, field.getType(), rules);
            }
        }


        private boolean collectionType() {
            return isCollectionType() || isMapType() || isArrayType();
        }

        private boolean isArrayType() {
            return field.getType().isArray();
        }

        private boolean isMapType() {
            return Map.class.isAssignableFrom(field.getType());
        }

        private boolean isCollectionType() {
            return Collection.class.isAssignableFrom(field.getType());
        }

        private void handleCollection() {
            new CollectionParser().process();
        }

        class CollectionParser {

            public void process() {
                nextParentRule = new Rule(parentRule, ruleClass, field, parentPath, field.getName(), currentRenamePath, rename);
                rules.add(nextParentRule);
                adjustFlatRenamePath();


                if (isCollectionType()) {
                    Class nextRuleClass = (Class) nextParentRule.getGenericTypes()[0];
                    if (isBasicType(nextRuleClass)) {
                        // basic type의 구조를 분석할 필요는 없음
                        return;
                    }
                    parse(nextParentRule, currentPath, currentRenamePath, nextRuleClass, rules);
                } else if (isMapType()) {
                    Class keyClass = (Class) nextParentRule.getGenericTypes()[0];

                    // Map의 경우 key는 basic type이여야 함
                    if (isBasicType(keyClass) == false) {
                        throw new IllegalArgumentException(" Type of map's key must be basic type. path=" + currentPath + " keyType:" + keyClass);
                    }
                    Class nextRuleClass = (Class) nextParentRule.getGenericTypes()[1];
                    if (isBasicType(nextRuleClass)) {
                        // basic type의 구조를 분석할 필요는 없음
                        return;
                    }
                    parse(nextParentRule, currentPath, currentRenamePath, nextRuleClass, rules);
                } else if (isArrayType()) {
                    Class nextRuleClass = (Class) nextParentRule.getGenericTypes()[0];
                    if (isBasicType(nextRuleClass)) {
                        // basic type의 구조를 분석할 필요는 없음
                        return;
                    }
                    parse(nextParentRule, currentPath, currentRenamePath, nextRuleClass, rules);
                }
            }
        }


        private void adjustFlatRenamePath() {
            currentRenamePath = currentRenamePath + rename + "/";
            if (currentRenamePath.startsWith("//")) {
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
            return isBasicType(targetType);
        }

        protected boolean isBasicType(Class targetType) {
            return
                    String.class.equals(targetType)

                            || Integer.class.equals(targetType)
                            || int.class.equals(targetType)

                            || Boolean.class.equals(targetType)
                            || boolean.class.equals(targetType)

                            || Long.class.equals(targetType)
                            || long.class.equals(targetType)


                            || Byte.class.equals(targetType)
                            || byte.class.equals(targetType)

                            || Short.class.equals(targetType)
                            || short.class.equals(targetType)

                            || Float.class.equals(targetType)
                            || float.class.equals(targetType)

                            || Double.class.equals(targetType)
                            || double.class.equals(targetType)

                            || Character.class.equals(targetType)
                            || char.class.equals(targetType)

                    ;
        }

    }


}

package net.narusas.util;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Rule {
    Rule parentRule;

    Class ruleClass;
    Field field;

    String parentPath;
    String[] parentPathTokens;
    String name;

    String renamePath;
    String[] renamePathTokens;
    String rename;

    List<Rule> childs = new ArrayList<>();
    private Type[] types;

    public Rule(Rule parentRule, Class ruleClass, Field field, String parentPath, String name, String renamePath, String rename) {
        this.parentRule = parentRule;
        this.ruleClass = ruleClass;
        this.field = field;
        this.parentPath = parentPath;
        this.parentPathTokens = parentPath.split("/");
        this.name = name;
        this.renamePath = renamePath;
        this.renamePathTokens = renamePath.split("/");
        this.rename = rename;
        parentRule.addChild(this);
        parseType();
    }


    private void addChild(Rule child) {
        childs.add(child);
    }

    public String getPath() {
        return parentPath + name;
    }

    public String getRenamePath() {
        return renamePath + rename;
    }


    @Override
    public String toString() {
        return getPath() + "->" + getRenamePath();
    }

    public void walk(RuleWalker walker) {
        walker.walk(this);
        childs.forEach(child -> walker.walk(child));
    }


    public Class getType() {
        return field.getType();
    }

    public Type[] getGenericTypes() {
        return types;
    }

    private void parseType() {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            return;
        }

        if (((Class) type).isArray()) {
            types = TypeSupports.arrayType((Class) type);
            return;
        }
    }

    public Rule splitRename(String path) {
        return new Rule(parentRule, ruleClass, field, parentPath, name, renamePath.substring(path.length() + 1), rename);
    }


    public <T> void copyTo(Object sourceRoot, T targetRoot, Rule targetRule) {
        if (isCollectionType()) {
            coypyCollection(sourceRoot, targetRoot, targetRule);
        } else if (TypeSupports.isBasicType(field.getType()) == false) {
            // fill paths   과정에서 채워짐
        } else {
            copyBasic(sourceRoot, targetRoot, targetRule);
        }

    }

    private <T> void copyObject(Object sourceRoot, T targetRoot, Rule targetRule) {

    }

    private <T> void coypyCollection(Object sourceRoot, T targetRoot, Rule targetRule) {
        List sourceItems = (List) readProperty(sourceRoot);
        List targetList = new ArrayList();
        targetRule.writeProperty(targetRoot, targetList);

        for (Object sourceItem : sourceItems) {
            if (TypeSupports.isBasicType(sourceItem.getClass())) {
                targetList.add(sourceItem);
            } else {
                LeftToRightConvert convert = new LeftToRightConvert(sourceItem, targetRule.getType());
                targetList.add(convert.doConvert());
            }

        }
    }

    private Object createInstance() {
        try {
            return field.getType().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void copyBasic(Object sourceRoot, T targetRoot, Rule targetRule) {
        Object property = readProperty(sourceRoot);
        targetRule.writeProperty(targetRoot, property);
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

    private Object readProperty(Object sourceRoot) {

        Object property = sourceRoot;
        for (String path : parentPathTokens) {
            if ("".equals(path)) {
                continue;
            }

            property = readSubProperty(property, path);
            if (property == null) {
                return null;
            }
        }
        property = readSubProperty(property, getName());
        return property;

    }

    Object readSubProperty(Object parent, String name) {
        try {
            Field field = parent.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(parent);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private <T> void writeProperty(T targetRoot, Object property) {
        try {

            Object holder = fillPaths(targetRoot);

            String path = name;

            Field field = holder.getClass().getDeclaredField(path);
            field.setAccessible(true);
            field.set(holder, typeMatch(field.getType(), property));


        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Object typeMatch(Class<?> type, Object property) {
        if (type.equals(property.getClass())) {
            return property;
        }
        if (String.class.equals(type)) {
            return String.valueOf(property);
        }
        if (String.class.equals(property.getClass())) {
            String strProperty = (String) property;
            if (Integer.class.equals(type) || int.class.equals(type)) {
                return Integer.parseInt(strProperty);
            }
            if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                return Boolean.parseBoolean(strProperty);
            }
            if (Long.class.equals(type) || long.class.equals(type)) {
                return Long.parseLong(strProperty);
            }
            if (Float.class.equals(type) || float.class.equals(type)) {
                return Float.parseFloat(strProperty);
            }
            if (Byte.class.equals(type) || byte.class.equals(type)) {
                return Byte.parseByte(strProperty);
            }
            if (Short.class.equals(type) || short.class.equals(type)) {
                return Short.parseShort(strProperty);
            }
            if (Double.class.equals(type) || double.class.equals(type)) {
                return Double.parseDouble(strProperty);
            }
            if (Character.class.equals(type) || char.class.equals(type)) {
                return (strProperty == null || strProperty.length() != 1) ? null : strProperty.charAt(0);
            }
            //@TODO BigDecimal BigInteger, AtomicInteger...등등

        }


        return property;
    }

    protected <T> Object fillPaths(T targetRoot) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Object holder = targetRoot;
        for (String path : parentPathTokens) {

            if ("".equals(path)) {
                continue;
            }
            Field field = holder.getClass().getDeclaredField(path);
            field.setAccessible(true);
            Object nextHolder = field.get(holder);
            if (nextHolder == null) {
                if (TypeSupports.isCollectionType(field.getType())) {
                    nextHolder = new ArrayList();
                } else {
                    nextHolder = field.getType().newInstance();

                }
                field.set(holder, nextHolder);
            }
            holder = nextHolder;
        }
        return holder;
    }
}

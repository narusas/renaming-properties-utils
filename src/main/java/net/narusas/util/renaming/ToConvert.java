package net.narusas.util.renaming;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 변환 룰이 target에 있을때 사용하는 컨버터
 *
 * @param <T>
 */
public class ToConvert<T> {

    private Object source;
    private final Rules sourceRules;
    private Class<T> targetClass;
    private final Rules targetRules;

    public ToConvert(Object source, Class<T> targetClass) {
        this.source = source;
        this.sourceRules = Rules.of(source.getClass());
        this.targetClass = targetClass;
        this.targetRules = Rules.of(targetClass);
    }

    public T doConvert() {

        try {
            enableSecurity(targetClass);
            Constructor<?> c = targetClass.getDeclaredConstructors()[0];
            c.setAccessible(true);
            T targetRoot = (T) c.newInstance();

            String previous = null;

            /**
             * 컬렉션 내용물은 별도의 컨버팅을 하기 때문에 컬렉션의 이름으로 시작하는 베이직 속성을 무시하면 됨
             * rule은 정렬 되어 있고 다른 문자열로 시작하는 것이 나오기 전까지만 비교하면 됨
             */
            for (Rule targetRule : targetRules) {

                if (targetRule.isCollectionType()) {
                    previous = targetRule.getPath();
                    copyCollection(source, sourceRules, targetRoot, targetRule);
                } else if (TypeSupports.isBasicType(targetRule.getType()) == false) {
                    // 객체는 basci property에서 채워짐
                } else {
                    if (previous != null && targetRule.getPath().startsWith(previous)) {
                        continue;
                    }
                    copyBasic(source, sourceRules, targetRoot, targetRule);
                }
            }
            return targetRoot;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void copyCollection(Object sourceRoot, Rules sourceRules, T targetRoot, Rule targetRule) {
        Rule sourceRule = sourceRules.find(targetRule.getRenamePath());
        List sourceItems = (List) readProperty(sourceRoot, sourceRule);
        List targetList = new ArrayList();
        writeProperty(targetRoot, targetRule, targetList);
        for (Object sourceItem : sourceItems) {
            if (TypeSupports.isBasicType(sourceItem.getClass())) {
                targetList.add(sourceItem);
            } else {

                ToConvert convert = new ToConvert(sourceItem, (Class) targetRule.getGenericTypes()[0]);
                targetList.add(convert.doConvert());
            }

        }
    }

    private void copyBasic(Object sourceRoot, Rules sourceRules, T targetRoot, Rule targetRule) {
        Rule sourceRule = sourceRules.find(targetRule.getRenamePath());
        Object property = readProperty(sourceRoot, sourceRule);
        if (property == null) {
            return;
        }
        writeProperty(targetRoot, targetRule, property);
    }

    private Object readProperty(Object root, Rule sourceRule) {

        Object property = root;
        for (String path : sourceRule.parentPathTokens) {
            if ("".equals(path)) {
                continue;
            }

            property = readSubProperty(property, path);
            if (property == null) {
                return null;
            }
        }
        property = readSubProperty(property, sourceRule.getName());
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

    private <T> void writeProperty(T targetRoot, Rule targetRule, Object property) {
        try {

            Object holder = fillPaths(targetRoot, targetRule);

            String path = targetRule.name;

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

    protected <T> Object fillPaths(T targetRoot, Rule rule) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Object holder = targetRoot;
        for (String path : rule.parentPathTokens) {

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


    private void enableSecurity(Class<T> targetClass) {

        for (Constructor c : targetClass.getDeclaredConstructors()) {
            c.setAccessible(true);
        }
    }
}
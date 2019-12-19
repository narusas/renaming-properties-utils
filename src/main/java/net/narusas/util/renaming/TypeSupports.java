package net.narusas.util.renaming;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class TypeSupports {
    static boolean collectionType(Class type) {
        return isCollectionType(type) || isMapType(type) || isArrayType(type);
    }

    static boolean isArrayType(Class type) {
        return type.isArray();
    }

    static boolean isMapType(Class type) {
        return Map.class.isAssignableFrom(type);
    }

    static boolean isCollectionType(Class type) {
        return Collection.class.isAssignableFrom(type);
    }

    static boolean isBasicType(Class targetType) {
        return String.class.equals(targetType)

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

                || BigDecimal.class.equals(targetType)
                || BigInteger.class.equals(targetType)
                ;
    }

    static Type[] arrayType(Class type) {
        return new Type[]{type.getComponentType()};

    }

    final static Class[] listClasses = new Class[]{Vector.class, ArrayList.class, LinkedList.class, Stack.class, Queue.class};
    final static Class[] setClasses = new Class[]{HashSet.class, TreeSet.class};

    public static Collection createCollection(Class type) {
        try {

            if (List.class.isAssignableFrom(type)) {
                Class target = findClass(listClasses, type);
                if (target == null) {
                    return new ArrayList();
                }
                return (List) target.newInstance();
            }
            if (Set.class.isAssignableFrom(type)) {
                Class target = findClass(setClasses, type);
                if (target == null) {
                    return new HashSet();
                }
                return (Set) target.newInstance();
            }


            return new ArrayList();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Class findClass(Class[] klasses, Class type) {
        for (Class klass : klasses) {
            if (klass.isAssignableFrom(type)) {
                return klass;
            }
        }
        return null;
    }

    static Object typeMatch(Class<?> type, Object property) {
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

    static <T> void writeProperty(T targetRoot, Rule targetRule, Object property) {
        try {

            Object holder = TypeSupports.fillPaths(targetRoot, targetRule);

            String path = targetRule.name;

            Field field = holder.getClass().getDeclaredField(path);
            field.setAccessible(true);
            field.set(holder, TypeSupports.typeMatch(field.getType(), property));


        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static <T> Object fillPaths(T targetRoot, Rule rule) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
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


    static Object readProperty(Object root, Rule sourceRule) {

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

    static Object readSubProperty(Object parent, String name) {
        try {

            Field field = parent.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(parent);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static <T> void copyBasic(Object sourceRoot, Rule sourceRule, T targetRoot, Rule targetRule) {
        Object property = TypeSupports.readProperty(sourceRoot, sourceRule);
        if (property == null) {
            return;
        }
        TypeSupports.writeProperty(targetRoot, targetRule, property);
    }

    static  <T> void coypyArray(Object sourceRoot, Rule sourceRule, T targetRoot, Rule targetRule) {

        Object arrayObj = TypeSupports.readProperty(sourceRoot, sourceRule);
        Class<?> componentType = arrayObj.getClass().getComponentType();


        int length = Array.getLength(arrayObj);
        Object targetArray = Array.newInstance((Class) targetRule.getGenericTypes()[0], length);

        TypeSupports.writeProperty(targetRoot, targetRule, targetArray);

        for (int i = 0; i < length; i++) {
            Object sourceItem = Array.get(arrayObj, i);
            if (TypeSupports.isBasicType(sourceItem.getClass())) {
                Array.set(targetArray, i, sourceItem);

            } else {
                FromConvert convert = new FromConvert(sourceItem, (Class) targetRule.getGenericTypes()[0]);
                Array.set(targetArray, i, convert.doConvert());

            }
        }
    }


}

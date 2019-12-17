package net.narusas.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

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
        try {
            if (type == byte[].class) {
                return new Type[]{byte.class};
            }
            if (type == char[].class) {
                return new Type[]{byte.class};
            }
            if (type == short[].class) {
                return new Type[]{short.class};
            }
            if (type == int[].class) {
                return new Type[]{int.class};
            }
            if (type == long[].class) {
                return new Type[]{long.class};
            }

            if (type == float[].class) {
                return new Type[]{float.class};
            }
            if (type == double[].class) {
                return new Type[]{float.class};
            }
            if (type == boolean[].class) {
                return new Type[]{boolean.class};
            }
            return new Type[]{Class.forName(type.getTypeName().substring(0, type.getTypeName().length() - 2))};
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}

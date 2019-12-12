import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

abstract class Converter {
    protected Map<String, Field> getFieldsMap(Field[] fields) {
        Map<String, Field> targetFieldSet = new HashMap<String, Field>();
        for (Field targetField : fields) {
            targetFieldSet.put(targetField.getName(), targetField);
        }
        return targetFieldSet;
    }


    protected Object convert(Class<?> targetType, Class<?> sourceType, Object sourceValue) {
        // 지원하는 타입 변경은 String -> Primitive Type임
        if (String.class.equals(sourceType)) {
            String sourceValueStr = (String) sourceValue;
            if (Byte.class.equals(targetType)) {
                return Byte.valueOf(sourceValueStr);
            }
            if (Short.class.equals(targetType)) {
                return Short.valueOf(sourceValueStr);
            }
            if (Integer.class.equals(targetType)) {
                return Integer.valueOf(sourceValueStr);
            }

            if (Long.class.equals(targetType)) {
                return Long.valueOf(sourceValueStr);
            }

            if (Float.class.equals(targetType)) {
                return Float.valueOf(sourceValueStr);
            }

            if (Double.class.equals(targetType)) {
                return Double.valueOf(sourceValueStr);
            }

            if (Boolean.class.equals(targetType)) {
                return Boolean.valueOf(sourceValueStr);
            }
            if (String.class.equals(targetType)) {
                return sourceValueStr;
            }
        }

        return null;
    }

    protected boolean isBasicType(Class<?> targetType) {
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

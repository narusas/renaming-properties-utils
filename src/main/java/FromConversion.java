import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class FromConversion<T> extends Converter {

    private Object source;
    private Class targetClass;
    T target;
    Class<?> sourceClass;
    Map<String, Field> sourceFieldSet;
    Map<String, Field> targetFieldSet;
    Set<Field> remainsSources;

    public <T> FromConversion(Object source, Class<T> targetClass) {
        this.source = source;
        this.targetClass = targetClass;
        this.sourceClass = source.getClass();
        this.sourceFieldSet = getFieldsMap(sourceClass.getDeclaredFields());
        this.targetFieldSet = getFieldsMap(targetClass.getDeclaredFields());
        this.remainsSources = new HashSet<Field>(sourceFieldSet.values());
    }

    public T convert() {
        createTarget();
        renamingCopy();
        plainCopy();
        return target;

    }

    private void createTarget() {
        try {
            target = (T) targetClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void renamingCopy() {
        for (Field sourceField : sourceFieldSet.values()) {
            Rename rename = sourceField.getAnnotation(Rename.class);
            if (rename == null) {
                continue;
            }
            copyValue(target, rename.value(), sourceField, sourceField.getName());
            remainsSources.remove(rename.value());
        }
    }


    private void plainCopy() {
        for (Field remainField : remainsSources) {
            Field targetField = targetFieldSet.get(remainField.getName());
            if (targetField == null) {
                continue;
            }
            copyValue(target, remainField.getName(), remainField, remainField.getName());
        }
    }


    private void copyValue(Object target, String targetName, Field sourceField, String soureceName) {
        try {
            if (targetName == null) {
                return;
            }
            if (targetName.contains(".") == false) {
                Field targetField = target.getClass().getDeclaredField(targetName);
                Class targetType = targetField.getType();
                if (targetField.getType().isAssignableFrom(sourceField.getType())) {
                    targetField.set(target, sourceField.get(source));
                    return;
                }
                if (isBasicType(targetField.getType())){
                    Object convertedValue = convert(targetField.getType(), sourceField.getType(), sourceField.get(source));
                    targetField.set(target, convertedValue);
                    return;
                }
                copyNestedValue(target, targetName, sourceField, soureceName);
                return;
            }

            copyPathedValue(target, targetName, sourceField);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyNestedValue(Object target, String targetName, Field sourceField, String soureceName) {
    }


    private void copyPathedValue(Object target, String targetName, Field sourceField) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        // path detpth를 만족할때까지 빈 객체를 채운다
        Object next = target;
        Field nextTargetField = null;
        String nextName = targetName;

        while (true) {

            String targetFieldName = nextName.contains(".") ? nextName.substring(0, nextName.indexOf(".")) : nextName;

            nextTargetField = next.getClass().getDeclaredField(targetFieldName);

            Object nextTarget = nextTargetField.get(next);

            if (nextTarget == null) {
                nextTarget = nextTargetField.getType().newInstance();

                nextTargetField.set(next, nextTarget);

            }

            next = nextTarget;


            nextName = nextName.substring(nextName.indexOf(".") + 1);

            if (nextName.contains(".") == false) {
                break;
            }
        }
        nextTargetField = next.getClass().getDeclaredField(nextName);

        Object convertedValue = convert(nextTargetField.getType(), sourceField.getType(), sourceField.get(source));
        nextTargetField.set(next, convertedValue);
    }


}

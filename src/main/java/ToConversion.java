import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ToConversion<T> extends Converter {

    private final Object source;
    private final Class targetClass;
    T target;
    Class<?> sourceClass;
    Map<String, Field> sourceFieldSet;
    Map<String, Field> targetFieldSet;
    Set<Field> remainsTargets;

    public <T> ToConversion(Object source, Class<T> targetClass) {
        this.source = source;
        this.targetClass = targetClass;
        this.sourceClass = source.getClass();
        this.sourceFieldSet = getFieldsMap(sourceClass.getDeclaredFields());
        this.targetFieldSet = getFieldsMap(targetClass.getDeclaredFields());


        this.remainsTargets = new HashSet<Field>(targetFieldSet.values());
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
        for (Field targetField : targetFieldSet.values()) {
            Rename rename = targetField.getAnnotation(Rename.class);
            if (rename == null) {
                continue;
            }
            copyValue(target, targetField, rename.value());
            remainsTargets.remove(targetField);
        }
    }


    private void plainCopy() {
        for (Field targetField : remainsTargets) {

            if (targetField == null) {
                continue;
            }
            copyValue(target, targetField, targetField.getName());
        }
    }

    private void copyValue(T target, Field targetField, String sourceName) {
        try {
            if (sourceName.contains(".") == false) {
                Field sourceField = source.getClass().getDeclaredField(sourceName);
                if (targetField.getType().isAssignableFrom(sourceField.getType())) {
                    targetField.set(target, sourceField.get(source));
                    return;
                }
                Object convertedValue = convert(targetField.getType(), sourceField.getType(), sourceField.get(source));
                targetField.set(target, convertedValue);
                return;
            }

            Object next = source;
            Field nextSourceField = null;
            String nextName = sourceName;

            while (true) {
                String sourceFieldName = nextName.contains(".") ? nextName.substring(0, nextName.indexOf(".")) : nextName;
                nextSourceField = next.getClass().getDeclaredField(sourceFieldName);
                Object nextTarget = nextSourceField.get(next);
                if (nextTarget == null) {
                    return;
                }
                next = nextTarget;
                nextName = nextName.substring(nextName.indexOf(".") + 1);

                if (nextName.contains(".") == false) {
                    break;
                }
            }
            nextSourceField = next.getClass().getDeclaredField(nextName);
            Object sourceValue = nextSourceField.get(next);

            Object convertedValue = convert(targetField.getType(), nextSourceField.getType(), sourceValue);
            targetField.set(target, convertedValue);

        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    private void copyValue(Object target, String targetName, Field sourceField, String soureceName) {
        try {
            if (targetName.contains(".") == false) {
                Field targetField = target.getClass().getDeclaredField(targetName);

                if (targetField.getType().isAssignableFrom(sourceField.getType())) {
                    targetField.set(target, sourceField.get(source));
                    return;
                }
                Object convertedValue = convert(targetField.getType(), sourceField.getType(), sourceField.get(source));
                targetField.set(target, convertedValue);
                return;
            }
            if (targetName == null) {
                return;
            }


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


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

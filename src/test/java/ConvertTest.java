import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConvertTest {

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq1A {
        String name;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class ServiceReq1B {
        String name;
    }

    @Test
    void simple() {
        LeftToRightConvert<ServiceReq1B> convert = new LeftToRightConvert(new WebReq1A("John"), ServiceReq1B.class);
        ServiceReq1B b = convert.doConvert();
        assertEquals("John", b.name);
    }


    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq2A {
        @Rename("nm")
        String name;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class ServiceReq2B {
        String nm;
    }

    @Test
    void rename() {
        LeftToRightConvert<ServiceReq2B> convert = new LeftToRightConvert(new WebReq2A("John"), ServiceReq2B.class);
        ServiceReq2B b = convert.doConvert();
        assertEquals("John", b.nm);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq3A {
        @Rename("nm")
        String name;
        WebReq3B b;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq3B {
        String code;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class ServiceReq3A {
        String nm;
        ServiceReq3B b;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    static class ServiceReq3B {
        String code;
    }

    @Test
    void 복합객체() {
        LeftToRightConvert<ServiceReq3A> convert = new LeftToRightConvert(new WebReq3A("John", new WebReq3B("CD001")), ServiceReq3A.class);
        ServiceReq3A b = convert.doConvert();
        assertEquals("John", b.nm);
        assertEquals("CD001", b.b.code);
    }


    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq4A {
        @Rename("nm")
        String name;

        @Rename(flatten = true)
        WebReq4B b;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq4B {
        String code;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class ServiceReq4A {
        String nm;
        String code;
    }

    @Test
    void 복합객체_flatten() {
        LeftToRightConvert<ServiceReq4A> convert = new LeftToRightConvert(new WebReq4A("John", new WebReq4B("CD001")), ServiceReq4A.class);
        ServiceReq4A b = convert.doConvert();
        assertEquals("John", b.nm);
        assertEquals("CD001", b.code);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq5A {
        @Rename("nm")
        String name;

        @Rename("cd")
        List<String> codes;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class ServiceReq5A {
        String nm;
        List<String> cd;
    }


    @Test
    void StringList() {
        LeftToRightConvert<ServiceReq5A> convert = new LeftToRightConvert(new WebReq5A("John", Arrays.asList("CD001", "CD002")), ServiceReq5A.class);
        ServiceReq5A b = convert.doConvert();
        assertEquals("John", b.nm);
        assertEquals("CD001", b.cd.get(0));
        assertEquals("CD002", b.cd.get(1));

    }
}

class LeftToRightConvert<T> {

    private final Object source;
    private final Rules sourceRules;
    private final Class<T> targetClass;
    private final Rules targetRules;

    public LeftToRightConvert(Object source, Class<T> targetClass) {
        this.source = source;
        this.sourceRules = Rules.of(source.getClass());
        this.targetClass = targetClass;
        this.targetRules = Rules.of(targetClass);
    }

    public T doConvert() {
        try {
            T target = targetClass.newInstance();
            for (Rule rule : sourceRules) {
                copy(source, target, rule);
            }


            return target;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void copy(Object source, T target, Rule rule) {
        String sourcePath = rule.getPath();
        Object sourceProperty = readProperty(source, sourcePath);
        if (sourceProperty == null) {
            return;
        }
        String targetPath = rule.getRenamePath();
        if (collectionType(sourceProperty.getClass())) {
            writeCollectionProperty(source, target, rule, sourceProperty);
        } else {
            new WriteProperty(target, targetPath, sourceProperty).write();
            
        }


    }


    private Object readProperty(Object source, String sourcePath) {
        try {
            Object property = source;
            String[] paths = sourcePath.split("/");
            for (String path : paths) {
                if ("".equals(path)) {
                    continue;
                }

                Field field = property.getClass().getDeclaredField(path);
                property = field.get(property);
                if (property == null) {
                    return null;
                }
            }
            return property;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    class WriteProperty<T> {


        T target;
        String targetPath;
        Object sourceProperty;

        Object holder;
        private final String[] paths;

        public WriteProperty(T target, String targetPath, Object sourceProperty) {
            this.target = target;
            this.targetPath = targetPath;
            this.sourceProperty = sourceProperty;

            holder = target;
            paths = targetPath.split("/");
        }

        void write(){
            try {

                fillPaths();

                String path = paths[paths.length - 1];
                Field field = holder.getClass().getDeclaredField(path);
                if (isBasicType(field.getType())) {
                    field.set(holder, sourceProperty);
                } else if (isCollectionType(field.getType())) {
                    List list = (List) holder;
                    list.add(sourceProperty);
                } else {
                    Object nextHolder = field.get(holder);
                    if (nextHolder == null) {
                        nextHolder = field.getType().newInstance();
                        field.set(holder, nextHolder);
                    }
                }


            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        protected void fillPaths() throws NoSuchFieldException, IllegalAccessException, InstantiationException {
            for (int i = 0; i < paths.length - 1; i++) {
                String path = paths[i];
                if ("".equals(path)) {
                    continue;
                }
                Field field = holder.getClass().getDeclaredField(path);
                Object nextHolder = field.get(holder);
                if (nextHolder == null) {
                    if (isCollectionType(field.getType())) {
                        nextHolder = new ArrayList();
                    } else {
                        nextHolder = field.getType().newInstance();

                    }
                    field.set(holder, nextHolder);
                }
                holder = nextHolder;
            }
        }
    }

    class WriteCollectionProperty {

    }

    private void writeCollectionProperty(Object source, T target, Rule rule, Object sourceProperty) {
        try {
            Object holder = target;
            String[] paths = rule.getRenamePath().split("/");
            for (int i = 0; i < paths.length - 1; i++) {
                String path = paths[i];
                if ("".equals(path)) {
                    continue;
                }
                Field field = holder.getClass().getDeclaredField(path);
                Object nextHolder = field.get(holder);
                if (nextHolder == null) {
                    if (isCollectionType(field.getType())) {
                        nextHolder = new ArrayList();
                    } else {
                        nextHolder = field.getType().newInstance();

                    }
                    field.set(holder, nextHolder);
                }
                holder = nextHolder;
            }


            String path = paths[paths.length - 1];
            Field field = holder.getClass().getDeclaredField(path);
            List wrapper = new ArrayList();
            field.set(holder, wrapper);
            List sourcePropertyList = (List) sourceProperty;

            Rules subPaths = sourceRules.subRenamePaths(rule);

            for (Object sourceItem : sourcePropertyList) {

            }


        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean collectionType(Class type) {
        return isCollectionType(type) || isMapType(type) || isArrayType(type);
    }

    private boolean isArrayType(Class type) {
        return type.isArray();
    }

    private boolean isMapType(Class type) {
        return Map.class.isAssignableFrom(type);
    }

    private boolean isCollectionType(Class type) {
        return Collection.class.isAssignableFrom(type);
    }

    protected boolean isBasicType(Class targetType) {
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

                ;
    }
}

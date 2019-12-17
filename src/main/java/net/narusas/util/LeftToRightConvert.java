package net.narusas.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class LeftToRightConvert<T> {

    private final Object sourceRoot;
    private final Rules sourceRules;
    private final Class<T> targetClass;
    private final Rules targetRules;

    public LeftToRightConvert(Object source, Class<T> targetClass) {
        this.sourceRoot = source;
        this.sourceRules = Rules.of(source.getClass());
        this.targetClass = targetClass;
        this.targetRules = Rules.of(targetClass);
    }

    public T doConvert() {
        try {
            enableSecurity(targetClass);
            Constructor<?> c = targetClass.getDeclaredConstructors()[0];
            c.setAccessible(true);
            T targetRoot = (T)c.newInstance();
            sourceRules.copyTo(sourceRoot, targetRoot, targetRules);

            return targetRoot;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void enableSecurity(Class<T> targetClass) {

        for (Constructor c : targetClass.getDeclaredConstructors()) {
            c.setAccessible(true);
        }
    }

}

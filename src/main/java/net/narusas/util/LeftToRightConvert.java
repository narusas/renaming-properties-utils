package net.narusas.util;

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
            T targetRoot = targetClass.newInstance();
            sourceRules.copyTo(sourceRoot, targetRoot, targetRules);

            return targetRoot;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}

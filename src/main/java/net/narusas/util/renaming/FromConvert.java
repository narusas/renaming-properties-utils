package net.narusas.util.renaming;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * 변환 룰이 source에 있을때 사용하는 컨버터
 *
 * @param <T>
 */
public class FromConvert<T> {

    private final Object sourceRoot;
    private final Rules sourceRules;
    private final Class<T> targetClass;
    private final Rules targetRules;

    public FromConvert(Object source, Class<T> targetClass) {
        this.sourceRoot = source;
        this.sourceRules = Rules.of(source.getClass());
        this.targetClass = targetClass;
        this.targetRules = Rules.of(targetClass);
    }

    public T doConvert() {
        try {

            T targetRoot = targetClass.newInstance();
            String previous = null;
            for (Rule sourceRule : sourceRules) {
                if (sourceRule.isCollectionType()) {
                    previous = sourceRule.getPath();
                    coypyCollection(sourceRoot, sourceRule, targetRoot, targetRules);
                } else if (sourceRule.isArrayType()) {
                    previous = sourceRule.getPath();
                    coypyArray(sourceRoot, sourceRule, targetRoot, targetRules);
                } else if (TypeSupports.isBasicType(sourceRule.field.getType()) == false) {
                    // fill paths   과정에서 채워짐
                } else {
                    if (previous != null && sourceRule.getPath().startsWith(previous)) {
                        continue;
                    }
                    copyBasic(sourceRoot, sourceRule, targetRoot, targetRules);
                }
            }


            return targetRoot;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    private <T> void copyBasic(Object sourceRoot, Rule sourceRule, T targetRoot, Rules targetRules) {
        Rule targetRule = targetRules.find(sourceRule.getRenamePath());
        if (targetRule == null) {
            return;
        }
        TypeSupports.copyBasic(sourceRoot, sourceRule, targetRoot, targetRule);

    }

    private <T> void coypyCollection(Object sourceRoot, Rule sourceRule, T targetRoot, Rules targetRules) {

        if (sourceRule.isUnpacking()) {
            List sourceItems = (List) TypeSupports.readProperty(sourceRoot, sourceRule);

            Rules unpackingTargetRules = targetRules.startsWith(sourceRule.getUnpackingPrefix());
            for (int i = 0; i < unpackingTargetRules.size(); i++) {
                Rule unpackTarget = unpackingTargetRules.get(i);
                TypeSupports.writeProperty(targetRoot, unpackTarget, sourceItems.get(i));
            }


        } else {
            Rule targetRule = targetRules.find(sourceRule.getRenamePath());
            Collection sourceItems = (Collection) TypeSupports.readProperty(sourceRoot, sourceRule);
            Collection targetList = TypeSupports.createCollection(targetRule.getType());
            TypeSupports.writeProperty(targetRoot, targetRule, targetList);

            for (Object sourceItem : sourceItems) {
                if (TypeSupports.isBasicType(sourceItem.getClass())) {
                    targetList.add(sourceItem);
                } else {
                    FromConvert convert = new FromConvert(sourceItem, (Class) targetRule.getGenericTypes()[0]);
                    targetList.add(convert.doConvert());
                }

            }
        }


    }

    private void coypyArray(Object sourceRoot, Rule sourceRule, T targetRoot, Rules targetRules) {
        Rule targetRule = targetRules.find(sourceRule.getRenamePath());
        if (targetRule == null) {
            return;
        }
        TypeSupports.coypyArray(sourceRoot, sourceRule, targetRoot, targetRule);

    }


}

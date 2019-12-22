package net.narusas.util.renaming;

import java.lang.reflect.Constructor;
import java.util.*;

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
    T targetRoot;
    String previous;

    public ToConvert(Object source, Class<T> targetClass) {
        this.source = source;
        this.sourceRules = Rules.of(source.getClass());
        this.targetClass = targetClass;
        this.targetRules = Rules.of(targetClass);
        this.targetRoot = newInstance();
    }

    private T newInstance() {
        try {
            for (Constructor c : targetClass.getDeclaredConstructors()) {
                c.setAccessible(true);
            }
            return targetClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 컬렉션 내용물은 별도의 컨버팅을 하기 때문에 컬렉션의 이름으로 시작하는 베이직 속성을 무시하면 됨
     * rule은 정렬 되어 있고 다른 문자열로 시작하는 것이 나오기 전까지만 비교하면 됨
     */

    public T doConvert() {
        for (Rule targetRule : targetRules) {
            if (targetRule.isCollectionType()) {
                copyCollection(source, sourceRules, targetRoot, targetRule, targetRules);
            } else if (targetRule.isArrayType()) {
                coypyArray(source, sourceRules, targetRoot, targetRule);
            } else if (TypeSupports.isBasicType(targetRule.getType()) == false) {
                // 객체는 basci property에서 채워짐
            } else {
                if (isChildOfCollection(targetRule)) {
                    continue;
                }
                copyBasic(source, sourceRules, targetRoot, targetRule);
            }
        }
        return targetRoot;
    }

    private boolean isChildOfCollection(Rule targetRule) {
        return previous != null && targetRule.getPath().startsWith(previous);
    }

    private void coypyArray(Object source, Rules sourceRules, T targetRoot, Rule targetRule) {
        previous = targetRule.getPath();
        Rule sourceRule = sourceRules.find(targetRule.getRenamePath());
        TypeSupports.coypyArray(source, sourceRule, targetRoot, targetRule);
    }


    private void copyBasic(Object sourceRoot, Rules sourceRules, T targetRoot, Rule targetRule) {
        if (targetRule.isUnpacking()) {
            copyUnpackings(sourceRoot, sourceRules, targetRoot, targetRule);
        } else {
            copyNormalBasic(sourceRoot, sourceRules, targetRoot, targetRule);
        }
    }

    private void copyNormalBasic(Object sourceRoot, Rules sourceRules, T targetRoot, Rule targetRule) {
        Rule sourceRule = sourceRules.find(targetRule.getRenamePath());
        TypeSupports.copyBasic(sourceRoot, sourceRule, targetRoot, targetRule);
    }

    private void copyUnpackings(Object sourceRoot, Rules sourceRules, T targetRoot, Rule targetRule) {
        // 소스에서 컬렉션 객체 하나를 읽어 오는것임
        Rule sourceRule = sourceRules.find(targetRule.getUnpackingPrefix());
        List sourceProperty = (List) TypeSupports.readProperty(sourceRoot, sourceRule);


        int index = targetRules.unpackIndex(targetRule);
        targetRule.setValue(targetRoot, sourceProperty.get(index));
    }

    private void copyCollection(Object sourceRoot, Rules sourceRules, T targetRoot, Rule targetRule, Rules targetRules) {
        previous = targetRule.getPath();
        if (targetRule.isPacking()) {
            copyPackingCollection(sourceRoot, sourceRules, targetRoot, targetRule);
        } else {
            copyNormalCollection(sourceRoot, sourceRules, targetRoot, targetRule);
        }
    }

    private void copyNormalCollection(Object sourceRoot, Rules sourceRules, T targetRoot, Rule targetRule) {
        Rule sourceRule = sourceRules.find(targetRule.getRenamePath());
        Object sourceProperty = TypeSupports.readProperty(sourceRoot, sourceRule);
        Collection sourceItems = (Collection) sourceProperty;
        Collection targetList = TypeSupports.createCollection(targetRule.getType());
        TypeSupports.writeProperty(targetRoot, targetRule, targetList);
        for (Object sourceItem : sourceItems) {
            if (TypeSupports.isBasicType(sourceItem.getClass())) {
                targetList.add(sourceItem);
            } else {
                ToConvert convert = new ToConvert(sourceItem, (Class) targetRule.getGenericTypes()[0]);
                targetList.add(convert.doConvert());
            }
        }
    }

    private void copyPackingCollection(Object sourceRoot, Rules sourceRules, T targetRoot, Rule targetRule) {
        Rules collectingSourceRules = sourceRules.startsWith(targetRule.getPackingPrefix());
        for (Rule sourceRule : collectingSourceRules) {
            Object sourceProperty = TypeSupports.readProperty(sourceRoot, sourceRule);
            Collection storedValue = (Collection) targetRule.getValue(targetRoot);
            if (storedValue == null) {
                storedValue = TypeSupports.createCollection((Class) targetRule.getGenericTypes()[0]);
                targetRule.setValue(targetRoot, storedValue);
            }
            storedValue.add(sourceProperty);
        }
    }
}

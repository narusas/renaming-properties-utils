package net.narusas.util.renaming;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    public ToConvert(Object source, Class<T> targetClass) {
        this.source = source;
        this.sourceRules = Rules.of(source.getClass());
        this.targetClass = targetClass;
        this.targetRules = Rules.of(targetClass);
    }

    public T doConvert() {

        try {
            enableSecurity(targetClass);
            Constructor<?> c = targetClass.getDeclaredConstructors()[0];
            c.setAccessible(true);
            T targetRoot = (T) c.newInstance();

            String previous = null;

            /**
             * 컬렉션 내용물은 별도의 컨버팅을 하기 때문에 컬렉션의 이름으로 시작하는 베이직 속성을 무시하면 됨
             * rule은 정렬 되어 있고 다른 문자열로 시작하는 것이 나오기 전까지만 비교하면 됨
             */
            for (Rule targetRule : targetRules) {
                Rule sourceRule = sourceRules.find(targetRule.getRenamePath());
                if (targetRule.isCollectionType()) {
                    previous = targetRule.getPath();
                    copyCollection(source, sourceRule, targetRoot, targetRule);
                } else if (targetRule.isArrayType()) {
                    previous = targetRule.getPath();
                    coypyArray(source, sourceRule, targetRoot, targetRule);
                } else if (TypeSupports.isBasicType(targetRule.getType()) == false) {
                    // 객체는 basci property에서 채워짐
                } else {
                    if (previous != null && targetRule.getPath().startsWith(previous)) {
                        continue;
                    }
                    copyBasic(source, sourceRule, targetRoot, targetRule);
                }
            }
            return targetRoot;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void coypyArray(Object source, Rule sourceRule, T targetRoot, Rule targetRule) {

        TypeSupports.coypyArray(source, sourceRule, targetRoot, targetRule);
    }


    private void copyBasic(Object sourceRoot, Rule sourceRule, T targetRoot, Rule targetRule) {

        TypeSupports.copyBasic(sourceRoot, sourceRule, targetRoot, targetRule);

    }

    private void copyCollection(Object sourceRoot, Rule sourceRule, T targetRoot, Rule targetRule) {


        Collection sourceItems = (Collection) TypeSupports.readProperty(sourceRoot, sourceRule);
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


    void enableSecurity(Class<T> targetClass) {

        for (Constructor c : targetClass.getDeclaredConstructors()) {
            c.setAccessible(true);
        }
    }
}

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
class Rule {
    Rule parentRule;

    Class ruleClass;
    Field field;

    String parentPath;
    String name;

    String renamePath;
    String rename;

    List<Rule> childs = new ArrayList<>();
    private Type[] types;

    public Rule(Rule parentRule, Class ruleClass, Field field, String parentPath, String name, String renamePath, String rename) {
        this.parentRule = parentRule;
        this.ruleClass = ruleClass;
        this.field = field;
        this.parentPath = parentPath;
        this.name = name;
        this.renamePath = renamePath;
        this.rename = rename;
        parentRule.addChild(this);
        parseType();
    }


    private void addChild(Rule child) {
        childs.add(child);
    }

    public String getPath() {
        return parentPath + name;
    }

    public String getRenamePath() {
        return renamePath + rename;
    }


    @Override
    public String toString() {
        return getPath() + "->" + getRenamePath();
    }

    public void walk(RuleWalker walker) {
        walker.walk(this);
        childs.forEach(child -> walker.walk(child));
    }


    public Class getType() {
        return field.getType();
    }

    public Type[] getGenericTypes() {
        return types;
    }

    private void parseType() {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            return;
        }

        if (((Class) type).isArray()) {
            types = arrayType((Class) type);
            return;
        }
        types = new Type[]{type};
    }


    private Type[] arrayType(Class type) {
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

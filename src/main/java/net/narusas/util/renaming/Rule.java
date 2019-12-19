package net.narusas.util.renaming;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class Rule {


    Class ruleClass;
    Field field;

    String parentPath;
    String[] parentPathTokens;
    String name;

    String renamePath;
    String[] renamePathTokens;
    String rename;

    List<Rule> childs = new ArrayList<>();
    private Type[] types;

    public Rule(Class ruleClass, Field field, String parentPath, String name, String renamePath, String rename) {

        this.ruleClass = ruleClass;
        this.field = field;
        this.parentPath = parentPath;
        this.parentPathTokens = parentPath.split("/");
        this.name = name;
        this.renamePath = renamePath;
        this.renamePathTokens = renamePath.split("/");
        this.rename = rename;

        parseGenericType();
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

    private void parseGenericType() {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            return;
        }

        if (((Class) type).isArray()) {
            types = TypeSupports.arrayType((Class) type);
            return;
        }
    }

    public Rule splitRename(String path) {
        return new Rule(ruleClass, field, parentPath, name, renamePath.substring(path.length() + 1), rename);
    }


    private Object createInstance() {
        try {
            return field.getType().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private boolean collectionType() {
        return isCollectionType() || isMapType() || isArrayType();
    }

    boolean isArrayType() {
        return field.getType().isArray();
    }

    private boolean isMapType() {
        return Map.class.isAssignableFrom(field.getType());
    }

    boolean isCollectionType() {
        return Collection.class.isAssignableFrom(field.getType());
    }


}



import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TypeTokenTest {

    @Test
    public void 필드로_선언된_리스트의_제네릭_타입구하기() throws NoSuchFieldException {
        Class<A> aClass = A.class;
        Field field = aClass.getDeclaredField("b");
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        assertEquals(List.class, type.getRawType());
        assertEquals(1, type.getActualTypeArguments().length);
        assertEquals(String.class, type.getActualTypeArguments()[0]);
    }

    @Test
    public void 필드로_선언된_맵의_제네릭_타입구하기() throws NoSuchFieldException {
        Class<B> aClass = B.class;
        Field field = aClass.getDeclaredField("a");
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        assertEquals(Map.class, type.getRawType());
        assertEquals(2, type.getActualTypeArguments().length);
        assertEquals(String.class, type.getActualTypeArguments()[0]);
        assertEquals(A.class, type.getActualTypeArguments()[1]);
    }

    @Test
    public void 필드로_선언된_배열의_제네릭_타입구하기() throws NoSuchFieldException {
        // 배열은 문자열 기반으로 작업해야 함
        Class<C> aClass = C.class;
        assertEquals("int[]", aClass.getDeclaredField("a").getGenericType().getTypeName());
        assertEquals("java.util.List[]", aClass.getDeclaredField("b").getGenericType().getTypeName());
        assertEquals("java.lang.Integer[]", aClass.getDeclaredField("c").getGenericType().getTypeName());
    }

    @Test
    void assignable() {
        assertTrue(Collection.class.isAssignableFrom(ArrayList.class));
        assertTrue(Collection.class.isAssignableFrom(List.class));
        assertTrue(Collection.class.isAssignableFrom(Set.class));
        assertTrue(Collection.class.isAssignableFrom(HashSet.class));
        assertTrue(Collection.class.isAssignableFrom(Vector.class));

        assertFalse(Collection.class.isAssignableFrom(HashMap.class), "컬렉션 기본 타입이 Collection/Map 이며 서로는 호환되지 않음");

        assertTrue(Map.class.isAssignableFrom(HashMap.class));
        assertTrue(Map.class.isAssignableFrom(Hashtable.class));
        assertTrue(Map.class.isAssignableFrom(ConcurrentHashMap.class));
    }
}

class A {
    List<String> b;
}

class B {
    Map<String, A> a;
}

class C {
    int[] a;
    java.util.List[] b;
    Integer[] c;
}
package tests;

import net.narusas.util.renaming.Rename;
import net.narusas.util.renaming.Rules;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class RulesTest {
    static class WebReq1 {
        String name;
        String phone;
    }

    @Test
    void simpleRule() {
        Rules rules = Rules.of(WebReq1.class);
        assertEquals("/name->/name", rules.get(0).toString());
        assertEquals("/phone->/phone", rules.get(1).toString());
    }

    static class WebReq2 {
        String name;

        @Rename("phn")
        String phone;
    }

    @Test
    void simpleRename() {
        Rules rules = Rules.of(WebReq2.class);
        assertEquals("/name->/name", rules.get(0).toString());
        assertEquals("/phone->/phn", rules.get(1).toString());
    }

    static class WebReq3A {
        String name;
        WebReq3B b;
    }

    static class WebReq3B {
        String phone;
        String code;
    }

    @Test
    void 복합객체() {
        Rules rules = Rules.of(WebReq3A.class);
        assertEquals("/b->/b", rules.get(0).toString());
        assertEquals("/b/code->/b/code", rules.get(1).toString());
        assertEquals("/b/phone->/b/phone", rules.get(2).toString());
        assertEquals("/name->/name", rules.get(3).toString());
    }

    static class WebReq4A {
        @Rename("nm")
        String name;

        @Rename("bb")
        WebReq4B b;
    }

    static class WebReq4B {
        @Rename("phn")
        String phone;

        @Rename("cd")
        String code;
    }

    @Test
    void 복합객체_rename() {
        Rules rules = Rules.of(WebReq4A.class);
        assertEquals("/b->/bb", rules.get(0).toString());
        assertEquals("/b/code->/bb/cd", rules.get(1).toString());
        assertEquals("/b/phone->/bb/phn", rules.get(2).toString());
        assertEquals("/name->/nm", rules.get(3).toString());
    }

    static class WebReq5A {
        @Rename("nm")
        String name;

        @Rename(flatten = true)
        WebReq4B b;
    }

    @Test
    void 복합객체_rename_flatten() {
        Rules rules = Rules.of(WebReq5A.class);
        assertEquals("/b/code->/cd", rules.get(0).toString());
        assertEquals("/b/phone->/phn", rules.get(1).toString());
        assertEquals("/name->/nm", rules.get(2).toString());
    }

    static class WebReq6A {
        @Rename("nm")
        String name;

        @Rename("b/phn")
        String phone;

    }

    @Test
    void 하위_경로로() {
        Rules rules = Rules.of(WebReq6A.class);
        assertEquals("/name->/nm", rules.get(0).toString());
        assertEquals("/phone->/b/phn", rules.get(1).toString());
    }

    static class WebReq7A {
        @Rename("nm")
        String name;

        @Rename("bb")
        WebReq7B b;
    }

    static class WebReq7B {
        @Rename("cd")
        String code;

        @Rename("cc")
        WebReq7C c;
    }

    static class WebReq7C {
        @Rename("phn")
        String phone;
    }

    @Test
    void 복합객체_밑에_복합객체() {
        Rules rules = Rules.of(WebReq7A.class);
        assertEquals("/b->/bb", rules.get(0).toString());
        assertEquals("/b/c->/bb/cc", rules.get(1).toString());
        assertEquals("/b/c/phone->/bb/cc/phn", rules.get(2).toString());
        assertEquals("/b/code->/bb/cd", rules.get(3).toString());
    }


    static class WebReq8A {
        @Rename("nm")
        String name;

        @Rename(flatten = true)
        WebReq8B b;
    }

    static class WebReq8B {
        @Rename("/cd")
        String code;

        @Rename("/ee/cc")
        WebReq8C c;
    }

    static class WebReq8C {
        @Rename("phn")
        String phone;
    }

    @Test
    void 절대경로로_rename() {
        Rules rules = Rules.of(WebReq8A.class);
        assertEquals("/b/c->/ee/cc", rules.get(0).toString());
        assertEquals("/b/c/phone->/ee/cc/phn", rules.get(1).toString(), "/b/c 가 /ee/cc로 rename 되었기 때문에 WebReq8C.phone은 그 하위로 들어가야 한다");
        assertEquals("/b/code->/cd", rules.get(2).toString());

        assertEquals("/name->/nm", rules.get(3).toString());
    }

    static class WebReq9A {
        String a;
        int b;
        List<String> c;
        Set<String> d;
        Map<String, String> e;
        String[] f;
        int[] g;

    }

    static class WebReq9B {
        String h;
    }


    @Test
    void 타입분석_제레릭포함() {
        Rules rules = Rules.of(WebReq9A.class);
        assertEquals("/a->/a", rules.get(0).toString());
        assertEquals(String.class, rules.get(0).getType());

        assertEquals("/b->/b", rules.get(1).toString());
        assertEquals(int.class, rules.get(1).getType());

        assertEquals("/c->/c", rules.get(2).toString());
        assertEquals(List.class, rules.get(2).getType());
        assertEquals(String.class, rules.get(2).getGenericTypes()[0]);

        assertEquals("/d->/d", rules.get(3).toString());
        assertEquals(Set.class, rules.get(3).getType());
        assertEquals(String.class, rules.get(3).getGenericTypes()[0]);

        assertEquals("/e->/e", rules.get(4).toString());
        assertEquals(Map.class, rules.get(4).getType());
        assertEquals(String.class, rules.get(4).getGenericTypes()[0]);
        assertEquals(String.class, rules.get(4).getGenericTypes()[1]);


        assertEquals(String[].class, rules.get(5).getType());
        assertEquals(String.class, rules.get(5).getGenericTypes()[0]);

        assertEquals(int[].class, rules.get(6).getType());
        assertEquals(int.class, rules.get(6).getGenericTypes()[0]);
    }

    static class WebReq10A {
        String name;
        List<WebReq10B> a;
    }

    static class WebReq10B {
        String code;
    }

    @Test
    void 리스트() {
        Rules rules = Rules.of(WebReq10A.class);
        assertEquals("/a->/a", rules.get(0).toString());
        assertEquals(List.class, rules.get(0).getType());
        assertEquals(WebReq10B.class, rules.get(0).getGenericTypes()[0]);
        assertEquals("/a/code->/a/code", rules.get(1).toString());
        assertEquals(String.class, rules.get(1).getType());

        assertEquals("/name->/name", rules.get(2).toString());
    }

    static class WebReq11A {
        String name;
        Map<String, WebReq11B> a;
    }

    static class WebReq11B {
        String code;
    }

    @Test
    void 맵() {
        Rules rules = Rules.of(WebReq11A.class);

        assertEquals("/a->/a", rules.get(0).toString());
        assertEquals(String.class, rules.get(0).getGenericTypes()[0]);
        assertEquals(WebReq11B.class, rules.get(0).getGenericTypes()[1]);

        // 맵의 key 까지 복합 객체를 지원하기에는 너무 복잡해져서 일단 베이직 타입만 지원하기로 함
        // 맵의 키에 대한 description이 필요 없기 때문에 value 에 대한 구조 description만 있으면 됨
        assertEquals("/a/code->/a/code", rules.get(1).toString());

        assertEquals("/name->/name", rules.get(2).toString());
    }

    static class WebReq12A {
        String name;
        WebReq12B[] a;
    }

    static class WebReq12B {
        String code;
    }

    @Test
    void 배열() {
        Rules rules = Rules.of(WebReq12A.class);
        assertEquals("/a->/a", rules.get(0).toString());
        assertEquals(WebReq12B.class, rules.get(0).getGenericTypes()[0]);
        assertEquals("/a/code->/a/code", rules.get(1).toString());
        assertEquals("/name->/name", rules.get(2).toString());

    }
}



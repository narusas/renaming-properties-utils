package tests;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.narusas.util.renaming.FromConvert;
import net.narusas.util.renaming.Rename;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class FromConvertTest {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq1A {
        String name;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq1B {
        String name;
    }

    @Test
    void simple() {
        FromConvert<ServiceReq1B> convert = new FromConvert(new WebReq1A("John"), ServiceReq1B.class);
        ServiceReq1B b = convert.doConvert();
        assertEquals("John", b.name);
    }


    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq2A {
        @Rename("nm")
        String name;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq2B {
        String nm;
    }

    @Test
    void rename() {
        FromConvert<ServiceReq2B> convert = new FromConvert(new WebReq2A("John"), ServiceReq2B.class);
        ServiceReq2B b = convert.doConvert();
        assertEquals("John", b.nm);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq3A {
        @Rename("nm")
        String name;
        WebReq3B b;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq3B {
        String code;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq3A {
        String nm;
        ServiceReq3B b;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq3B {
        String code;
    }

    @Test
    void 복합객체() {
        FromConvert<ServiceReq3A> convert = new FromConvert(new WebReq3A("John", new WebReq3B("CD001")), ServiceReq3A.class);
        ServiceReq3A b = convert.doConvert();
        assertEquals("John", b.nm);
        assertEquals("CD001", b.b.code);
    }


    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq4A {
        @Rename("nm")
        String name;

        @Rename(flatten = true)
        WebReq4B b;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq4B {
        String code;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq4A {
        String nm;
        String code;
    }

    @Test
    void 복합객체_flatten() {
        FromConvert<ServiceReq4A> convert = new FromConvert(new WebReq4A("John", new WebReq4B("CD001")), ServiceReq4A.class);
        ServiceReq4A b = convert.doConvert();
        assertEquals("John", b.nm);
        assertEquals("CD001", b.code);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq5A {
        @Rename("nm")
        String name;

        @Rename("cd")
        List<String> codes;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq5A {
        String nm;
        List<String> cd;
    }


    @Test
    void ListOfString() {
        FromConvert<ServiceReq5A> convert = new FromConvert(new WebReq5A("John", Arrays.asList("CD001", "CD002")), ServiceReq5A.class);
        ServiceReq5A b = convert.doConvert();
        assertEquals("John", b.nm);
        assertEquals("CD001", b.cd.get(0));
        assertEquals("CD002", b.cd.get(1));

    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq6A {
        @Rename("nm")
        String name;

        @Rename("cd")
        List<WebReq6ACode> codes;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq6ACode {
        String code;
        String no;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq6A {
        String nm;
        List<ServiceReq6B> cd;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq6B {
        String code;
        int no;
    }

    @Test
    void ListOfObject() {
        WebReq6A webReq = new WebReq6A("John", Arrays.asList(new WebReq6ACode("CD", "1"), new WebReq6ACode("CD", "2")));
        FromConvert<ServiceReq6A> convert = new FromConvert<>(webReq, ServiceReq6A.class);
        ServiceReq6A serviceReq = convert.doConvert();
        assertEquals("John", serviceReq.nm);
        assertEquals(2, serviceReq.cd.size());
        assertEquals("CD", serviceReq.cd.get(0).code);
        assertEquals(1, serviceReq.cd.get(0).no);
        assertEquals("CD", serviceReq.cd.get(1).code);
        assertEquals(2, serviceReq.cd.get(1).no);

    }

    @AllArgsConstructor
    public static class WebReq7 {
        String name;
        String[] codes;
    }

    @NoArgsConstructor
    public static class ServiceReq7 {
        String name;
        String[] codes;
    }

    @Test
    void 문자열배열() {
        WebReq7 webReq = new WebReq7("John", new String[]{"CD001", "CD002"});

        FromConvert<ServiceReq7> convert = new FromConvert<>(webReq, ServiceReq7.class);
        ServiceReq7 serviceReq = convert.doConvert();
        assertEquals("John", serviceReq.name);
        assertEquals(2, serviceReq.codes.length);
        assertEquals("CD001", serviceReq.codes[0]);
        assertEquals("CD002", serviceReq.codes[1]);

    }

    @AllArgsConstructor
    public static class WebReq8 {
        String name;
        WebReq8Code[] codes;
    }

    @AllArgsConstructor
    public static class WebReq8Code {
        String code;
        String no;
    }

    @NoArgsConstructor
    public static class ServiceReq8 {
        String name;
        ServiceReq8Code[] codes;
    }

    @NoArgsConstructor
    public static class ServiceReq8Code {
        String code;
        int no;
    }


    @Test
    void 객체배열() {
        WebReq8 webReq = new WebReq8("John", new WebReq8Code[]{new WebReq8Code("CD", "1"), new WebReq8Code("CD", "2")});
        FromConvert<ServiceReq8> convert = new FromConvert<>(webReq, ServiceReq8.class);
        ServiceReq8 serviceReq = convert.doConvert();

        assertEquals("John", serviceReq.name);
        assertEquals(2, serviceReq.codes.length);
        assertEquals("CD", serviceReq.codes[0].code);
        assertEquals(1, serviceReq.codes[0].no);
        assertEquals("CD", serviceReq.codes[1].code);
        assertEquals(2, serviceReq.codes[1].no);
    }

    @AllArgsConstructor
    public static class WebRequest9 {
        @Rename("names")
        String name1;
        @Rename("names")
        String name2;
        @Rename("names")
        String name3;

    }

    public static class ServiceRequest9 {
        List<String> names;
    }

    @Test
    void 리스트로_수집() {
        WebRequest9 webReq = new WebRequest9("John", "Micheal","Kale");
        FromConvert<ServiceRequest9> convert = new FromConvert<>(webReq, ServiceRequest9.class);
        ServiceRequest9 serviceReq = convert.doConvert();
        assertEquals(3, serviceReq.names.size());
        assertEquals("John", serviceReq.names.get(0));
        assertEquals("Micheal", serviceReq.names.get(1));
        assertEquals("Kale", serviceReq.names.get(2));

    }
}


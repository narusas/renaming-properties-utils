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
}


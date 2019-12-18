package tests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.narusas.util.Rename;
import net.narusas.util.ToConvert;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ToConvertTest {

    @AllArgsConstructor
    public static class ServiceRes2 {
        String name;
        String code;
    }

    public static class WebRes2 {
        String name;
        String code;
    }

    @Test
    void 단순_변환() {
        ServiceRes2 serviceRes = new ServiceRes2("John", "CD001");
        ToConvert<WebRes2> convert = new ToConvert<>(serviceRes, WebRes2.class);
        WebRes2 webRes = convert.doConvert();
        assertEquals("John", webRes.name);
        assertEquals("CD001", webRes.code);

    }


    @Data
    @AllArgsConstructor
    public static class ServiceRes1 {
        ServiceDto_Person person;
    }

    @Data
    @AllArgsConstructor
    public static class ServiceDto_Person {
        String firstName;
        String middleName;
        String lastName;

    }

    public static class WebResponse1 {
        @Rename("person/firstName")
        String name1;
        @Rename("person/middleName")
        String name2;
        @Rename("person/lastName")
        String name3;
    }

    @Test
    void 경로변경() {
        ServiceRes1 serviceRes = new ServiceRes1(new ServiceDto_Person("John", null, "Smith"));

        ToConvert<WebResponse1> convert = new ToConvert<>(serviceRes, WebResponse1.class);
        WebResponse1 webRes = convert.doConvert();

        assertEquals("John", webRes.name1);
        assertNull(webRes.name2);
        assertEquals("Smith", webRes.name3);
    }

    @AllArgsConstructor
    public static class ServiceRes3 {
        String name;
        ServiceRes3A a;
    }

    @AllArgsConstructor
    public static class ServiceRes3A {
        String code1;
        String code2;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebRes3 {
        String name;
        WebRes3A a;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebRes3A {
        String code1;
        String code2;
    }


    @Test
    void 중첩객체() {
        ServiceRes3 serviceRes = new ServiceRes3("John", new ServiceRes3A("CD001", "DD002"));
        ToConvert<WebRes3> convert = new ToConvert<>(serviceRes, WebRes3.class);
        WebRes3 webRes = convert.doConvert();
        assertEquals("John", webRes.name);
        assertEquals("CD001", webRes.a.code1);
        assertEquals("DD002", webRes.a.code2);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebRes4 {
        @Rename("name")
        String nm;
        @Rename("a")
        WebRes4A b;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebRes4A {
        @Rename("code1")
        String cd1;
        @Rename("code2")
        String cd2;
    }

    @Test
    void 중첩객체_이름변경() {
        ServiceRes3 serviceRes = new ServiceRes3("John", new ServiceRes3A("CD001", "DD002"));
        ToConvert<WebRes4> convert = new ToConvert<>(serviceRes, WebRes4.class);
        WebRes4 webRes = convert.doConvert();
        assertEquals("John", webRes.nm);
        assertEquals("CD001", webRes.b.cd1);
        assertEquals("DD002", webRes.b.cd2);
    }


    @AllArgsConstructor
    public static class ServiceRes5 {
        String name;
        ServiceRes5A a;
    }

    @AllArgsConstructor
    public static class ServiceRes5A {
        String code1;
        String code2;

        ServiceRes5B b;

    }

    @AllArgsConstructor
    public static class ServiceRes5B {
        String section1;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebRes5 {
        String name;

        @Rename("/a/b/section1")
        String section;

        @Rename("/a")
        WebRes5A code;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebRes5A {
        String code1;
        String code2;
    }


    @Test
    void 중첩객체_밑에_중첩객체() {
        ServiceRes5 serviceRes = new ServiceRes5("John", new ServiceRes5A("CD001", "CD002", new ServiceRes5B("SEC_1")));
        ToConvert<WebRes5> convert = new ToConvert<>(serviceRes, WebRes5.class);
        WebRes5 webRes = convert.doConvert();
        assertEquals("John", webRes.name);
        assertEquals("SEC_1", webRes.section);
        assertEquals("CD001", webRes.code.code1);
        assertEquals("CD002", webRes.code.code2);
    }

    @AllArgsConstructor
    public static class ServiceRes6 {
        String name;
        List<String> codes;
    }


    @NoArgsConstructor
    public static class WebRes6 {
        String name;
        List<String> codes;
    }


    @Test
    void listOfString() {
        ServiceRes6 serviceRes = new ServiceRes6("John", Arrays.asList("CD001", "CD002"));
        ToConvert<WebRes6> convert = new ToConvert<>(serviceRes, WebRes6.class);
        WebRes6 webRes = convert.doConvert();
        assertEquals("John", webRes.name);
        assertEquals(2, webRes.codes.size());
        assertEquals("CD001", webRes.codes.get(0));
        assertEquals("CD002", webRes.codes.get(1));
    }


    @AllArgsConstructor
    public static class ServiceRes7 {
        String name;
        List<ServiceRes7Code> codes;
    }

    @AllArgsConstructor
    public static class ServiceRes7Code {
        String code1;
        String code2;
    }


    @NoArgsConstructor
    public static class WebRes7 {
        @Rename("name")
        String nm;

        @Rename("codes")
        List<WebRes7Code> cds;
    }

    @NoArgsConstructor
    public static class WebRes7Code {
        @Rename("code1")
        String cd1;
        @Rename("code2")
        String cd2;
    }

    @Test
    void 복합객체_리스트() {
        ServiceRes7 serviceRes = new ServiceRes7("John", Arrays.asList(
                new ServiceRes7Code("CD001", "SUB01")
                , new ServiceRes7Code("CD002", "SUB02")
        ));
        WebRes7 webRes  = new ToConvert<>(serviceRes, WebRes7.class).doConvert();
        assertEquals("John", webRes.nm);
        assertEquals(2, webRes.cds.size());
        assertEquals("CD001", webRes.cds.get(0).cd1);
        assertEquals("SUB01", webRes.cds.get(0).cd2);
        assertEquals("CD002", webRes.cds.get(1).cd1);
        assertEquals("SUB02", webRes.cds.get(1).cd2);

    }


}

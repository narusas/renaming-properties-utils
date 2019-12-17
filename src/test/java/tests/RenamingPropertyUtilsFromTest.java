package tests;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.narusas.util.Rename;
import net.narusas.util.RenamingPropertyUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RenamingPropertyUtilsFromTest {

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq1A {
        String name;
        String phone;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    static class ServiceReq1A {
        String name;
        String phone;
    }





    @Test
    void 동일필드명() {
        WebReq1A a = new WebReq1A("John", "01099999999");
        ServiceReq1A b = RenamingPropertyUtils.from(a, ServiceReq1A.class);
        assertEquals("John", b.name);
        assertEquals("01099999999", b.phone);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq2 {
        @Rename("name")
        String nm;

        String phn;
    }

    @Test
    void 일부_단순변환룰_일부필드_다른_이름일때() {
        WebReq2 c = new WebReq2("John", "01099999999");
        ServiceReq1A a = RenamingPropertyUtils.from(c, ServiceReq1A.class);
        assertEquals("John", a.name, "rename 규칙이 있기 때문에 nm -> name으로 전환");
        assertNull(a.phone, "동일한 필드명도 없고 rename 규칙이 없기 때문에 전환되지 말아야함");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq3 {
        @Rename("name")
        String nm;

        @Rename( "phone")
        String phn;
    }


    @Test
    void 단순변환룰() {
        WebReq3 d = new WebReq3("John", "01099999999");
        ServiceReq1A a = RenamingPropertyUtils.from(d, ServiceReq1A.class);
        assertEquals("John", a.name, "rename 규칙이 있기 때문에 nm -> name으로 전환");
        assertEquals("01099999999", a.phone, "rename 규칙이 있기 때문에 nm -> name으로 전환");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReq4 {
        String name;
        Integer phone;
    }

    @Test
    void 동일필드명_다른_타입() {
        ServiceReq1A a = new ServiceReq1A("John", "01099999999");
        WebReq4 e = RenamingPropertyUtils.from(a, WebReq4.class);
        assertEquals("John", e.name);
        assertEquals(Integer.parseInt("1099999999"), e.phone);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReqA {
        String name;

        @Rename("c.phone")
        String phone;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    static class ServiceReqB {
        String name;
        ServiceReqC c;
    }

    @NoArgsConstructor
    @ToString
    static class ServiceReqC {
        String phone;
    }


    @Test
    void 중첩객체() {
        WebReqA a = new WebReqA("John", "01099999999");
        ServiceReqB b = RenamingPropertyUtils.from(a, ServiceReqB.class);
        assertEquals("John", b.name);
        assertEquals("01099999999", b.c.phone);

    }


    @NoArgsConstructor
    @AllArgsConstructor
    static class WebReqA2 {
        String name;

        @Rename("c.d.phone")
        String phone;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    static class ServiceReqB2 {
        String name;
        ServiceReqC2 c;
    }

    @NoArgsConstructor
    @ToString
    static class ServiceReqC2 {
        ServiceReqD2 d;
    }

    @NoArgsConstructor
    @ToString
    static class ServiceReqD2 {
        String phone;
    }


    @Test
    void 깊은중첩객체() {
        WebReqA2 a = new WebReqA2("John", "01099999999");
        ServiceReqB2 b = RenamingPropertyUtils.from(a, ServiceReqB2.class);
        assertEquals("John", b.name);
        assertEquals("01099999999", b.c.d.phone);

    }

    @AllArgsConstructor
    static class WebReq3A {
        String name;

        WebReq3B b;
    }

    @AllArgsConstructor
    static class WebReq3B {

        String section;

        WebReq3C c;
    }
    @AllArgsConstructor
    static class WebReq3C {
        String code;
    }

    static class ServieReq3A {
        String name;
        ServieReq3B b;
    }

    static class ServieReq3B {
        String section;
        ServieReq3C c;
    }

    static class ServieReq3C {
        String code;
    }




    @Test
    void 복합객체에서_복합객체로(){
        WebReq3A web = new WebReq3A("John", new WebReq3B("Seoul", new WebReq3C("01233")));
        ServieReq3A service = RenamingPropertyUtils.from(web, ServieReq3A.class);
        assertEquals("John", service.name);
        assertEquals("Seoul", service.b.section);
        assertEquals("01233", service.b.c.code);
    }

}






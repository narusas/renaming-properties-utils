package tests;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.narusas.util.Rename;
import net.narusas.util.RenamingPropertyUtils;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class RenamingPropertyUtilToTest {
    @AllArgsConstructor
    static class ServiceResult1 {
        String name;
        String phone;
        String dummy;
    }

    @NoArgsConstructor
    static class WebResponse1 {
        String name;
        String phone;
    }

    @Test
    void 동일필드명() {
        ServiceResult1 serviceResult = new ServiceResult1("John", "01099999999", "dummy");
        WebResponse1 webResponse = RenamingPropertyUtils.to(serviceResult, WebResponse1.class);
        assertEquals("John", webResponse.name);
        assertEquals("01099999999", webResponse.phone);
    }

    @NoArgsConstructor
    static class WebResponse2 {
        String name;

        @Rename("phone")
        String telephone;
    }


    @Test
    void 일부_동일_필드명_일부_단순변환룰() {
        ServiceResult1 serviceResult = new ServiceResult1("John", "01099999999", "dummy");
        WebResponse2 webResponse = RenamingPropertyUtils.to(serviceResult, WebResponse2.class);
        assertEquals("John", webResponse.name);
        assertEquals("01099999999", webResponse.telephone);
    }

    @NoArgsConstructor
    static class WebResponse3 {
        @Rename("name")
        String nm;

        @Rename("phone")
        String telephone;
    }

    @Test
    void 단순변환룰() {
        ServiceResult1 serviceResult = new ServiceResult1("John", "01099999999", "dummy");
        WebResponse3 webResponse = RenamingPropertyUtils.to(serviceResult, WebResponse3.class);
        assertEquals("John", webResponse.nm);
        assertEquals("01099999999", webResponse.telephone);
    }

    @AllArgsConstructor
    static class ServiceResponse4A {
        String name;
        ServiceResponse4B b;
    }

    @AllArgsConstructor
    static class ServiceResponse4B {
        String phone;
    }


    static class WebResponse4 {
        String name;

        @Rename("b/phone")
        String phone;
    }

    @Test
    void 단순중첩() {
        ServiceResponse4A serviceResult = new ServiceResponse4A("John", new ServiceResponse4B("01099999999"));
        WebResponse4 webResponse = RenamingPropertyUtils.to(serviceResult, WebResponse4.class);
        assertEquals("John", webResponse.name);
        assertEquals("01099999999", webResponse.phone);

    }

    @AllArgsConstructor
    static class ServiceResponse5A {
        String name;
        ServiceResponse5B b;
    }

    @AllArgsConstructor
    static class ServiceResponse5B {
        ServiceResponse5C c;
    }

    @AllArgsConstructor
    static class ServiceResponse5C {
        String phone;
    }


    static class WebResponse5 {
        String name;

        @Rename("b/c/phone")
        String phone;
    }

    @Test
    void 깊은중첩() {
        ServiceResponse5A serviceResult = new ServiceResponse5A("John", new ServiceResponse5B(new ServiceResponse5C("01099999999")));
        WebResponse5 webResponse = RenamingPropertyUtils.to(serviceResult, WebResponse5.class);
        assertEquals("John", webResponse.name);
        assertEquals("01099999999", webResponse.phone);

    }
}

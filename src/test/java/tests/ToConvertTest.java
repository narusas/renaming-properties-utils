package tests;


import lombok.AllArgsConstructor;
import lombok.Data;
import net.narusas.util.Rename;
import net.narusas.util.ToConvert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ToConvertTest {
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
    void simple() {
        ServiceRes1 serviceRes = new ServiceRes1(new ServiceDto_Person("John", null, "Smith") );

        ToConvert<WebResponse1> convert = new ToConvert<>(serviceRes, WebResponse1.class);
        WebResponse1 webRes = convert.doConvert();

        assertEquals("John", webRes.name1);
        assertNull(webRes.name2);
        assertEquals("Smith", webRes.name3);
    }
}

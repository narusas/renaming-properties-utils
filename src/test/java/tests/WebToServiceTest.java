package tests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.narusas.util.renaming.Rename;
import net.narusas.util.renaming.RenamingPropertyUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebToServiceTest {
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq1 {
        @Rename("person/sirName")
        String name1;
        @Rename("person/name")
        String name2;

        @Rename("address/country")
        String addr1;
        @Rename("address/city")
        String addr2;
        @Rename("address/detail")
        String addr3;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ServiceReq1 {
        ServiceReq1Person person;
        ServiceReq1Address address;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ServiceReq1Person {
        String sirName;
        String name;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ServiceReq1Address {
        String country;
        String city;
        String detail;
    }


    @Test
    void test1() {
        WebReq1 webReq = new WebReq1("Smith", "John", "USA", "LA", "11123-1");
        ServiceReq1 serviceReq = RenamingPropertyUtils.from(webReq, ServiceReq1.class);
        assertEquals(new ServiceReq1Person("Smith","John"), serviceReq.getPerson());
        assertEquals(new ServiceReq1Address("USA","LA","11123-1"), serviceReq.getAddress());
    }
}

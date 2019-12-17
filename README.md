# Renaming Properties Utils

웹요청 DTO을 서비스 요청 DTO 로 변환하거나, 서비스 응답 DTO를 웹 응답 DTO로 변환하는 것은 매우 지겨운 작업이다. 

이 작업을 어느정도 수월하게 수행하기 위해 속성 복사중 이름/경로 변경을 지원하는 유틸이다 

사용예
```

public class RenamingPropertyUtilsFromTest {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebReq1A {
        String name;
        String phone;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceReq1A {
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
    public static class WebReq2 {
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
    public static class WebReq3 {
        @Rename("name")
        String nm;

        @Rename("phone")
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
    public static class WebReq4 {
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
    public static class WebReqA {
        String name;

        @Rename("c/phone")
        String phone;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ServiceReqB {
        String name;
        ServiceReqC c;
    }

    @NoArgsConstructor
    @ToString
    public static class ServiceReqC {
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
    public static class WebReqA2 {
        String name;

        @Rename("c/d/phone")
        String phone;

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ServiceReqB2 {
        String name;
        ServiceReqC2 c;
    }

    @NoArgsConstructor
    @ToString
    public static class ServiceReqC2 {
        ServiceReqD2 d;
    }

    @NoArgsConstructor
    @ToString
    public static class ServiceReqD2 {
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
    public static class WebReq3A {
        String name;

        WebReq3B b;
    }

    @AllArgsConstructor
    public static class WebReq3B {

        String section;

        WebReq3C c;
    }

    @AllArgsConstructor
    public static class WebReq3C {
        String code;
    }

    public static class ServieReq3A {
        String name;
        ServieReq3B b;
    }

    public static class ServieReq3B {
        String section;
        ServieReq3C c;
    }

    public static class ServieReq3C {
        String code;
    }


    @Test
    void 복합객체에서_복합객체로() {
        WebReq3A web = new WebReq3A("John", new WebReq3B("Seoul", new WebReq3C("01233")));
        ServieReq3A service = RenamingPropertyUtils.from(web, ServieReq3A.class);
        assertEquals("John", service.name);
        assertEquals("Seoul", service.b.section);
        assertEquals("01233", service.b.c.code);
    }

}
```

기본적인 사용법은 `RenamingPropertyUtilsFromTest.java` 를 참조

## Done
* From Copy: 복사 룰이 Left 객체에 있을때

## Todo
* To Copy:  복사 룰이 Right 객체이 있을때 
* BigDecimal, BigInteger, AtomicInteger등 추가 타입 
* Array, Map 지원
* ArrayList 말고 지정된 Collection으로 컬렉션 생성
* 깊은 중첩 객체의 값을 낮은 곳으로 정리하기 
* 여러 변수를 하나의 목록으로 변환. (form에서 phone1, phone2, phone3 등 여러 이름으로 받아서 List<String> phone에 넣을수 있는 기능 `@Rename(value="phone", collect=true)` 정도가 될듯)
* 목록을 복수의 변수명으로 풀기. 위 기능의 반대. `@Rename(value="phone", split=true)`  . phone으로 시작하는 변수명을 정렬하여 순서대로 넣기. 
* 상대 경로 지원 
* 기본 설정. List를 arrylist로 만들지 concurrent list로 만들지 등등. 기본 설정을 할수 있게
* 성능 측정하여 성능 개선. 
* 캐시 기능에서 클래스가 런타임에 변경되는 경우를 지원하기 위해  weak reference 사용하게.
* Validation 지원
* Spring MVC에서 controller에 Web Request class를 지정하고  `@Rule(ServiceReqest.class)` 를 지정하면,   컨버팅 된 객체 바로 받아서 쓸수 있게
```
@Rule(value=ServiceRequest.class, field="as")
public static class WebReq1 {
    ...
    ServiceRequest as;
}
@RequestMapping
public void doSometing(WebReq1 req){
    ServiceRequest serviceReq  = req.as; 
}
```
* .... Too many 
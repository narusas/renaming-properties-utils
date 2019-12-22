# Renaming Properties Utils

***주의: 이 문서는 현재 작성중입니다***

개발중에 웹 요청을 서비스 요청으로 변환하거나  서비스 응답을 웹 요청으로 변환하는 것은 매우 지루한 작업입니다.

그 지루함 때문에 개발자들은 종종 서비스 요청/응답을 위한 DTO를 웹 요청/응답을 위해 사용하려는 유혹에 쉽게 빠지게 되죠.

이런 유혹에 빠지면 얼마 지나지 않아 웹 계층의 요구사항이 서비스 계층 DTO에 스며들고 곧 거대한 진흙 뭉텅이 DTO로 커지게 됩니다. (https://thedomaindrivendesign.io/big-ball-of-mud/)

이 프로젝트는 이런 지루함 때문에 발생하는 이슈를 해결해 보고자 객체 복사 과정에 서로 다른 타입이나 구조로 객체를 복사할수 있는 유틸을 제공합니다. 
 
이 프로젝트에서는 다음과 같은 개발 관례를 추천합니다. 

1. 각 웹 요청별로 전용 웹 요청 클래스를 정의합니다. 이 클래스에 요청과 관련된 스펙을 기술합니다.  웹의 Form 형태에 맞춰 가급적 Flat 한 구조를 가지는게 좋습니다. 
2. 마찬가지로 응답 역시 웹 전용 응답 클래스를 사용하여 복잡한 도메인 구조가 아닌, 웹에 맞게 설계된 응답 규약을 정의합니다. 서비스의 응답이 복잡한 구조를 가질수 있지만 이를 웹에 표시할때는 가급적 flat한 구조로 바꿔서 제공하세요. 또한 불필요한 정보가 누설되지 않게 화면에 필요한 정보만 포함하는게 좋습니다.  
3. 이 요청/응답 전용 DTO 클래스를 컨트롤러의 요청 처리 메소드 위에 정적 클래스로 선언하여 응집성을 높입니다.  


Example
```
import static net.narusas.util.renaming.RenamingPropertyUtils.*;
@Controller
public class ExampleController {

    @RenameFrom("as") 
    public static class UpdatePersonalInfoRequest {
        @Rename("personalInfo/familyName") @NotEmpty            String familyName;
        @Rename("personalInfo/name") @NotEmpty                  String name;
        @Rename("personalInfo/gender") @NotEmpty                String gender;

        @Rename("addressInfo/country")                          String addr1;
        @Rename("addressInfo/city")                             String addr2;
        @Rename("addressInfo/etc")                              String addr3;

        PersonalInfo as;
    }

    public static class UpdatePersonalInfoResponse {
        boolean isUpdated;
        String[] modifiedKeys;
    }


    @RequestMapping(value="/me", methods=POSTs) 
    public UpdatePersonalInfoResponse updatePersonalInfo(@Valid WebRequest1 req) {
        ServiceCallResult serviceResult = req.as;
        return to(serviceResult, UpdatePersonalInfoResponse.class);
     }   
}
```

Note: 별도의 프로젝트로 제공되는 spring 지원기능에 제공되는 `@RenameFrom` 을 이용해 사전이 이미 변경된 객체를 주입받아 사용할수 있습니다.  


## 사용방법

이 유틸리티는 두개의 변환 메소드를 제공합니다. 각각의 메소드는 재활용되는 서비스쪽에는 어떤 요구사항을 넣지 않고 각 개별 웹 요청/응답 클래스에만 변환 규칙을 넣어서 사용할수 있게 하였습니다. 
 

* `from` : 변환 규칙이 source 객체이 있는 변환 메소드. 웹 요청이 source이고 서비스요청으로 변환할때 사용합니다. 
* `to` 변환 규칙이 target 객체에 있는 변환 메소드. 서비스 응답이 source이고 웹 응답으로 변환할때 사용합니다 .

### `from`

#### 기초적인 변환 
```
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
```

#### 일부_단순변환룰_일부필드_다른_이름일때
```
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
```

#### 단순변환룰
```
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
```

#### 동일필드명_다른_타입
```
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
```

#### 중첩객체
```
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
```

#### 깊은중첩객체
```

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
```

#### 복합객체에서_복합객체로
```

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

추가적인  사용법은 `FromConvertTest.java`, `ToConvertTest.java` 를 참조


## Done
* From Copy: 복사 룰이 Left 객체에 있을때
* To Copy:  복사 룰이 Right 객체이 있을때
* 깊은 중첩 객체의 값을 낮은 곳으로 정리하기
* ArrayList 말고 지정된 Collection으로 컬렉션 생성
* Array지원 
* 여러 변수를 하나의 목록으로 변환. (form에서 phone1, phone2, phone3 등 여러 이름으로 받아서 List<String> phone에 넣을수 있는 기능 `@Rename(pack="phone",)` 정도가 될듯)
* 목록을 복수의 변수명으로 풀기. 위 기능의 반대. `@Rename(unpack="phone")`  . phone으로 시작하는 변수명을 정렬하여 순서대로 넣기. 


## Todo


* .... Too many 



## Next Version Todo
* Env/Request에서 값 가져오기  
* unpack/pack에서 List말고 Set, Array 지원하기
* 수 -> String 변환시 Formatter 지원
* 캐시 기능에서 클래스가 런타임에 변경되는 경우를 지원하기 위해  weak reference 사용하게.
* Validation 지원
* 기본 설정. List를 arrylist로 만들지 concurrent list로 만들지 등등. 기본 설정을 할수 있게
* 상대 경로 지원 
* 성능 측정하여 성능 개선. 
* Map 지원
* BigDecimal, BigInteger, AtomicInteger등 추가 타입 
* String to Enum, Enum To String , int to enum, enum to int 지원
* (이 기능은 별도 프로젝트로 진행하기로 함) Spring MVC에서 controller에 Web Request class를 지정하고  `@Rule(ServiceReqest.class)` 를 지정하면,   컨버팅 된 객체 바로 받아서 쓸수 있게
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
 
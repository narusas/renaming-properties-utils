# Renaming Properties Utils

웹요청 DTO을 서비스 요청 DTO 로 변환하거나, 서비스 응답 DTO를 웹 응답 DTO로 변환하는 것은 매우 지겨운 작업이다. 

이 작업을 어느정도 수월하게 수행하기 위해 속성 복사중 이름/경로 변경을 지원하는 유틸이다 

사용예
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

기본적인 사용법은 `RenamingPropertyUtilsFromTest.java` 를 참조

## Done
* From Copy: 복사 룰이 Left 객체에 있을때

## Todo
* To Copy:  복사 룰이 Right 객체이 있을때 
* BigDecimal, BigInteger, AtomicInteger등 추가 타입 
* 깊은 중첩 객체의 값을 낮은 곳으로 정리하기 
* .... Too many 
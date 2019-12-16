import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Rename {
    String value() default "";

    /**
     * 평탄화 여부. 평탄화된 객체의 속성만을 부모의 속성으로 전달함. 컬렉션, 맵, 배열은 평탄화 대상이 될수 없습니다.
     */
    boolean flatten() default false;
}

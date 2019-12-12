import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RenamingPropertyUtils {

    /**
     * 변환 규칙에 소스쪽에 있을때 사용하는 변환 메소드. 보통 웹 요청을 서비스 요청으로 변경할때 사용함
     *
     * @param source
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> T from(Object source, Class<T> targetClass) {
        return (T) new FromConversion(source, targetClass).convert();
    }

    /**
     * 변환 규칙에 타겟 쪽에 있을때 사용하는 변환 메소드. 보통 서비스 응답을 웹 응답으로  변경할때 사용함
     *
     * @param source
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> T to(Object source, Class<T> targetClass) {

        return (T) new ToConversion(source, targetClass).convert();
    }


}



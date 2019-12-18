package net.narusas.util;

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
        FromConvert<T> converter = new FromConvert<T>(source, targetClass);
        return converter.doConvert();
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
        ToConvert<T> converter = new ToConvert<>(source, targetClass);
        return converter.doConvert();
    }


}



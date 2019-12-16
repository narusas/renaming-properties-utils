import java.lang.reflect.Field;

public class FieldUtils {
    public static Object read(Object source, String path) {

        try {
            Field field  = source.getClass().getDeclaredField(path);
            return field.get(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

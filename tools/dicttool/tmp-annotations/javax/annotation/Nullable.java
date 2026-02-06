package javax.annotation;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE, ElementType.LOCAL_VARIABLE})
public @interface Nullable {}
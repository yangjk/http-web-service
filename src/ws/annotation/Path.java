package ws.annotation;

import java.lang.annotation.*;

/**
 * @author: jkyang
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Path {

    String[] value();
    
    String[] paramNames() default {};

}

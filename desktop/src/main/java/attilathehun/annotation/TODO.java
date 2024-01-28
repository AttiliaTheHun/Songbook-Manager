package attilathehun.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A pre-compile time marker annotation implying that a feature is yet to be finished. This annotation does not have any effect.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE})
public @interface TODO {
    boolean required() default false;

    boolean priority() default false;

    String description() default "";
}

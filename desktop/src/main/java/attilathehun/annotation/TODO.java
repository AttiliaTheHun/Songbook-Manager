package attilathehun.annotation;

import java.lang.annotation.*;

/**
 * A pre-compile time marker annotation implying that a feature is yet to be finished. This annotation does not have any effect.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE})
public @interface TODO {
    public boolean required() default false;
    public boolean priority() default false;
    public String description() default "";
}

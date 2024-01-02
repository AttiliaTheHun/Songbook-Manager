package attilathehun.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE})
public @interface TODO {
    public boolean required() default false;
    public boolean priority() default false;
    public String description() default "";
}

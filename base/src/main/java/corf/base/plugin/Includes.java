package corf.base.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that is used to specify the list {@link Extension}'s
 * provided by the given {@link Plugin}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Includes {

    Class<? extends Extension>[] value() default { };
}

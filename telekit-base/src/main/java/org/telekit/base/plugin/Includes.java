package org.telekit.base.plugin;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Includes {

    Class<? extends Extension>[] value() default {};
}

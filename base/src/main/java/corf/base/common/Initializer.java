package corf.base.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method annotated with @Initializer is expected to always be invoked before the object is used.
 * Null-safe type checker respects this annotation when checking field initialization: if a field is
 * assigned a (non-nullable) value in @Initializer-annotated method, it is considered initialized,
 * and hence does not require @Nullable annotation.
 * <p>
 * Methods annotated as @Initializer should not be private. If the actual initialization is
 * happening in a private helper method, then the public method that calls this helper method should
 * be annotated.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface Initializer { }

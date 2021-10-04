package telekit.base;

import org.junit.jupiter.api.extension.ExtendWith;
import telekit.tests.HSQLMemoryDatabaseResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({OrdinaryExtension.class, HSQLMemoryDatabaseResolver.class})
public @interface DatabaseTest {}

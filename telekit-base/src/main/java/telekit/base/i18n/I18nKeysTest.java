package telekit.base.i18n;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

// I18n keys verification util.
// Should only be used for unit tests.
public class I18nKeysTest {

    protected final Class<?> keysClass;
    protected final BundleLoader loader;
    protected final Logger log;

    public I18nKeysTest(Class<?> keysClass, BundleLoader loader) {
        this.keysClass = keysClass;
        this.loader = loader;
        this.log = Logger.getLogger(keysClass.getName());
    }

    public void run(Locale locale) {
        // Remember about ResourceBundle property inheritance. Key-values pairs included in less
        // specific files are inherited by those which are higher in the inheritance tree.
        // So, we basically just test that all Java keys are present in default resource bundle.
        run(collectJavaKeys(), locale);
    }

    protected Set<String> collectJavaKeys() {
        Set<String> result = new HashSet<>();
        for (Field field : keysClass.getDeclaredFields()) {
            if (!isStatic(field.getModifiers()) || !field.getType().isAssignableFrom(String.class)) { continue; }
            try {
                result.add((String) field.get(null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    protected void run(Set<String> javaKeys, Locale locale) {
        List<String> bundleKeys = loader.load(locale).map(Pair::getKey).collect(Collectors.toList());

        Collection<String> notPresentInBundle = CollectionUtils.subtract(javaKeys, bundleKeys);
        Collection<String> notPresentInJava = CollectionUtils.subtract(bundleKeys, javaKeys);

        // all java keys must be present in resource bundle
        // this certainly something we don't want to happen in runtime
        if (isNotEmpty(notPresentInBundle)) {
            throw new AssertionError(String.format(
                    "[%s] Some Java keys are not present in the resource bundle: %s", locale, notPresentInBundle
            ));
        }

        // warn if resource bundle contains extra keys
        // they should be commented or removed
        if (isNotEmpty(notPresentInJava)) {
            log.warning(String.format(
                    "[%s] Resource bundle contains some extra keys, which are not mapped to %s fields: %s",
                    locale, keysClass.getName(), notPresentInBundle
            ));
        }
    }
}

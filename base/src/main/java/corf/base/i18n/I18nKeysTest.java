package corf.base.i18n;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.WARNING;

/**
 * I18n keys verification util.
 * Must only be used for unit tests.
 */
public class I18nKeysTest {

    protected final Class<?> keysClass;
    protected final BundleLoader loader;
    protected final System.Logger logger;

    public I18nKeysTest(Class<?> keysClass, BundleLoader loader) {
        this.keysClass = Objects.requireNonNull(keysClass, "keysClass");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.logger = System.getLogger(keysClass.getName());
    }

    public void run(Locale locale) {
        // Remember about ResourceBundle property inheritance. Key-values pairs included in less
        // specific files are inherited by those which are higher in the inheritance tree.
        // So, we basically just test that all Java keys are present in default resource bundle.
        run(getClassStaticFields(), locale);
    }

    @SuppressWarnings("CatchAndPrintStackTrace")
    protected Set<String> getClassStaticFields() {
        var fields = new HashSet<String>();
        for (Field field : keysClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !field.getType().isAssignableFrom(String.class)) {
                continue;
            }
            try {
                fields.add((String) field.get(null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fields;
    }

    protected void run(Set<String> staticFields, Locale locale) {
        List<String> bundleKeys = loader.load(locale).map(Pair::getKey).collect(Collectors.toList());

        Collection<String> notPresentInBundle = CollectionUtils.subtract(staticFields, bundleKeys);
        Collection<String> notPresentInJava = CollectionUtils.subtract(bundleKeys, staticFields);

        // all java keys must be present in resource bundle
        // this certainly something we don't want to happen in runtime
        if (CollectionUtils.isNotEmpty(notPresentInBundle)) {
            throw new AssertionError(String.format(
                    "[%s] Some Java keys are not present in the resource bundle: %s", locale, notPresentInBundle
            ));
        }

        // warn if resource bundle contains extra keys
        // they should be commented or removed
        if (CollectionUtils.isNotEmpty(notPresentInJava)) {
            logger.log(WARNING, String.format(
                    "[%s] Resource bundle contains some extra keys, which are not mapped to %s fields: %s",
                    locale, keysClass.getName(), notPresentInBundle
            ));
        }
    }
}

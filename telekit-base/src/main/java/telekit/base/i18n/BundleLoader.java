package telekit.base.i18n;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public interface BundleLoader {

    String id();

    Stream<Pair<String, String>> load(Locale locale);

    default Stream<Pair<String, String>> pullFrom(ResourceBundle bundle) {
        return bundle.keySet().stream().map(key -> new ImmutablePair<>(key, bundle.getString(key)));
    }

    static BundleLoader of(Class<?> keysClass) {
        return of(keysClass.getCanonicalName(), keysClass.getClassLoader());
    }

    static BundleLoader of(String baseName, ClassLoader classLoader) {
        return new BundleLoader() {

            @Override
            public String id() { return baseName; }

            @Override
            public Stream<Pair<String, String>> load(Locale locale) {
                // ResourceBundle use UTF-8 since JDK
                // https://openjdk.java.net/jeps/226
                return pullFrom(ResourceBundle.getBundle(baseName, locale, classLoader));
            }
        };
    }
}

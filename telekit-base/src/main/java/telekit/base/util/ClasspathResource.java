package telekit.base.util;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Small utility wrapper to simplify extraction of classpath resources.
 * <p>
 * - Does not support schemas;
 * <p>
 * - Can't contain any special symbols (including tabs or spaces) except
 * slashes, underscores or hyphens;
 * <p>
 * - Alphabets other than english are also prohibited;
 * <p>
 * - When starts from slash (<code>"/"</code>) represents absolute path
 * and relative path, otherwise
 */
public class ClasspathResource {

    private static final String PATH_SEPARATOR = "/";
    private static final String PATH_REGEX = "^[a-zA-Z0-9_\\-/.]+$";

    private final List<String> path;
    private final boolean absolutePath;
    private final Class<?> clazz;

    private ClasspathResource(String path, Class<?> clazz) {
        Objects.requireNonNull(path);
        validatePath(path);

        this.path = split(path);
        this.absolutePath = path.startsWith(PATH_SEPARATOR);
        this.clazz = clazz;
    }

    private ClasspathResource(ClasspathResource that) {
        Objects.requireNonNull(that);

        this.path = new ArrayList<>(that.path);
        this.absolutePath = that.absolutePath;
        this.clazz = that.clazz;
    }

    private void validatePath(String path) {
        if (!path.matches(PATH_REGEX)) { throwError(); }
        // should be probably included into regex
        if ('.' == path.charAt(0)) {
            if (path.length() == 1) { throwError(); }
            if (!Character.isLetterOrDigit(path.charAt(1))) { throwError(); }
        }
    }

    private void throwError() {
        throw new IllegalArgumentException("Invalid path: '" + path + "'");
    }

    private List<String> split(String path) {
        return Arrays.stream(path.split(Pattern.quote(PATH_SEPARATOR)))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    public boolean isAbsolutePath() {
        return absolutePath;
    }

    public ClasspathResource concat(String path) {
        return concat(new ClasspathResource(path, null));
    }

    public ClasspathResource concat(ClasspathResource resource) {
        ClasspathResource copy = new ClasspathResource(this);
        copy.path.addAll(resource.path);
        return copy;
    }

    /** {@link #getResourceAsStream()} */
    public Optional<URL> getResource() {
        Class<?> origin = clazz != null ? clazz : getClass();
        return Optional.ofNullable(origin.getResource(String.valueOf(this)));
    }

    /**
     * Note that defining source class may not be enough. {@link Class#getResourceAsStream(String)}
     * also checks the caller module. So if you call this method from another named module,
     * corresponding package must be explicitly opened in module-info.
     */
    public InputStream getResourceAsStream() {
        Class<?> origin = clazz != null ? clazz : getClass();
        return origin.getResourceAsStream(String.valueOf(this));
    }

    @Override
    public String toString() {
        String result = String.join(PATH_SEPARATOR, path);
        return absolutePath ? PATH_SEPARATOR + result : result;
    }

    public static ClasspathResource of(String path) {
        return of(path, null);
    }

    public static ClasspathResource of(String path, Class<?> clazz) {
        return new ClasspathResource(path, clazz);
    }
}

package corf.base.common;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Small utility wrapper to simplify obtaining and operating with classpath resources.
 * <ul>
 * <li>Does not support URI scheme.</li>
 * <li>Can't contain any special symbols (including tabs or spaces) except
 * slashes, underscores or hyphens.</li>
 * <li>Alphabets other than english are prohibited.</li>
 * <li>When starts from slash (<code>"/"</code>) represents absolute path
 * and relative path, otherwise.</li>
 * </ul>
 */
public class ClasspathResource {

    private static final String PATH_SEPARATOR = "/";
    private static final String PATH_REGEX = "^[a-zA-Z0-9_\\-/.]+$";

    private final List<String> path;
    private final boolean absoluteFlag;
    private final @Nullable Class<?> anchor;

    private ClasspathResource(String path, @Nullable Class<?> anchor) {
        checkPath(path);

        this.path = split(path);
        this.absoluteFlag = path.startsWith(PATH_SEPARATOR);
        this.anchor = anchor;
    }

    private ClasspathResource(ClasspathResource resource) {
        Objects.requireNonNull(resource, "resource");

        this.path = new ArrayList<>(resource.path);
        this.absoluteFlag = resource.absoluteFlag;
        this.anchor = resource.anchor;
    }

    private void checkPath(String path) {
        Objects.requireNonNull(path, "path");
        if (!path.matches(PATH_REGEX)) {
            throwError();
        }

        if ('.' == path.charAt(0) && (path.length() == 1 || !Character.isLetterOrDigit(path.charAt(1)))) {
            throwError();
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
        return absoluteFlag;
    }

    public @Nullable Class<?> getAnchorClass() {
        return anchor;
    }

    public ClasspathResource concat(String path) {
        return concat(new ClasspathResource(path, getAnchorClass()));
    }

    public ClasspathResource concat(ClasspathResource resource) {
        Objects.requireNonNull(resource, "resource");
        var copy = new ClasspathResource(this);
        copy.path.addAll(resource.path);
        return copy;
    }

    /** {@link #getResourceAsStream()} */
    public Optional<URL> getResource() {
        var cls = anchor != null ? anchor : getClass();
        return Optional.ofNullable(cls.getResource(String.valueOf(this)));
    }

    /**
     * Note that defining source class may not be enough. {@link Class#getResourceAsStream(String)}
     * also checks the caller module. So if you call this method from another named module,
     * corresponding package <b>must be explicitly opened in module-info</b>.
     */
    public InputStream getResourceAsStream() {
        var cls = anchor != null ? anchor : getClass();
        return cls.getResourceAsStream(String.valueOf(this));
    }

    @Override
    public String toString() {
        var s = String.join(PATH_SEPARATOR, path);
        return absoluteFlag ? PATH_SEPARATOR + s : s;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static ClasspathResource of(String path) {
        return of(path, null);
    }

    public static ClasspathResource of(String path, @Nullable Class<?> anchor) {
        return new ClasspathResource(path, anchor);
    }
}

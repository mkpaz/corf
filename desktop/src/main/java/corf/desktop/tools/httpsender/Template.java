package corf.desktop.tools.httpsender;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import corf.base.db.Entity;
import corf.base.net.HttpConstants.Method;
import corf.desktop.tools.common.Param;

import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Template extends Entity<Template, UUID> {

    public static final int DEFAULT_WAIT_TIMEOUT = 5;
    public static final Method DEFAULT_METHOD = Method.POST;

    private String name;
    private String uri;
    private Method method = DEFAULT_METHOD;
    private @Nullable String headers;
    private @Nullable String body;
    private @Nullable String description;
    private int waitTimeout = DEFAULT_WAIT_TIMEOUT; // seconds
    private Batch batch = Batch.OFF;
    private Set<Param> params = new TreeSet<>();

    @SuppressWarnings("NullAway.Init")
    public Template() { }

    @SuppressWarnings("NullAway.Init")
    public Template(UUID id,
                    String name,
                    String uri,
                    Method method,
                    @Nullable String headers,
                    @Nullable String body,
                    @Nullable String description,
                    int waitTimeout,
                    @Nullable Batch batch,
                    @Nullable Set<Param> params) {
        super(id);
        this.name = Objects.requireNonNull(name, "name");
        this.uri = Objects.requireNonNull(uri, "uri");
        this.method = Objects.requireNonNull(method, "method");
        this.headers = headers;
        this.body = body;
        this.description = description;
        setWaitTimeout(waitTimeout);
        setBatch(batch);
        setParams(params);
    }

    @SuppressWarnings("NullAway.Init")
    public Template(Template template) {
        this.setId(template.getId());
        this.setName(template.getName());
        this.uri = template.getUri();
        this.method = template.getMethod();
        this.headers = template.getHeaders();
        this.body = template.getBody();
        this.batch = template.getBatch().copy();

        if (template.getParams() != null) {
            Set<Param> params = new TreeSet<>();
            for (Param param : template.getParams()) {
                params.add(param.copy());
            }
            this.params = params;
        }

        this.waitTimeout = template.getWaitTimeout();
        this.description = template.getDescription();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = Objects.requireNonNull(uri, "uri");
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = Objects.requireNonNull(method, "method");
    }

    public @Nullable String getHeaders() {
        return headers;
    }

    public void setHeaders(@Nullable String headers) {
        this.headers = headers;
    }

    public @Nullable String getBody() {
        return body;
    }

    public void setBody(@Nullable String body) {
        this.body = body;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = Math.max(waitTimeout, 0);
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(@Nullable Batch batch) {
        this.batch = Objects.requireNonNullElse(batch, Batch.OFF);
    }

    public Set<Param> getParams() {
        return params;
    }

    public void setParams(@Nullable Set<Param> params) {
        this.params = Objects.requireNonNullElse(params, new TreeSet<>());
    }

    @JsonIgnore
    public boolean isBatchMode() {
        return batch.getSize() > 1;
    }

    @Override
    public int compareTo(@NotNull Template that) {
        if (this == that) { return 0; }
        return String.valueOf(name).compareTo(String.valueOf(that.getName()));
    }

    @Override
    public String toString() {
        return "Template{" +
                "name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                ", method=" + method +
                ", headers='" + headers + '\'' +
                ", body='" + body + '\'' +
                ", description='" + description + '\'' +
                ", waitTimeout=" + waitTimeout +
                ", batch=" + batch +
                ", params=" + params +
                "} " + super.toString();
    }

    @Override
    public Template copy() {
        return new Template(this);
    }

    public Template duplicate() {
        var template = copy();
        template.setId(UUID.randomUUID());
        return template;
    }

    public boolean deepEquals(Template that) {
        if (!Objects.equals(this, that)) { return false; }

        if (!Objects.equals(this.name, that.name)) { return false; }
        if (!Objects.equals(this.uri, that.uri)) { return false; }
        if (!Objects.equals(this.method, that.method)) { return false; }
        if (!Objects.equals(this.headers, that.headers)) { return false; }
        if (!Objects.equals(this.body, that.body)) { return false; }
        if (!Objects.equals(this.description, that.description)) { return false; }
        if (!(this.waitTimeout == that.waitTimeout)) { return false; }

        if (!this.batch.deepEquals(that.batch)) { return false; }

        if (this.params.size() != that.params.size()) { return false; }
        if (this.params.isEmpty()) { return true; }

        // thanks to Java devs for not implementing Set.get() method
        Map<Param, Param> thatParams = that.params.stream().collect(Collectors.toMap(e -> e, e -> e));
        for (var thisParam : this.params) {
            var thatParam = thatParams.get(thisParam);
            if (thatParam == null) { return false; }
            if (!thisParam.deepEquals(thatParam)) { return false; }
        }

        return true;
    }

    public static Template create(String name, String uri, Method method) {
        var template = new Template();
        template.setId(UUID.randomUUID());
        template.setName(name);
        template.setUri(uri);
        template.setMethod(method);
        return template;
    }

    ///////////////////////////////////////////////////////////////////////////

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Batch {

        public static final Batch OFF = new Batch(0, null, null, null);

        private int size = 0;
        private @Nullable String start;
        private @Nullable String end;
        private @Nullable String separator;

        public Batch() { }

        public Batch(int size,
                     @Nullable String start,
                     @Nullable String end,
                     @Nullable String separator) {
            setSize(size);
            this.start = start;
            this.end = end;
            this.separator = separator;
        }

        public Batch(Batch batch) {
            this.size = batch.getSize();
            this.start = batch.getStart();
            this.end = batch.getEnd();
            this.separator = batch.getSeparator();
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = Math.max(size, 0);
        }

        public @Nullable String getStart() {
            return start;
        }

        public void setStart(@Nullable String start) {
            this.start = start;
        }

        public @Nullable String getEnd() {
            return end;
        }

        public void setEnd(@Nullable String end) {
            this.end = end;
        }

        public @Nullable String getSeparator() {
            return separator;
        }

        public void setSeparator(@Nullable String separator) {
            this.separator = separator;
        }

        public Batch copy() {
            return new Batch(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Batch batch = (Batch) o;

            if (size != batch.size) return false;
            if (!Objects.equals(start, batch.start)) return false;
            if (!Objects.equals(end, batch.end)) return false;
            return Objects.equals(separator, batch.separator);
        }

        @SuppressWarnings("RedundantIfStatement")
        public boolean deepEquals(Batch that) {
            if (!Objects.equals(this, that)) { return false; }
            if (!(this.size == that.size)) { return false; }
            if (!Objects.equals(this.start, that.start)) { return false; }
            if (!Objects.equals(this.end, that.end)) { return false; }
            if (!Objects.equals(this.separator, that.separator)) { return false; }
            return true;
        }

        @Override
        public int hashCode() {
            int result = start != null ? start.hashCode() : 0;
            result = 31 * result + (end != null ? end.hashCode() : 0);
            result = 31 * result + size;
            result = 31 * result + (separator != null ? separator.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Batch{" +
                    "size=" + size +
                    ", start='" + start + '\'' +
                    ", end='" + end + '\'' +
                    ", separator='" + separator + '\'' +
                    '}';
        }
    }
}

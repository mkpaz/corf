package corf.desktop.tools.filebuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import corf.base.text.Encoding;
import corf.base.db.Entity;
import corf.base.text.LineSeparator;
import corf.desktop.tools.common.Param;

import java.util.*;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Template extends Entity<Template, UUID> {

    private static final Encoding DEFAULT_ENCODING = Encoding.UTF8;
    private static final LineSeparator DEFAULT_SEPARATOR = LineSeparator.UNIX;

    private String name;
    private @Nullable String header;
    private @Nullable String footer;
    private String pattern;
    private @Nullable String delimiter;
    private @Nullable String description;
    private @Nullable String outputFileName;
    private Encoding encoding = DEFAULT_ENCODING;
    private LineSeparator lineSeparator = DEFAULT_SEPARATOR;
    private Set<Param> params = new TreeSet<>();

    @SuppressWarnings("NullAway.Init")
    public Template() { }

    @SuppressWarnings("NullAway.Init")
    public Template(UUID id,
                    String name,
                    @Nullable String header,
                    @Nullable String footer,
                    @Nullable String delimiter,
                    String pattern,
                    @Nullable String description,
                    @Nullable String outputFileName,
                    @Nullable Encoding encoding,
                    @Nullable LineSeparator lineSeparator,
                    @Nullable Set<Param> params
    ) {
        super(id);
        this.name = Objects.requireNonNull(name, "name");
        this.header = header;
        this.footer = footer;
        this.delimiter = delimiter;
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.description = description;
        this.outputFileName = outputFileName;
        setEncoding(encoding);
        setLineSeparator(lineSeparator);
        setParams(params);
    }

    @SuppressWarnings("NullAway.Init")
    public Template(Template template) {
        this.setId(template.getId());
        this.name = template.getName();
        this.header = template.getHeader();
        this.footer = template.getFooter();
        this.delimiter = template.getDelimiter();
        this.pattern = template.getPattern();
        this.description = template.getDescription();
        this.outputFileName = template.getOutputFileName();
        this.encoding = template.getEncoding();
        this.lineSeparator = template.getLineSeparator();

        if (template.getParams() != null) {
            Set<Param> params = new TreeSet<>();
            for (Param param : template.getParams()) {
                params.add(param.copy());
            }
            this.params = params;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public @Nullable String getHeader() {
        return header;
    }

    public void setHeader(@Nullable String header) {
        this.header = header;
    }

    public @Nullable String getFooter() {
        return footer;
    }

    public void setFooter(@Nullable String footer) {
        this.footer = footer;
    }

    public @Nullable String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(@Nullable String delimiter) {
        this.delimiter = delimiter;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public @Nullable String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(@Nullable String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(@Nullable Encoding encoding) {
        this.encoding = Objects.requireNonNullElse(encoding, DEFAULT_ENCODING);
    }

    public LineSeparator getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(@Nullable LineSeparator lineSeparator) {
        this.lineSeparator = Objects.requireNonNullElse(lineSeparator, DEFAULT_SEPARATOR);
    }

    public Set<Param> getParams() {
        return params;
    }

    public void setParams(@Nullable Set<Param> params) {
        this.params = Objects.requireNonNullElse(params, new TreeSet<>());
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
                ", header='" + header + '\'' +
                ", footer='" + footer + '\'' +
                ", pattern='" + pattern + '\'' +
                ", delimiter='" + delimiter + '\'' +
                ", description='" + description + '\'' +
                ", outputFileName='" + outputFileName + '\'' +
                ", encoding=" + encoding +
                ", lineSeparator=" + lineSeparator +
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
        if (!Objects.equals(this.header, that.header)) { return false; }
        if (!Objects.equals(this.footer, that.footer)) { return false; }
        if (!Objects.equals(this.pattern, that.pattern)) { return false; }
        if (!Objects.equals(this.delimiter, that.delimiter)) { return false; }
        if (!Objects.equals(this.description, that.description)) { return false; }
        if (!Objects.equals(this.outputFileName, that.outputFileName)) { return false; }
        if (!Objects.equals(this.encoding, that.encoding)) { return false; }
        if (!Objects.equals(this.lineSeparator, that.lineSeparator)) { return false; }

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

    public static Template create(String name, String pattern) {
        var template = new Template();
        template.setId(UUID.randomUUID());
        template.setName(name);
        template.setPattern(pattern);
        return template;
    }
}

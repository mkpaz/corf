package org.telekit.desktop.tools.filebuilder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.domain.Encoding;
import org.telekit.base.domain.Entity;
import org.telekit.base.domain.LineSeparator;
import org.telekit.desktop.tools.common.Param;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@JacksonXmlRootElement
public class Template extends Entity<Template, UUID> {

    private String name;
    private @JacksonXmlCData String header;
    private @JacksonXmlCData String footer;
    private @JacksonXmlCData String pattern;
    private String delimiter;
    private Set<Param> params;
    private @JacksonXmlCData String description;
    private Encoding encoding = Encoding.UTF8;
    private LineSeparator lineSeparator = LineSeparator.UNIX;

    public Template() {}

    public Template(Template template) {
        this.setId(template.getId());
        this.setName(template.getName());
        this.header = template.getHeader();
        this.footer = template.getFooter();
        this.delimiter = template.getDelimiter();
        this.pattern = template.getPattern();
        this.encoding = template.getEncoding();
        this.lineSeparator = template.getLineSeparator();
        if (template.getParams() != null) {
            Set<Param> params = new TreeSet<>();
            for (Param param : template.getParams()) {
                params.add(new Param(param));
            }
            this.params = params;
        }
        this.description = template.getDescription();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        if (delimiter != null && DelimiterStringConverter.VALUES.containsKey(delimiter)) {
            this.delimiter = delimiter;
        } else {
            this.delimiter = "";
        }
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Set<Param> getParams() {
        return params;
    }

    public void setParams(Set<Param> params) {
        this.params = params;
    }

    public void addParam(Param param) {
        if (this.params == null) { this.params = new TreeSet<>(); }
        this.params.add(param);
    }

    public void removeParam(Param param) {
        if (this.params != null) { this.params.remove(param); }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public LineSeparator getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(LineSeparator lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    @Override
    public int compareTo(@NotNull Template that) {
        if (this == that) { return 0; }
        return String.valueOf(name).compareTo(String.valueOf(that.getName()));
    }

    @Override
    public String toString() {
        return "Template{" +
                "header='" + header + '\'' +
                ", footer='" + footer + '\'' +
                ", pattern='" + pattern + '\'' +
                ", delimiter='" + delimiter + '\'' +
                ", params=" + params +
                ", description='" + description + '\'' +
                ", encoding=" + encoding +
                ", lineSeparator=" + lineSeparator +
                "} " + super.toString();
    }

    @Override
    public Template deepCopy() {
        return new Template(this);
    }
}

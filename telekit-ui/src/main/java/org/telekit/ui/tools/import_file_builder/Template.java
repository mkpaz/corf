package org.telekit.ui.tools.import_file_builder;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.telekit.base.domain.NamedBean;

import java.util.Set;
import java.util.TreeSet;

@JacksonXmlRootElement
public class Template extends NamedBean<Template> {

    private @JacksonXmlCData String header;
    private @JacksonXmlCData String footer;
    private @JacksonXmlCData String pattern;
    private String delimiter;
    private Set<Param> params;
    private @JacksonXmlCData String description;

    public Template() {}

    public Template(String id) {
        super(id);
    }

    public Template(Template template) {
        this.setId(template.getId());
        this.setName(template.getName());
        this.header = template.getHeader();
        this.footer = template.getFooter();
        this.delimiter = template.getDelimiter();
        this.pattern = template.getPattern();
        if (template.getParams() != null) {
            Set<Param> params = new TreeSet<>();
            for (Param param : template.getParams()) {
                params.add(new Param(param));
            }
            this.params = params;
        }
        this.description = template.getDescription();
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
        this.delimiter = delimiter;
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
        if (this.params == null) this.params = new TreeSet<>();
        this.params.add(param);
    }

    public void removeParam(Param param) {
        if (this.params != null) this.params.remove(param);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
                "} " + super.toString();
    }

    @Override
    public Template deepCopy() {
        return new Template(this);
    }
}

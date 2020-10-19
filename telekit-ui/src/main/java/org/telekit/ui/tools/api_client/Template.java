package org.telekit.ui.tools.api_client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import org.jetbrains.annotations.NotNull;
import org.telekit.base.domain.Entity;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class Template extends Entity<Template, UUID> {

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_SOAP = "application/soap+xml";
    public static final String CONTENT_TYPE_OTHER = "other";

    private String name;
    private String uri;
    private HTTPMethod method = HTTPMethod.POST;
    private String contentType = CONTENT_TYPE_JSON;
    private @JacksonXmlCData String headers;
    private @JacksonXmlCData String body;
    private Integer batchSize = 0;
    private String batchWrapper;
    private Integer waitTimeout = HttpClient.CONNECT_TIMEOUT / 1000;
    private Set<Param> params;
    private @JacksonXmlCData String description;

    public Template() {}

    public Template(Template template) {
        this.setId(template.getId());
        this.setName(template.getName());
        this.uri = template.getUri();
        this.method = template.getMethod();
        this.contentType = template.getContentType();
        this.headers = template.getHeaders();
        this.body = template.getBody();
        this.batchSize = template.getBatchSize();
        this.batchWrapper = template.getBatchWrapper();
        if (template.getParams() != null) {
            Set<Param> params = new TreeSet<>();
            for (Param param : template.getParams()) {
                params.add(new Param(param));
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
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Template.HTTPMethod getMethod() {
        return method;
    }

    public void setMethod(Template.HTTPMethod method) {
        this.method = method;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public String getBatchWrapper() {
        return batchWrapper;
    }

    public void setBatchWrapper(String batchWrapper) {
        this.batchWrapper = batchWrapper;
    }

    public Integer getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(Integer waitTimeout) {
        this.waitTimeout = waitTimeout;
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
    public int compareTo(@NotNull Template that) {
        if (this == that) return 0;
        return String.valueOf(name).compareTo(String.valueOf(that.getName()));
    }

    @Override
    public String toString() {
        return "Template{" +
                "uri='" + uri + '\'' +
                ", method=" + method +
                ", contentType='" + contentType + '\'' +
                ", headers='" + headers + '\'' +
                ", body='" + body + '\'' +
                ", batchSize=" + batchSize +
                ", batchWrapper='" + batchWrapper + '\'' +
                ", waitTimeout=" + waitTimeout +
                ", params=" + params +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }

    @Override
    public Template deepCopy() {
        return new Template(this);
    }

    public enum HTTPMethod {
        DELETE, GET, HEAD, PATCH, POST, PUT
    }
}

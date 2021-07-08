package org.telekit.desktop.tools.apiclient;

import org.telekit.base.util.TextBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.telekit.base.net.ApacheHttpClient.Request;
import static org.telekit.base.net.ApacheHttpClient.Response;

public class CompletedRequest {

    private final Integer index;
    private final Integer processedLines;
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final Request request;
    private final Response response;
    private final String userData;

    public CompletedRequest(Integer index,
                            Integer processedLines,
                            Request request,
                            Response response,
                            String userData
    ) {
        this.index = index;
        this.processedLines = processedLines;
        this.request = request;
        this.response = response;
        this.userData = userData;
    }

    public Integer getIndex() {
        return index;
    }

    public Integer getProcessedLines() {
        return processedLines;
    }

    public String getProcessedRange() {
        return processedLines == 1 ? String.valueOf(index + 1) : (index + 1) + " - " + (index + processedLines);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getUserData() {
        return userData;
    }

    public Integer getStatusCode() {
        return response.statusCode();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isResponded() {
        return response.statusCode() >= 200;
    }

    public boolean isSucceeded() {
        return response.isSucceeded();
    }

    public boolean isForwarded() {
        return response.isForwarded();
    }

    public boolean isFailed() {
        return response.isFailed();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        CompletedRequest that = (CompletedRequest) o;
        return index.equals(that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    public String print() {
        TextBuilder text = new TextBuilder();
        text.appendLine(request.method() + ": " + request.uri());
        text.appendLine("Headers: " + request.headers());
        text.appendLine(request.body());
        text.newLine();
        text.appendLine("--------------------------------------");
        text.newLine();
        text.appendLine("Status: \"" + response.statusCode() + "\", " + response.reasonPhrase());
        text.appendLine("Headers: " + response.headers());
        text.appendLine(response.body());
        return text.toString();
    }
}

package org.telekit.ui.tools.api_client;

import org.telekit.ui.tools.api_client.HttpClient.Request;
import org.telekit.ui.tools.api_client.HttpClient.Response;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class CompletedRequest {

    private final Integer index;
    private final Integer processedLines;
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final String userData;

    private final URI requestUri;
    private final String requestMethod;
    private final Map<String, String> requestHeaders;
    private final String requestBody;

    private final int responseStatus;
    private final String responseReasonPhrase;
    private final Map<String, String> responseHeaders;
    private final String responseBody;

    public CompletedRequest(Integer index, Integer processedLines, Request request, Response response,
                            String userData) {
        this.index = index;
        this.processedLines = processedLines;

        this.requestUri = request.getUri();
        this.requestMethod = request.getMethod();
        this.requestHeaders = request.getHeaders() != null ? request.getHeaders() : Collections.emptyMap();
        this.requestBody = request.getBody();

        this.responseStatus = response.getStatus();
        this.responseReasonPhrase = response.getReasonPhrase();
        this.responseHeaders = response.getHeaders() != null ? response.getHeaders() : Collections.emptyMap();
        this.responseBody = response.getBody();

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

    public Integer getStatus() {
        return responseStatus;
    }

    public boolean isResponded() {
        return responseStatus >= 200;
    }

    public boolean isSucceeded() {
        return responseStatus >= 200 & responseStatus < 300;
    }

    public boolean isForwarded() {
        return responseStatus >= 300 & responseStatus < 400;
    }

    public boolean isFailed() {
        return responseStatus >= 400;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompletedRequest that = (CompletedRequest) o;
        return index.equals(that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static final String PRINT_SEPARATOR = "--------------------------------------\n";

    public String print() {
        return requestMethod + ": " + requestUri.toString() + "\n" +
                "Headers: " + requestHeaders.toString() + "\n" +
                requestBody + "\n" +
                "\n" + PRINT_SEPARATOR + "\n" +
                "Status: \"" + responseStatus + "\", " + responseReasonPhrase + "\n" +
                "Headers: " + responseHeaders.toString() + "\n" +
                responseBody + "\n";
    }
}

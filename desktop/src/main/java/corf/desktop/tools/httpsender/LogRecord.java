package corf.desktop.tools.httpsender;

import corf.base.net.HttpClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public final class LogRecord {

    private final LocalDateTime timestamp = LocalDateTime.now(ZoneId.systemDefault());
    private final int index;
    private final int batchSize;
    private final HttpClient.Request httpRequest;
    private final HttpClient.Response httpResponse;
    private final long duration;

    public LogRecord(int index,
                     int batchSize,
                     HttpClient.Request httpRequest,
                     HttpClient.Response httpResponse,
                     long duration) {
        this.index = index;
        this.batchSize = batchSize;
        this.httpRequest = Objects.requireNonNull(httpRequest, "httpRequest");
        this.httpResponse = Objects.requireNonNull(httpResponse, "httpResponse");
        this.duration = duration;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public HttpClient.Request getHttpRequest() {
        return httpRequest;
    }

    public HttpClient.Response getHttpResponse() {
        return httpResponse;
    }

    public long getDuration() {
        return duration;
    }

    public String getUri() {
        return String.valueOf(httpRequest.uri());
    }

    public Integer getStatusCode() {
        return httpResponse.statusCode();
    }

    public String getReasonPhrase() {
        return httpResponse.reasonPhrase();
    }

    public boolean responded() {
        return getStatusCode() >= 200;
    }

    public boolean succeeded() {
        return httpResponse.isSucceeded();
    }

    public boolean forwarded() {
        return httpResponse.isForwarded();
    }

    public boolean failed() {
        return httpResponse.isFailed();
    }

    public String getProcessedRange() {
        return batchSize <= 1
                ? String.valueOf(index + 1)
                : (index + 1) + " - " + (index + batchSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogRecord that = (LogRecord) o;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        return index;
    }
}

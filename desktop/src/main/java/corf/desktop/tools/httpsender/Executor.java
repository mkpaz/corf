package corf.desktop.tools.httpsender;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.text.LineSeparator;
import corf.base.security.UsernamePasswordCredentials;
import corf.base.net.ApacheHttpClient;
import corf.base.net.HttpConstants.AuthScheme;
import corf.base.net.HttpConstants.Headers;
import corf.base.preferences.Proxy;
import corf.base.text.PlaceholderReplacer;
import corf.base.text.CSV;
import corf.desktop.tools.common.ReplacementCheckResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

final class Executor extends Task<ObservableList<LogRecord>> {

    static final String HEADER_KEY_VALUE_SEPARATOR = ":";

    private final Template template;
    private final CSV csv;
    private final ObservableList<LogRecord> log;
    private final Options options;
    private final ApacheHttpClient.Builder httpBuilder;
    private final ExecutorQueue queue;

    public Executor(Template template, CSV csv, ObservableList<LogRecord> log, Options options) {
        this.template = Objects.requireNonNull(template, "template");
        this.csv = Objects.requireNonNull(csv, "csv");
        this.log = Objects.requireNonNull(log, "log");
        this.options = Objects.requireNonNull(options, "options");

        var httpHeaders = new HashMap<>(parseHttpHeaders(template.getHeaders()));

        this.httpBuilder = ApacheHttpClient.builder()
                .timeouts((int) TimeUnit.SECONDS.toMillis(template.getWaitTimeout()))
                .ignoreCookies()
                .trustAllCertificates();

        // add auth headers (if applicable), so they will be present in every request
        configureAuth(httpHeaders);

        // configure http proxy, if present
        if (options.proxy() != null) {
            httpBuilder.proxy(options.proxy());
        }

        queue = new ExecutorQueue(template, csv, httpHeaders);
    }

    @Override
    protected ObservableList<LogRecord> call() {
        var httpClient = httpBuilder.build();

        while (queue.hasNext()) {
            // stop if task has been canceled
            if (isCancelled()) { break; }

            var index = queue.getIndex();
            var startTime = System.currentTimeMillis();

            var request = queue.next();
            var response = httpClient.execute(request);

            var endTime = System.currentTimeMillis();
            var processedRows = queue.getIndex() - index;
            var logRecord = new LogRecord(index, processedRows, request, response, endTime - startTime);

            Platform.runLater(() -> log.add(logRecord));
            updateProgress(queue.getIndex(), csv.length());

            // timeout before sending next request
            if (queue.hasNext()) {
                sleepSilently(options.pollTimeout());
            }
        }

        return log;
    }

    private void configureAuth(Map<String, String> httpHeaders) {
        if (options.authScheme() == null || options.credentials() == null) { return; }

        // Since it's allowed to specify HTTP headers manually as string, user can add auth headers as well.
        // However, auth dialog has higher priority. If the latter, we must remove user headers here,
        // because Apache HTTP client doesn't support overriding for such cases.
        httpHeaders.entrySet().removeIf(e -> Headers.AUTHORIZATION.equalsIgnoreCase(e.getKey()));

        // placeholders use % sign that makes whole URL invalid
        String safeString = PlaceholderReplacer.removePlaceholders(template.getUri());
        if (options.authScheme() == AuthScheme.BASIC) {
            httpBuilder.basicAuth(
                    options.credentials().toPasswordAuthentication(),
                    removeUriPath(URI.create(safeString)),
                    true
            );
        }
    }

    private void sleepSilently(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private URI removeUriPath(URI uri) {
        try {
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("StringSplitter")
    private Map<String, String> parseHttpHeaders(@Nullable String text) {
        var headers = new LinkedHashMap<String, String>();
        if (StringUtils.isBlank(text)) { return headers; }

        for (var line : text.split(LineSeparator.LINE_SPLIT_PATTERN)) {
            if (StringUtils.isBlank(line)) { continue; }

            String[] kv = line.split(HEADER_KEY_VALUE_SEPARATOR);
            if (kv.length == 2) {
                headers.put(StringUtils.trim(kv[0]), StringUtils.trim(kv[1]));
            }
        }

        return headers;
    }

    public static ReplacementCheckResult validate(Template template, CSV csv) {
        return ExecutorQueue.validate(template, csv);
    }

    public int getPlannedRequestCount() {
        return queue.size();
    }

    ///////////////////////////////////////////////////////////////////////////

    record Options(Duration pollTimeout,
                   @Nullable Proxy proxy,
                   @Nullable AuthScheme authScheme,
                   @Nullable UsernamePasswordCredentials credentials) {

        Options(Duration pollTimeout,
                @Nullable Proxy proxy,
                @Nullable AuthScheme authScheme,
                @Nullable UsernamePasswordCredentials credentials) {

            this.pollTimeout = Objects.requireNonNullElse(pollTimeout, Duration.ofMillis(200));
            this.proxy = proxy;
            this.authScheme = authScheme;
            this.credentials = credentials;
        }

        public static Options simple(int pollTimeout, @Nullable Proxy proxy) {
            return new Options(Duration.ofMillis(pollTimeout), proxy, null, null);
        }

        public static Options forBasicAuth(int pollTimeout,
                                           @Nullable Proxy proxy,
                                           String username,
                                           String password) {
            return new Options(
                    Duration.ofMillis(pollTimeout),
                    proxy,
                    AuthScheme.BASIC,
                    UsernamePasswordCredentials.of(
                            StringUtils.trim(StringUtils.defaultString(username)),
                            StringUtils.trim(StringUtils.defaultString(password))
                    )
            );
        }
    }
}

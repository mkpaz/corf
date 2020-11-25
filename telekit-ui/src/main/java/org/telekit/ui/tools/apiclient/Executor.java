package org.telekit.ui.tools.apiclient;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.telekit.base.domain.AuthPrincipal;
import org.telekit.base.domain.HttpConstants.AuthType;
import org.telekit.base.domain.HttpConstants.ContentType;
import org.telekit.base.domain.KeyValue;
import org.telekit.base.domain.LineSeparator;
import org.telekit.base.i18n.Messages;
import org.telekit.base.net.SimpleHttpClient;
import org.telekit.base.net.SimpleHttpClient.Request;
import org.telekit.base.net.SimpleHttpClient.Response;
import org.telekit.base.preferences.Proxy;
import org.telekit.base.util.ConcurrencyUtils;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.ui.tools.common.Param;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.util.CollectionUtils.ensureNotNull;
import static org.telekit.base.util.NumberUtils.ensureRange;
import static org.telekit.base.util.PlaceholderReplacer.containsPlaceholders;
import static org.telekit.base.util.PlaceholderReplacer.format;
import static org.telekit.ui.MessageKeys.*;
import static org.telekit.ui.tools.common.ReplacementUtils.*;

public class Executor extends Task<ObservableList<CompletedRequest>> {

    public static final int MAX_CSV_SIZE = 100000;

    private final Template template;
    private final String[][] csv;
    private final SimpleHttpClient httpClient;

    private final ReadOnlyObjectWrapper<ObservableList<CompletedRequest>> partialResults =
            new ReadOnlyObjectWrapper<>(this, "partialResults", FXCollections.observableArrayList());

    public final ObservableList<CompletedRequest> getPartialResults() {
        return partialResults.get();
    }

    public final ReadOnlyObjectProperty<ObservableList<CompletedRequest>> partialResultsProperty() {
        return partialResults.getReadOnlyProperty();
    }

    private AuthType authType;
    private AuthPrincipal authPrincipal;
    private Proxy proxy;
    private int timeoutBetweenRequests = 200;

    public Executor(Template template, String[][] csv) {
        Objects.requireNonNull(template);
        Objects.requireNonNull(csv);

        this.template = new Template(template);
        this.csv = csv;
        this.httpClient = new SimpleHttpClient(
                (int) TimeUnit.SECONDS.toMillis(template.getWaitTimeout()),
                (int) TimeUnit.SECONDS.toMillis(template.getWaitTimeout())
        );
    }

    public void setTimeoutBetweenRequests(int timeoutBetweenRequests) {
        this.timeoutBetweenRequests = timeoutBetweenRequests;
    }

    public void setAuthData(AuthType authType, AuthPrincipal authPrincipal) {
        this.authType = authType;
        this.authPrincipal = authPrincipal;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    protected ObservableList<CompletedRequest> call() {
        Map<String, String> replacements = new HashMap<>();
        Set<Param> params = ensureNotNull(template.getParams());
        Map<String, String> headers = new HashMap<>();

        // set default content-type header
        KeyValue<String, String> contentType = contentTypeHeader(template.getContentType());
        headers.put(contentType.getKey(), contentType.getValue());
        // allow default headers to be overridden with user specified ones
        headers.putAll(parseHeaders(template.getHeaders()));

        // configure proxy
        if (proxy != null) {
            httpClient.setProxy(proxy.getUrl(), proxy.getPrincipal());
        }

        // configure auth
        if (authType == AuthType.BASIC) {
            httpClient.setBasicAuth(
                    authPrincipal.getUsername(),
                    authPrincipal.getPassword(),
                    // preemptive auth requires domain part of URL to be specified
                    // TODO: implement via java.net.URL wrapper
                    template.getUri().replaceAll(PlaceholderReplacer.PLACEHOLDER_PATTERN, "")
            );
        }

        // batch size is limited by rows count
        int batchSize = ensureRange(template.getBatchSize(), 1, csv.length);
        // sequential index is always incremented by 1, it doesn't depend on loop step value
        int sequentialIndex = 0;

        for (int idx = 0; idx < csv.length & idx < MAX_CSV_SIZE; idx += batchSize) {
            // stop if task has been canceled
            if (isCancelled()) break;

            // template can contain generated params, that have to be updated on each iteration
            putTemplatePlaceholders(replacements, params);

            String uri, body, userData;
            int processedLines;

            if (batchSize == 1) {
                /* requests payload not merged (batch mode = off) */
                String[] row = csv[idx];

                // update replacement params
                putCsvPlaceholders(replacements, row);
                putIndexPlaceholders(replacements, sequentialIndex);

                // uri, headers and body can contain any placeholders
                uri = format(template.getUri(), replacements);
                body = format(template.getBody(), replacements);
                replaceHeadersPlaceholders(headers, replacements);

                userData = Arrays.toString(row) + " | " + uri;
                processedLines = 1;
                sequentialIndex++;
            } else {
                /* requests payload merged (batch mode = on) */
                int endIdx = Math.min(idx + batchSize, csv.length);
                String[][] batchCsv = Arrays.copyOfRange(csv, idx, endIdx);
                String[] batchBody = new String[batchCsv.length];

                // uri and headers can't contain CSV or index placeholders,
                // because they have to be identical for multiple CSV rows
                uri = format(template.getUri(), replacements);
                replaceHeadersPlaceholders(headers, replacements);

                // only payload can contain CSV or index placeholders
                for (int batchIndex = 0; batchIndex < batchCsv.length; batchIndex++) {
                    String[] row = batchCsv[batchIndex];

                    putCsvPlaceholders(replacements, row);
                    putIndexPlaceholders(replacements, sequentialIndex);

                    batchBody[batchIndex] = format(template.getBody(), replacements);
                    sequentialIndex++;
                }

                body = mergeBatchItems(batchBody, template.getBatchWrapper(), template.getContentType());
                userData = uri;
                processedLines = batchCsv.length;
            }

            // perform request
            final Request request = new Request(template.getMethod().name(), uri, headers, body);
            final Response response = httpClient.execute(request);
            final CompletedRequest result = new CompletedRequest(idx, processedLines, request, response, userData);

            // update progress property
            Platform.runLater(() -> getPartialResults().add(result));
            updateProgress(idx, csv.length);

            // timeout before sending next request
            if (idx < csv.length - 1) {
                ConcurrencyUtils.sleep(timeoutBetweenRequests);
            }
        }

        return partialResults.get();
    }

    public static List<String> validate(Template template, String[][] csv) {
        Set<Param> params = ensureNotNull(template.getParams());
        Map<String, String> replacements = new HashMap<>();
        List<String> warnings = new ArrayList<>();

        // verify max size
        if (csv.length > MAX_CSV_SIZE) {
            warnings.add(Messages.get(TOOLS_MSG_VALIDATION_CSV_THRESHOLD_EXCEEDED, MAX_CSV_SIZE));
        }

        // verify that all non-autogenerated params values has been specified
        boolean hasBlankValues = putTemplatePlaceholders(replacements, params);
        if (hasBlankValues) warnings.add(Messages.get(TOOLS_MSG_VALIDATION_BLANK_PARAM_VALUES));

        int firstRowSize = 0, maxRowSize = 0;
        String urlAndBodyFormatted = "";
        for (int rowIndex = 0; rowIndex < csv.length & rowIndex < MAX_CSV_SIZE; rowIndex++) {
            String[] row = csv[rowIndex];
            if (rowIndex == 0) {
                firstRowSize = maxRowSize = row.length;
                // unresolved placeholders validation can be performed for the first line only
                putIndexPlaceholders(replacements, rowIndex);
                putCsvPlaceholders(replacements, row);
                urlAndBodyFormatted = format(template.getUri() + template.getBody(), replacements);
            } else {
                maxRowSize = row.length;
            }
        }

        // verify that all csv table rows has the same columns count
        if (firstRowSize != maxRowSize) warnings.add(Messages.get(TOOLS_MSG_VALIDATION_MIXED_CSV));

        // verify that all placeholders has been replaced
        if (containsPlaceholders(urlAndBodyFormatted)) {
            warnings.add(Messages.get(TOOLS_MSG_VALIDATION_UNRESOLVED_PLACEHOLDERS));
        }

        return warnings;
    }

    private static Map<String, String> parseHeaders(String text) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (isBlank(text)) return headers;

        for (String line : text.split(LineSeparator.LINE_SPLIT_PATTERN)) {
            if (isBlank(line)) continue;
            String[] kv = line.split(":");
            if (kv.length == 2) headers.put(trim(kv[0]), trim(kv[1]));
        }
        return headers;
    }

    private static void replaceHeadersPlaceholders(Map<String, String> headers, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            entry.setValue(format(entry.getValue(), replacements));
        }
    }

    private static KeyValue<String, String> contentTypeHeader(ContentType contentType) {
        return contentType != null ?
                new KeyValue<>(SimpleHttpClient.CONTENT_TYPE_HEADER, contentType.getMimeType()) :
                new KeyValue<>(SimpleHttpClient.CONTENT_TYPE_HEADER, ContentType.TEXT_PLAIN.getMimeType());
    }

    private static String mergeBatchItems(String[] items, String batchWrapper, ContentType contentType) {
        String separator = contentType == ContentType.APPLICATION_JSON ? "," : "\n";
        return format(batchWrapper, Map.of("batch", String.join(separator, items)));
    }
}
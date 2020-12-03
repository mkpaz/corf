package org.telekit.ui.tools.apiclient;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.telekit.base.domain.LineSeparator;
import org.telekit.base.domain.Proxy;
import org.telekit.base.domain.UsernamePasswordCredential;
import org.telekit.base.i18n.Messages;
import org.telekit.base.net.ApacheHttpClient;
import org.telekit.base.net.HttpClient;
import org.telekit.base.net.HttpClient.Request;
import org.telekit.base.net.HttpClient.Response;
import org.telekit.base.net.HttpConstants;
import org.telekit.base.net.HttpConstants.AuthScheme;
import org.telekit.base.net.UriUtils;
import org.telekit.base.util.CollectionUtils;
import org.telekit.base.util.ConcurrencyUtils;
import org.telekit.base.util.PlaceholderReplacer;
import org.telekit.ui.tools.apiclient.Template.BatchSeparator;
import org.telekit.ui.tools.common.Param;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.util.NumberUtils.ensureRange;
import static org.telekit.base.util.PlaceholderReplacer.containsPlaceholders;
import static org.telekit.base.util.PlaceholderReplacer.format;
import static org.telekit.base.util.StringUtils.ensureNotNull;
import static org.telekit.ui.MessageKeys.*;
import static org.telekit.ui.tools.common.ReplacementUtils.*;

public class Executor extends Task<ObservableList<CompletedRequest>> {

    public static final String BATCH_PLACEHOLDER_NAME = "_batch";
    public static final String HEADER_KV_SEPARATOR = ":";

    public static final int MAX_CSV_SIZE = 100000;

    private final Template template;
    private final String[][] csv;
    private final ApacheHttpClient.Builder httpClientBuilder;

    private final ReadOnlyObjectWrapper<ObservableList<CompletedRequest>> partialResults =
            new ReadOnlyObjectWrapper<>(this, "partialResults", FXCollections.observableArrayList());

    public final ObservableList<CompletedRequest> getPartialResults() {
        return partialResults.get();
    }

    public final ReadOnlyObjectProperty<ObservableList<CompletedRequest>> partialResultsProperty() {
        return partialResults.getReadOnlyProperty();
    }

    private AuthScheme authScheme;
    private UsernamePasswordCredential credential;
    private Proxy proxy;
    private int timeoutBetweenRequests = 200; // millis

    public Executor(Template template, String[][] csv) {
        Objects.requireNonNull(template);
        Objects.requireNonNull(csv);

        this.template = new Template(template);
        this.csv = csv;
        this.httpClientBuilder = ApacheHttpClient.builder()
                .timeouts((int) TimeUnit.SECONDS.toMillis(template.getWaitTimeout()))
                .ignoreCookies()
                .trustAllCertificates();
    }

    public void setTimeoutBetweenRequests(int timeoutBetweenRequests) {
        this.timeoutBetweenRequests = timeoutBetweenRequests;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public void setPasswordBasedAuth(AuthScheme authScheme, UsernamePasswordCredential credential) {
        this.authScheme = authScheme;
        this.credential = credential;
    }

    @Override
    protected ObservableList<CompletedRequest> call() {
        Map<String, String> replacements = new HashMap<>();
        Set<Param> params = CollectionUtils.ensureNotNull(template.getParams());
        Map<String, String> origHeaders = new HashMap<>(parseHeaders(template.getHeaders()));

        configureAuth(origHeaders);
        configureProxy();

        HttpClient httpClient = httpClientBuilder.build();

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
            SortedMap<String, String> headers = new TreeMap<>(origHeaders);
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

                // batch wrapper is allowed to contain params placeholders (e.g. API key)
                String batchWrapper = format(template.getBatchWrapper(), replacements);

                // only payload can contain CSV or index placeholders
                Map<String, String> batchReplacements = new HashMap<>(replacements);
                for (int batchIndex = 0; batchIndex < batchCsv.length; batchIndex++) {
                    String[] row = batchCsv[batchIndex];

                    putCsvPlaceholders(batchReplacements, row);
                    putIndexPlaceholders(batchReplacements, sequentialIndex);

                    batchBody[batchIndex] = format(template.getBody(), batchReplacements);
                    sequentialIndex++;
                }

                body = mergeBatchItems(batchBody, batchWrapper, template.getBatchSeparator());
                userData = uri;
                processedLines = batchCsv.length;
            }

            // perform request
            final Request request = new Request(template.getMethod(), URI.create(uri), headers, body);
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

    private void configureProxy() {
        if (proxy == null || !proxy.isValid()) return;
        httpClientBuilder.proxy(proxy.getUri(), proxy.passwordAuthentication());
    }

    private void configureAuth(Map<String, String> userHeaders) {
        if (authScheme == null || credential == null) return;

        // we have to remove authorization header manually, because Apache HTTP won't override it
        userHeaders.entrySet().removeIf(e -> HttpConstants.Headers.AUTHORIZATION.equalsIgnoreCase(e.getKey()));

        // placeholders use % sign that makes whole URL invalid
        String safeUri = PlaceholderReplacer.removePlaceholders(template.getUri());
        if (authScheme == AuthScheme.BASIC) {
            httpClientBuilder.basicAuth(
                    credential.toPasswordAuthentication(),
                    UriUtils.withoutPath(URI.create(safeUri)),
                    true
            );
        }
    }

    public static List<String> validate(Template template, String[][] csv) {
        Set<Param> params = CollectionUtils.ensureNotNull(template.getParams());
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
        String payloadFormatted = "";
        for (int rowIndex = 0; rowIndex < csv.length & rowIndex < MAX_CSV_SIZE; rowIndex++) {
            String[] row = csv[rowIndex];
            if (rowIndex == 0) {
                firstRowSize = maxRowSize = row.length;

                // unresolved placeholders validation can be performed for the first line only
                if (template.getBatchSize() == 0) {
                    putIndexPlaceholders(replacements, rowIndex);
                    putCsvPlaceholders(replacements, row);

                    payloadFormatted = format(
                            template.getUri() +
                                    ensureNotNull(template.getBody()) +
                                    ensureNotNull(template.getHeaders())
                            , replacements);
                } else {
                    Map<String, String> batchReplacements = new HashMap<>(replacements);
                    putIndexPlaceholders(batchReplacements, rowIndex);
                    putCsvPlaceholders(batchReplacements, row);

                    payloadFormatted = format(template.getUri() + ensureNotNull(template.getHeaders()), replacements) +
                            format(ensureNotNull(template.getBody()), batchReplacements);
                }
            } else {
                maxRowSize = row.length;
            }
        }

        // verify that all csv table rows has the same columns count
        if (firstRowSize != maxRowSize) warnings.add(Messages.get(TOOLS_MSG_VALIDATION_MIXED_CSV));

        // verify that all placeholders has been replaced
        if (containsPlaceholders(payloadFormatted)) {
            warnings.add(Messages.get(TOOLS_MSG_VALIDATION_UNRESOLVED_PLACEHOLDERS));
        }

        return warnings;
    }

    private static Map<String, String> parseHeaders(String text) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (isBlank(text)) return headers;

        for (String line : text.split(LineSeparator.LINE_SPLIT_PATTERN)) {
            if (isBlank(line)) continue;
            String[] kv = line.split(HEADER_KV_SEPARATOR);
            if (kv.length == 2) headers.put(trim(kv[0]), trim(kv[1]));
        }
        return headers;
    }

    private static void replaceHeadersPlaceholders(Map<String, String> headers, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            entry.setValue(format(entry.getValue(), replacements));
        }
    }

    private static String mergeBatchItems(String[] items, String wrapper, BatchSeparator separator) {
        return format(wrapper, Map.of(BATCH_PLACEHOLDER_NAME, String.join(separator.getValue(), items)));
    }
}
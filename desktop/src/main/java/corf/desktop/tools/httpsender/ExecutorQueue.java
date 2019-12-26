package corf.desktop.tools.httpsender;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.net.HttpClient;
import corf.base.common.NumberUtils;
import corf.base.text.PlaceholderReplacer;
import corf.base.text.CSV;
import corf.desktop.tools.common.Param;
import corf.desktop.tools.common.ReplacementCheckResult;
import corf.desktop.tools.common.TemplateWorker;
import corf.desktop.tools.httpsender.Template.Batch;

import java.net.URI;
import java.util.*;

final class ExecutorQueue implements Iterator<HttpClient.Request>, TemplateWorker {

    static final int MAX_CSV_SIZE = 100_000;

    private final Template template;
    private final CSV csv;
    private final Map<String, String> baseHttpHeaders;

    private final int csvSize;
    private final int batchSize;
    private final Map<String, String> replacements;
    private final Set<Param> params;

    // index is always incremented by 1, it doesn't depend on batch size
    private int index = 0;
    private int processedCount = 0;

    public ExecutorQueue(Template template, CSV csv, Map<String, String> httpHeaders) {
        this.template = Objects.requireNonNull(template, "template");
        this.csv = Objects.requireNonNull(csv, "csv");
        this.baseHttpHeaders = Objects.requireNonNullElse(httpHeaders, Collections.emptyMap());

        csvSize = Math.min(csv.length(), MAX_CSV_SIZE);

        // batch size is limited by row count
        batchSize = NumberUtils.ensureRange(template.getBatch().getSize(), 1, csv.length());

        replacements = new HashMap<>();
        params = template.getParams();
    }

    @Override
    public boolean hasNext() {
        return processedCount < csvSize;
    }

    @Override
    public HttpClient.Request next() {
        String uri, body;
        var httpHeaders = new TreeMap<>(baseHttpHeaders);
        int processedRows;

        if (!template.isBatchMode()) {
            String[] row = csv.get(index);

            TemplateWorker.putParamReplacements(replacements, params);
            TemplateWorker.putCsvReplacements(replacements, row);
            TemplateWorker.putIndexReplacements(replacements, index);

            // URI, HTTP headers and body are allowed to contain placeholders
            uri = PlaceholderReplacer.replace(template.getUri(), replacements);
            body = PlaceholderReplacer.replace(template.getBody(), replacements);
            replaceHttpHeaders(httpHeaders, replacements);

            processedRows = 1;
            index++;
        } else {
            int batchEndIndex = Math.min(index + batchSize, csv.length());
            String[][] batchCsvRange = csv.getDataRange(index, batchEndIndex);
            String[] batchBodyArray = new String[batchCsvRange.length];

            // In batch mode URI and HTTP headers are not allowed to contain CSV
            // or index placeholders, because they would be identical for multiple
            // CSV rows. So, we ONLY REPLACE NAMED PARAMS here.
            TemplateWorker.putParamReplacements(replacements, params);
            uri = PlaceholderReplacer.replace(template.getUri(), replacements);
            replaceHttpHeaders(httpHeaders, replacements);

            // only payload can contain CSV or index placeholders
            var batchReplacements = new HashMap<>(replacements);
            for (int batchIndex = 0; batchIndex < batchCsvRange.length; batchIndex++) {
                String[] row = batchCsvRange[batchIndex];

                // auto-generated named params MUST be updated at EVERY iteration,
                // skipping the first iteration, because we already put param
                // replacements once (for URI/headers)
                if (batchIndex > 0) {
                    TemplateWorker.putParamReplacements(batchReplacements, params);
                }

                TemplateWorker.putCsvReplacements(batchReplacements, row);
                TemplateWorker.putIndexReplacements(batchReplacements, index);

                batchBodyArray[batchIndex] = PlaceholderReplacer.replace(template.getBody(), batchReplacements);

                index++;
            }

            Batch batch = template.getBatch();

            // batch wrapper is allowed to contain params placeholders (e.g. for API key)
            String batchStart = PlaceholderReplacer.replace(batch.getStart(), replacements);
            String batchEnd = PlaceholderReplacer.replace(batch.getEnd(), replacements);

            body = batchStart
                    + String.join(StringUtils.defaultString(batch.getSeparator()), batchBodyArray)
                    + batchEnd;
            processedRows = batchCsvRange.length;
        }

        processedCount += processedRows;

        // simulate unsuccessful requests
        // if (List.create(1, 3).contains(index)) { body = "(*&^%^%$%"; }

        return new HttpClient.Request(template.getMethod(), URI.create(uri), httpHeaders, body);
    }

    public int getIndex() {
        return index;
    }

    public int size() {
        int len = csv.length();
        return batchSize <= 1 ? len : len / batchSize + ((len % batchSize > 0) ? 1 : 0);
    }

    private void replaceHttpHeaders(Map<String, String> headers,
                                    Map<String, String> replacements) {
        for (var entry : headers.entrySet()) {
            entry.setValue(PlaceholderReplacer.replace(entry.getValue(), replacements));
        }
    }

    public static ReplacementCheckResult validate(Template template, CSV csv) {
        var replacements = new HashMap<String, String>();
        var params = SetUtils.emptyIfNull(template.getParams());
        var check = new ReplacementCheckResult.Builder();

        // verify max size
        if (csv.length() > MAX_CSV_SIZE) {
            check.setSizeThresholdExceeded(csv.length(), MAX_CSV_SIZE);
        }

        // put param replacements and verify all param values specified
        boolean blankParamValuesDetected = TemplateWorker.putParamReplacements(replacements, params);
        check.setContainsBlankValues(blankParamValuesDetected);

        int firstRowCellCount = 0, maxCellCount = 0;
        int csvSize = Math.min(csv.length(), MAX_CSV_SIZE);

        for (int rowNum = 0; rowNum < csvSize; rowNum++) {
            String[] row = csv.get(rowNum);

            // unresolved placeholder validation can be performed for the first line only
            if (rowNum == 0) {
                firstRowCellCount = maxCellCount = row.length;

                if (template.isBatchMode()) {
                    TemplateWorker.putIndexReplacements(replacements, rowNum);
                    TemplateWorker.putCsvReplacements(replacements, row);

                    validateLine(template.getUri(), replacements, check);
                    validateLine(template.getHeaders(), replacements, check);
                    validateLine(template.getBody(), replacements, check);
                } else {
                    var batchReplacements = new HashMap<>(replacements);
                    TemplateWorker.putIndexReplacements(batchReplacements, rowNum);
                    TemplateWorker.putCsvReplacements(batchReplacements, row);

                    validateLine(template.getUri(), replacements, check);
                    validateLine(template.getHeaders(), replacements, check);
                    validateLine(template.getBody(), batchReplacements, check);
                }
            } else {
                maxCellCount = Math.max(maxCellCount, row.length);
            }
        }

        // verify all csv rows have the same cell count
        check.setHasVariableRowLength(firstRowCellCount != maxCellCount);

        return check.build();
    }

    private static void validateLine(@Nullable String line,
                                     Map<String, String> replacements,
                                     ReplacementCheckResult.Builder check) {
        var formattedLine = PlaceholderReplacer.replace(StringUtils.defaultString(line), replacements);
        if (PlaceholderReplacer.containsPlaceholders(formattedLine)) {
            check.addInvalidLine(formattedLine);
        }
    }
}

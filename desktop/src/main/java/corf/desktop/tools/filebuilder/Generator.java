package corf.desktop.tools.filebuilder;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.exception.AppException;
import corf.base.text.CSV;
import corf.base.text.Encoding;
import corf.base.text.LineSeparator;
import corf.base.text.PlaceholderReplacer;
import corf.desktop.i18n.DM;
import corf.desktop.tools.common.ReplacementCheckResult;
import corf.desktop.tools.common.TemplateWorker;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

import static corf.base.i18n.I18n.t;

final class Generator implements TemplateWorker, Runnable {

    public static final int MAX_CSV_SIZE = 100_000;

    private final Template template;
    private final CSV csv;
    private final File outputFile;
    private final Options options;

    public Generator(Template template, CSV csv, File outputFile, Options options) {
        this.template = Objects.requireNonNull(template, "template");
        this.csv = Objects.requireNonNull(csv, "csv");
        this.outputFile = Objects.requireNonNull(outputFile, "file");
        this.options = Objects.requireNonNull(options, "options");
    }

    @Override
    public void run() {
        try (var outputStream = new FileOutputStream(outputFile, options.append());
             var streamWriter = new OutputStreamWriter(outputStream, options.charset());
             var out = new BufferedWriter(streamWriter)) {

            generate(out);
        } catch (Exception e) {
            throw new AppException(t(DM.MSG_GENERIC_IO_ERROR), e);
        }
    }

    // package private for unit tests
    void generate(Writer out) throws Exception {
        var replacements = new HashMap<String, String>();
        var params = template.getParams();

        // write BOM for new files if specified
        if (options.bom() && !options.append()) {
            out.write(Encoding.BOM);
        }

        String pattern = StringUtils.trim(template.getPattern());
        String footer = null;
        String delimiter = StringUtils.isNotEmpty(template.getDelimiter()) ? template.getDelimiter() : null;
        int csvSize = Math.min(csv.length(), MAX_CSV_SIZE);

        for (int rowNum = 0; rowNum < csvSize; rowNum++) {
            String[] row = csv.get(rowNum);
            boolean lastRow = rowNum == csvSize - 1;

            // auto-generated named params MUST be updated at EVERY iteration
            TemplateWorker.putParamReplacements(replacements, params);

            if (rowNum == 0) {
                // header (replace BEFORE putting index and csv placeholders)
                if (StringUtils.isNotBlank(template.getHeader())) {
                    String header = PlaceholderReplacer.replace(template.getHeader(), replacements);
                    write(out, header, null);
                }

                // footer (replace BEFORE putting index and csv placeholders)
                if (StringUtils.isNotBlank(template.getFooter())) {
                    footer = PlaceholderReplacer.replace(template.getFooter(), replacements);
                    // write after the last row ...
                }
            }

            TemplateWorker.putIndexReplacements(replacements, rowNum);
            TemplateWorker.putCsvReplacements(replacements, row);

            var formattedLine = PlaceholderReplacer.replace(pattern, replacements);
            write(out, formattedLine, delimiter != null && !lastRow ? delimiter : null);
        }

        if (StringUtils.isNotBlank(footer)) {
            write(out, footer, null);
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

        String headerAfterFormatting = StringUtils.isNotBlank(template.getHeader()) ?
                PlaceholderReplacer.replace(template.getHeader(), replacements) : null;

        String footerAfterFormatting = StringUtils.isNotBlank(template.getHeader()) ?
                PlaceholderReplacer.replace(template.getFooter(), replacements) : null;

        int firstRowCellCount = 0, maxCellCount = 0;
        String firstRowAfterFormatting = "";
        int csvSize = Math.min(csv.length(), MAX_CSV_SIZE);

        for (int rowNum = 0; rowNum < csvSize; rowNum++) {
            String[] row = csv.get(rowNum);

            // unresolved placeholder validation can be performed for the first line only
            if (rowNum == 0) {
                firstRowCellCount = maxCellCount = row.length;

                TemplateWorker.putIndexReplacements(replacements, rowNum);
                TemplateWorker.putCsvReplacements(replacements, row);

                firstRowAfterFormatting = PlaceholderReplacer.replace(template.getPattern(), replacements);
            } else {
                maxCellCount = Math.max(maxCellCount, row.length);
            }
        }

        // verify all csv rows have the same cell count
        check.setHasVariableRowLength(firstRowCellCount != maxCellCount);

        // verify all placeholders has been replaced
        if (headerAfterFormatting != null && PlaceholderReplacer.containsPlaceholders(headerAfterFormatting)) {
            check.addInvalidLine(headerAfterFormatting);
        }
        if (PlaceholderReplacer.containsPlaceholders(firstRowAfterFormatting)) {
            check.addInvalidLine(firstRowAfterFormatting);
        }
        if (footerAfterFormatting != null && PlaceholderReplacer.containsPlaceholders(footerAfterFormatting)) {
            check.addInvalidLine(footerAfterFormatting);
        }

        return check.build();
    }

    private void write(Writer writer, String s, @Nullable String delimiter) throws IOException {
        writer.write(ensureLineSeparator(StringUtils.trim(s), options.lineSeparator()));
        if (delimiter != null) { writer.write(delimiter); }
        writer.write(options.lineSeparator());
    }

    private String ensureLineSeparator(String text, String lineSeparator) {
        return String.join(lineSeparator, text.split(LineSeparator.LINE_SPLIT_PATTERN));
    }

    ///////////////////////////////////////////////////////////////////////////

    record Options(Charset charset, String lineSeparator, boolean bom, boolean append) {

        Options(Charset charset, String lineSeparator, boolean bom, boolean append) {
            this.charset = Objects.requireNonNullElse(charset, StandardCharsets.UTF_8);
            this.lineSeparator = Objects.requireNonNullElse(lineSeparator, System.lineSeparator());
            this.bom = bom;
            this.append = append;
        }
    }
}

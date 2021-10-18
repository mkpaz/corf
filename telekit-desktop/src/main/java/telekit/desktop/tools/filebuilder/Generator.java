package telekit.desktop.tools.filebuilder;

import telekit.base.domain.Encoding;
import telekit.base.domain.LineSeparator;
import telekit.base.domain.exception.TelekitException;
import telekit.base.i18n.I18n;
import telekit.desktop.tools.common.Param;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.apache.commons.collections4.SetUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static telekit.base.util.PlaceholderReplacer.containsPlaceholders;
import static telekit.base.util.PlaceholderReplacer.format;
import static telekit.desktop.i18n.DesktopMessages.*;
import static telekit.desktop.tools.common.ReplacementUtils.*;

public class Generator implements Runnable {

    public static final int MAX_CSV_SIZE = 100000;
    public static final int MODE_REPLACE = 0;
    public static final int MODE_APPEND = 1;

    private final Template template;
    private final String[][] csv;
    private final File outputFile;

    private String lineSeparator = System.lineSeparator();
    private Charset charset = StandardCharsets.UTF_8;
    private boolean bom = false;
    private int mode = MODE_REPLACE;

    public Generator(Template template, String[][] csv, File outputFile) {
        Objects.requireNonNull(template);
        Objects.requireNonNull(csv);
        Objects.requireNonNull(outputFile);

        this.template = new Template(template);
        this.csv = csv;
        this.outputFile = outputFile;
    }

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setCharset(Charset charset, boolean bom) {
        this.charset = charset;
        this.bom = bom;
    }

    @Override
    public void run() {
        Map<String, String> replacements = new HashMap<>();
        Set<Param> params = emptyIfNull(template.getParams());

        try (FileOutputStream fos = new FileOutputStream(outputFile, mode == MODE_APPEND);
             OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
             BufferedWriter out = new BufferedWriter(osw)) {

            putTemplatePlaceholders(replacements, params);

            // write BOM for new files if specified
            if (bom && mode != MODE_APPEND) { out.write(Encoding.BOM); }

            // header
            if (isNotBlank(template.getHeader())) {
                String header = format(template.getHeader(), replacements);
                out.write(forceLineSeparator(trim(header), lineSeparator));
                out.write(lineSeparator);
            }

            // footer (format it here, before putting index and csv placeholders)
            String footer = null;
            if (isNotBlank(template.getFooter())) {
                footer = format(template.getFooter(), replacements);
            }

            // pattern
            String pattern = forceLineSeparator(trim(template.getPattern()), lineSeparator);
            for (int rowIndex = 0; rowIndex < csv.length & rowIndex < MAX_CSV_SIZE; rowIndex++) {
                String[] row = csv[rowIndex];

                putIndexPlaceholders(replacements, rowIndex);
                putCsvPlaceholders(replacements, row);

                out.write(format(pattern, replacements));
                out.write(lineSeparator);
            }

            if (footer != null) {
                out.write(forceLineSeparator(trim(footer), lineSeparator));
                out.write(lineSeparator);
            }
        } catch (Exception e) {
            throw new TelekitException(I18n.t(MSG_GENERIC_IO_ERROR), e);
        }
    }

    public static List<String> validate(Template template, String[][] csv) {
        Set<Param> params = emptyIfNull(template.getParams());
        Map<String, String> replacements = new HashMap<>();
        List<String> warnings = new ArrayList<>();

        // verify max size
        if (csv.length > MAX_CSV_SIZE) {
            warnings.add(I18n.t(TOOLS_MSG_VALIDATION_CSV_THRESHOLD_EXCEEDED, MAX_CSV_SIZE));
        }

        // verify that all non-autogenerated params values has been specified
        boolean hasBlankValues = putTemplatePlaceholders(replacements, params);
        if (hasBlankValues) { warnings.add(I18n.t(TOOLS_MSG_VALIDATION_BLANK_PARAM_VALUES)); }

        String headerAfterFormatting = template.getHeader() != null ?
                format(template.getHeader(), replacements) : null;

        String footerAfterFormatting = template.getHeader() != null ?
                format(template.getFooter(), replacements) : null;

        int firstRowSize = 0, maxRowSize = 0;
        String firstLineAfterFormatting = "";
        for (int rowIndex = 0; rowIndex < csv.length & rowIndex < MAX_CSV_SIZE; rowIndex++) {
            String[] row = csv[rowIndex];
            if (rowIndex == 0) {
                firstRowSize = maxRowSize = row.length;
                // unresolved placeholders validation can be performed for the first line only
                putIndexPlaceholders(replacements, rowIndex);
                putCsvPlaceholders(replacements, row);
                firstLineAfterFormatting = format(template.getPattern(), replacements);
            } else {
                maxRowSize = row.length;
            }
        }

        // verify that all csv table rows has the same columns count
        if (firstRowSize != maxRowSize) { warnings.add(I18n.t(TOOLS_MSG_VALIDATION_MIXED_CSV)); }

        // verify that all placeholders has been replaced
        if (containsPlaceholders(firstLineAfterFormatting) |
                (headerAfterFormatting != null && containsPlaceholders(headerAfterFormatting)) |
                (footerAfterFormatting != null && containsPlaceholders(footerAfterFormatting))
        ) {
            warnings.add(I18n.t(TOOLS_MSG_VALIDATION_UNRESOLVED_PLACEHOLDERS));
        }

        return warnings;
    }

    private static String forceLineSeparator(String text, String lineSeparator) {
        return String.join(lineSeparator, text.split(LineSeparator.LINE_SPLIT_PATTERN));
    }
}
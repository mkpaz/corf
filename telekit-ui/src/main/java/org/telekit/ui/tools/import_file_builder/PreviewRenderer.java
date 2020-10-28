package org.telekit.ui.tools.import_file_builder;

import j2html.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.telekit.base.domain.LineSeparator;
import org.telekit.base.util.CollectionUtils;
import org.telekit.base.util.PlaceholderReplacer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static j2html.TagCreator.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.util.CollectionUtils.isNotEmpty;
import static org.telekit.base.util.CollectionUtils.nullToEmpty;

public final class PreviewRenderer {

    public static String render(Template template) {
        Tag<?> templateHTML;
        if (isNotBlank(template.getDelimiter())) {
            String delimiter = template.getDelimiter();
            if ("\\t".equals(delimiter)) delimiter = "\t";
            if ("\\s".equals(delimiter)) delimiter = "\s";

            List<Row> theadRows = splitHeaderToTable(template.getHeader(), delimiter);
            List<Row> tbodyRows = splitTextToTable(template.getPattern(), delimiter);
            tbodyRows.addAll(splitTextToTable(template.getFooter(), delimiter));

            Tag<?> head = thead(
                    each(theadRows, row -> tr(
                            each(row.cells, cell -> th(cell.content).attr("colspan", cell.colspan))
                    )));
            Tag<?> body = tbody(
                    each(tbodyRows, row -> tr(
                            each(row.cells, cell -> th(cell.content))
                    )));

            templateHTML = table(attrs("#template"), head, body);
        } else {
            templateHTML =
                    div(attrs("#template"),
                        iff(isNotBlank(template.getHeader()), div(attrs(".header"), template.getHeader())),
                        div(attrs(".pattern"), template.getPattern()),
                        div(attrs(".footer"), template.getFooter())
                    );
        }

        Tag<?> paramsHTML =
                table(attrs("#params"),
                      thead(
                              th("NAME"),
                              th("TYPE"),
                              th("LENGTH")
                      ),
                      tbody(each(nullToEmpty(template.getParams()), param ->
                              tr(td(param.getName()),
                                 td(String.valueOf(param.getType())),
                                 td(param.getLength() > 0 ? String.valueOf(param.getLength()) : "")
                              )
                      ))
                );

        String content =
                div(
                        attrs(".wrapper"),
                        h3("Template: " + template.getName()),
                        templateHTML,
                        iff(isNotBlank(template.getDescription()), h3("Description:")),
                        iff(isNotBlank(template.getDescription()), pre(attrs(".description"), template.getDescription())),
                        iff(isNotEmpty(template.getParams()), h3("Params:")),
                        iff(isNotEmpty(template.getParams()), paramsHTML)
                )
                        .render()
                        .replaceAll('(' + PlaceholderReplacer.PLACEHOLDER_PATTERN + ')', "<span class='placeholder'>$1</span>");

        return document(html(
                head(style(CSS)),
                body(rawHtml(content))
        ));
    }

    private static List<Row> splitHeaderToTable(String text, String delimiter) {
        if (text == null || text.isBlank()) return Collections.emptyList();

        List<Row> rows = new ArrayList<>();
        for (String line : text.split(LineSeparator.LINE_SPLIT_PATTERN)) {
            List<Cell> cells = new ArrayList<>();
            String[] chunks = splitLineToCells(line, delimiter);
            for (int cellIdx = 0; cellIdx < chunks.length; cellIdx++) {
                String cellContent = chunks[cellIdx];
                if (cellIdx == 0 || !cellContent.isBlank()) {
                    cells.add(new Cell(trim(cellContent), 1));
                } else {
                    Cell lastCell = CollectionUtils.getLast(cells);
                    lastCell.colspan++;
                }

                // last cell spans all remaining columns
                if (cellIdx == chunks.length - 1) {
                    Cell lastCell = CollectionUtils.getLast(cells);
                    lastCell.colspan = 999;
                }
            }
            rows.add(new Row(cells));
        }

        return rows;
    }

    private static List<Row> splitTextToTable(String text, String delimiter) {
        if (text == null || text.isBlank()) return Collections.emptyList();

        List<Row> rows = new ArrayList<>();
        for (String line : text.split(LineSeparator.LINE_SPLIT_PATTERN)) {
            List<Cell> cells = new ArrayList<>();
            for (String cell : splitLineToCells(line, delimiter)) {
                cells.add(new Cell(trim(cell), 1));
            }
            rows.add(new Row(cells));
        }

        return rows;
    }

    private static String[] splitLineToCells(String line, String delimiter) {
        line = StringUtils.defaultString(line, "").trim();

        if (line.startsWith(delimiter)) line = line.substring(line.indexOf(delimiter) + 1);
        if (line.endsWith(delimiter)) line = line.substring(0, line.lastIndexOf(delimiter));
        return line.split(Pattern.quote(delimiter), -1);
    }

    private static class Row {

        public final List<Cell> cells;

        public Row(List<Cell> cells) {
            this.cells = cells;
        }
    }

    private static class Cell {

        public final String content;
        public int colspan;

        public Cell(String content, int colspan) {
            this.content = content;
            this.colspan = colspan;
        }
    }

    private static final String CSS = """
            body { overflow: auto; width: 100%; font-family: monospace; font-size: 11pt; }
            .wrapper { display: inline-block; padding: 10px 20px; word-break: keep-all; }
            table { border-collapse: collapse; }
            th { font-weight: bold; min-width: 100px; }
            td, th { text-align: left; padding: 4px 8px; border: 1px solid #9e9e9e; }
            .placeholder { color: #e91e63; }
            """;
}
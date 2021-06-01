package org.telekit.desktop.tools.apiclient;

import j2html.tags.Tag;
import org.telekit.base.util.PlaceholderReplacer;

import static j2html.TagCreator.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.telekit.base.util.CollectionUtils.ensureNotNull;
import static org.telekit.base.util.CollectionUtils.isNotEmpty;

public final class PreviewRenderer {

    public static String render(Template template) {
        Tag<?> templateHTML =
                div(attrs("#template"),
                    p(
                            span(template.getMethod() + ":"),
                            rawHtml("&nbsp;"),
                            span(template.getUri())
                    ),
                    p("Batch Size: " + template.getBatchSize()),
                    p("Batch Separator: " + template.getBatchSeparator()),
                    iff(isNotBlank(template.getDescription()), h3("Description:")),
                    iff(isNotBlank(template.getDescription()), pre(attrs(".description"), template.getDescription())),
                    iff(isNotBlank(template.getHeaders()), h3("Headers:")),
                    iff(isNotBlank(template.getHeaders()), pre(attrs(".headers"), template.getHeaders())),
                    iff(isNotBlank(template.getBody()), h3("Body:")),
                    iff(isNotBlank(template.getBody()), pre(attrs(".body"), template.getBody()))
                );

        Tag<?> paramsHTML =
                table(attrs("#params"),
                      thead(
                              th("NAME"),
                              th("TYPE"),
                              th("LENGTH")
                      ),
                      tbody(
                              each(ensureNotNull(template.getParams()), param ->
                                      tr(
                                              td(param.getName()),
                                              td(String.valueOf(param.getType())),
                                              td(param.getLength() > 0 ? String.valueOf(param.getLength()) : "")
                                      )
                              )
                      )
                );

        Tag<?> wrapperHTML = div(attrs(".wrapper"),
                                 h3("Template: " + template.getName()),
                                 templateHTML,
                                 iff(isNotEmpty(template.getParams()), h3("Params:")),
                                 iff(isNotEmpty(template.getParams()), paramsHTML)
        );

        String content = wrapperHTML
                .render()
                .replaceAll("(" + PlaceholderReplacer.PLACEHOLDER_PATTERN + ")", "<span class='placeholder'>$1</span>");

        return document(html(
                head(style(CSS)),
                body(rawHtml(content))
        ));
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
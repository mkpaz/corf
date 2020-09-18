package org.telekit.ui.tools.ss7;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.telekit.base.Messages;
import org.telekit.base.Settings;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.fx.Controller;
import org.telekit.base.util.DesktopUtils;
import org.telekit.base.util.telecom.SS7Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static j2html.TagCreator.*;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.telekit.base.util.CollectionUtils.*;
import static org.telekit.base.util.telecom.SS7Utils.*;
import static org.telekit.ui.main.AllMessageKeys.MSG_GENERIC_IO_ERROR;

public class CICTableController extends Controller {

    private static final String PREVIEW_FILE_NAME = "cic-table.preview.html";

    public @FXML ListView<Integer> listE1;
    public @FXML ListView<String> listTimeslots;
    public @FXML TextField tfCicSearch;
    public @FXML Label lbFirstCic;
    public @FXML Label lbLastCic;

    @FXML
    public void initialize() {
        listE1.setItems(FXCollections.observableArrayList(generate(1, SS7Utils.MAX_CIC / 32)));
        listE1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateTimeslots(newValue - 1); // E1 number starts from 0, but shown from 1
                listTimeslots.scrollTo(0);
            }
        });

        tfCicSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isNotBlank(newValue)) {
                findCIC(newValue);
            } else {
                listE1.getSelectionModel().selectFirst();
                listTimeslots.getSelectionModel().selectFirst();
                listTimeslots.scrollTo(0);
            }
        });

        listE1.getSelectionModel().selectFirst();
    }

    private void updateTimeslots(Integer e1Num) {
        ObservableList<String> timeslots = FXCollections.observableArrayList();
        List<Integer> cicIDs = getCICRange(e1Num);

        for (int index = 0; index < cicIDs.size(); index++) {
            timeslots.add(leftPad(String.valueOf(index + 1), 2) + " - " + cicIDs.get(index));
        }

        listTimeslots.setItems(timeslots);
        lbFirstCic.setText(
                String.valueOf(getFirst(cicIDs))
        );
        lbLastCic.setText(
                String.valueOf(getLast(cicIDs))
        );
    }

    private void findCIC(String cicStr) {
        try {
            int e1Num = findE1ByCIC(Integer.parseInt(cicStr));
            if (e1Num >= 0) {
                listE1.getSelectionModel().select(e1Num);
                listE1.scrollTo(e1Num - 1);
            }

            int timeslot = findTimeslotByCIC(Integer.parseInt(cicStr));
            if (timeslot > 0) {
                listTimeslots.getSelectionModel().select(timeslot - 1);
                listTimeslots.scrollTo(timeslot - 1);
            }
        } catch (Exception ignored) {
        }
    }

    @FXML
    public void showInBrowser() {
        StringBuilder tableBuilder = new StringBuilder();

        tableBuilder.append("<table class=\"cic\">");

        tableBuilder.append("<thead>");
        tableBuilder.append("<th>E1 / Timeslot</th>\n");

        for (int timeslot = 1; timeslot <= 31; timeslot++) {
            tableBuilder.append("<th>");
            tableBuilder.append(timeslot);
            tableBuilder.append("</th>\n");
        }
        tableBuilder.append("</thead>\n");

        tableBuilder.append("<tbody>");
        for (int e1Num = 0; e1Num < SS7Utils.MAX_CIC / 32; e1Num++) {
            tableBuilder.append("<tr>\n");

            tableBuilder.append("<th>");
            tableBuilder.append(e1Num + 1);
            tableBuilder.append("</th>\n");

            List<Integer> cicIDs = getCICRange(e1Num);
            for (Integer cicID : cicIDs) {
                tableBuilder.append("<td>");
                tableBuilder.append(cicID);
                tableBuilder.append("</td>\n");
            }
            tableBuilder.append("</tr>\n");
        }
        tableBuilder.append("</tbody>\n");

        tableBuilder.append("</table>\n");

        // content
        String html = document(html(
                head(style(CSS)),
                body(div(attrs(".wrapper"),
                         h3("CIC Table: "),
                         rawHtml(tableBuilder.toString())
                ))
        ));

        File outputFile = Settings.TEMP_DIR.resolve(PREVIEW_FILE_NAME).toFile();
        try {
            Files.writeString(outputFile.toPath(), html);
            DesktopUtils.browse(outputFile.toURI());
        } catch (IOException e) {
            throw new TelekitException(Messages.get(MSG_GENERIC_IO_ERROR), e);
        }
    }

    private static final String CSS =
            "body { overflow: auto; width: 100%; font-family: monospace; font-size: 11pt; }\n" +
                    ".wrapper { display: inline-block; padding: 10px 20px; word-break: keep-all; }\n" +
                    "table { border-collapse: collapse; }\n" +
                    "th { font-weight: bold; text-align: center; background-color: #EEEEEE; }\n" +
                    "td, th { text-align: center; padding: 4px 8px; border: 1px solid #9E9E9E; }\n";

    @Override
    public void reset() {}
}

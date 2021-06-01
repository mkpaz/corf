package org.telekit.desktop.tools.ss7;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.telekit.base.ui.Controller;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.telekit.base.telecom.ss7.ISUPUtils.*;
import static org.telekit.base.util.CollectionUtils.*;

public class CICTableController extends Controller {

    public @FXML ListView<Integer> listE1;
    public @FXML ListView<String> listTimeslots;
    public @FXML TextField tfCicSearch;
    public @FXML Label lbFirstCic;
    public @FXML Label lbLastCic;

    @FXML
    public void initialize() {
        tfCicSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isNotBlank(newVal)) {
                findCICPositionAndScrollToIt(newVal);
            } else {
                listE1.getSelectionModel().selectFirst();
                listTimeslots.getSelectionModel().selectFirst();
                listTimeslots.scrollTo(0);
            }
        });

        listE1.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            // E1 numbers starts from 0, but list values starts from 1
            updateCICInfo(newVal - 1);
            listTimeslots.scrollTo(0);
        });
        listE1.setItems(FXCollections.observableArrayList(generate(1, MAX_CIC / 32)));
        listE1.getSelectionModel().selectFirst();
    }

    private void updateCICInfo(Integer e1num) {
        ObservableList<String> timeslots = FXCollections.observableArrayList();
        List<Integer> cicIDs = getCICRange(e1num);

        for (int index = 0; index < cicIDs.size(); index++) {
            timeslots.add(leftPad(String.valueOf(index + 1), 2) + " - " + cicIDs.get(index));
        }

        listTimeslots.setItems(timeslots);
        lbFirstCic.setText(String.valueOf(getFirst(cicIDs)));
        lbLastCic.setText(String.valueOf(getLast(cicIDs)));
    }

    private void findCICPositionAndScrollToIt(String str) {
        try {
            int cic = Integer.parseInt(str);

            int e1num = findE1ByCIC(cic);
            if (e1num >= 0) {
                listE1.getSelectionModel().select(e1num);
                listE1.scrollTo(e1num - 1);
            }

            int timeslot = findTimeslotByCIC(cic);
            if (timeslot > 0) {
                listTimeslots.getSelectionModel().select(timeslot - 1);
                listTimeslots.scrollTo(timeslot - 1);
            }
        } catch (Exception ignored) {}
    }
}

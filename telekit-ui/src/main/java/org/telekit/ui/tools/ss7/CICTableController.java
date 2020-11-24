package org.telekit.ui.tools.ss7;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.telekit.base.telecom.ss7.SS7Utils;
import org.telekit.base.ui.Controller;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.telekit.base.telecom.ss7.SS7Utils.*;
import static org.telekit.base.util.CollectionUtils.*;

public class CICTableController extends Controller {

    public @FXML ListView<Integer> listE1;
    public @FXML ListView<String> listTimeslots;
    public @FXML TextField tfCicSearch;
    public @FXML Label lbFirstCic;
    public @FXML Label lbLastCic;

    @FXML
    public void initialize() {
        tfCicSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isNotBlank(newValue)) {
                findCIC(newValue);
            } else {
                listE1.getSelectionModel().selectFirst();
                listTimeslots.getSelectionModel().selectFirst();
                listTimeslots.scrollTo(0);
            }
        });

        listE1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateTimeslots(newValue - 1); // E1 number starts from 0, but shown from 1
                listTimeslots.scrollTo(0);
            }
        });
        listE1.setItems(FXCollections.observableArrayList(generate(1, SS7Utils.MAX_CIC / 32)));
        listE1.getSelectionModel().selectFirst();
    }

    private void updateTimeslots(Integer e1Num) {
        ObservableList<String> timeslots = FXCollections.observableArrayList();
        List<Integer> cicIDs = getCICRange(e1Num);

        for (int index = 0; index < cicIDs.size(); index++) {
            timeslots.add(leftPad(String.valueOf(index + 1), 2) + " - " + cicIDs.get(index));
        }

        listTimeslots.setItems(timeslots);
        lbFirstCic.setText(String.valueOf(getFirst(cicIDs)));
        lbLastCic.setText(String.valueOf(getLast(cicIDs)));
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
        } catch (Exception ignored) {}
    }
}

package corf.base.desktop.controls;

import atlantafx.base.theme.Styles;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class HorizontalForm extends GridPane {

    public HorizontalForm() {
        super();
    }

    public HorizontalForm(double hgap, double vgap) {
        super();
        setHgap(hgap);
        setVgap(vgap);
        getStyleClass().add("horizontal-form");
    }

    public void add(String labelText, Node... inputs) {
        add(getRowCount(), labelText, false, inputs);
    }

    public void add(String labelText, boolean required, Node... inputs) {
        add(getRowCount(), labelText, required, inputs);
    }

    public void add(int rowIndex, String labelText, boolean required, Node... inputs) {
        var label = new Label(labelText);
        label.setWrapText(false);

        if (required) {
            var mark = new Text("*");
            mark.getStyleClass().addAll("text", "danger");
            label.setContentDisplay(ContentDisplay.RIGHT);
            label.setGraphic(mark);
        }

        add(rowIndex, label, inputs);
    }

    public void add(int rowIndex, Node label, Node... inputs) {
        add(label, 0, rowIndex);
        if (inputs.length == 0) { return; }

        Node n;
        if (inputs.length == 1) {
            n = inputs[0];
        } else {
            n = new HBox(inputs);
            setInputGroup(inputs);
        }

        if (label instanceof Label l) {
            l.setLabelFor(n);
        }

        add(n, 1, rowIndex);
    }

    protected void setInputGroup(Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];

            if (i == 0) {
                n.getStyleClass().add(Styles.LEFT_PILL);
            } else if (i == nodes.length - 1) {
                n.getStyleClass().add(Styles.RIGHT_PILL);
            } else {
                n.getStyleClass().add(Styles.CENTER_PILL);
            }
        }
    }
}

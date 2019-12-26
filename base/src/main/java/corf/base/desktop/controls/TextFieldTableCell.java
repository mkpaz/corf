package corf.base.desktop.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Editable text table cell that provides more control over text field behaviour.
 * Namely, the {@link #editablePredicate} allows to manipulate editable state depending
 * on cell value and {@link #textFormatterFactory} allows to pass {@link TextFormatter}
 * to the internal text field to control user input.
 * <p>
 * If {@link #textFormatterFactory} return an instance of the {@link CellTextFormatter}
 * it will be notified on start / stop edit events.
 */
public class TextFieldTableCell<T> extends TableCell<T, String> {

    protected final @Nullable Predicate<T> editablePredicate;
    protected final @Nullable Supplier<TextFormatter<T>> textFormatterFactory;

    @SuppressWarnings("NullAway.Init")
    protected TextField textField;

    public TextFieldTableCell() {
        this(null, null);
    }

    /**
     * @param predicate the predicate to determine whether table cell editable or not
     * @param factory text formatter factory, according to Javafx docs {@link TextFormatter} instance
     * can not be reused for multiple text fields
     */
    public TextFieldTableCell(@Nullable Predicate<T> predicate,
                              @Nullable Supplier<TextFormatter<T>> factory) {
        super();

        this.textFormatterFactory = factory;
        this.editablePredicate = predicate;

        getStyleClass().add("text-field-table-cell");
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) { return; }

        T rowItem = getRowItem();
        if (rowItem != null && editablePredicate != null && !editablePredicate.test(rowItem)) { return; }

        super.startEdit();

        if (isEditing()) {
            if (textField == null) {
                textField = createTextField();
            }

            textField.setText(getItem());

            setText(null);
            setGraphic(textField);

            if (textField.getTextFormatter() instanceof CellTextFormatter ctf) {
                ctf.startEdit(rowItem);
            }

            textField.selectAll();
            textField.requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());

        if (textField.getTextFormatter() instanceof CellTextFormatter ctf) {
            ctf.finishEdit();
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);

        if (textField.getTextFormatter() instanceof CellTextFormatter ctf) {
            ctf.finishEdit();
        }
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (isEmpty()) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItem());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getItem());
                setGraphic(null);
            }
        }
    }

    protected TextField createTextField() {
        var textField = new TextField(getItem());

        if (textFormatterFactory != null) {
            textField.setTextFormatter(textFormatterFactory.get());
        }

        // commit cell edit on ENTER
        textField.setOnAction(event -> {
            commitEdit(textField.getText());
            event.consume();
        });

        // cancel cell edit on ESC
        textField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                event.consume();
            }
        });

        // commit cell edit on focus lost
        textField.focusedProperty().addListener((obs, old, val) -> {
            if (!val) {
                commitEdit(textField.getText());
            }
        });

        return textField;
    }

    protected @Nullable T getRowItem() {
        TableRow<T> row = getTableRow();
        return row != null && row.getItem() != null ? row.getItem() : null;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class CellTextFormatter<T> extends TextFormatter<T> {

        protected @Nullable T rowItem;

        public CellTextFormatter(UnaryOperator<Change> filter) {
            super(filter);
        }

        public CellTextFormatter(StringConverter<T> valueConverter) {
            super(valueConverter);
        }

        public CellTextFormatter(StringConverter<T> valueConverter, T defaultValue) {
            super(valueConverter, defaultValue);
        }

        public CellTextFormatter(StringConverter<T> valueConverter, T defaultValue, UnaryOperator<Change> filter) {
            super(valueConverter, defaultValue, filter);
        }

        /** This method has to be called when user start to edit the cell. */
        public void startEdit(@Nullable T rowItem) {
            this.rowItem = rowItem;
        }

        /** This method has to be called after user finished edit the cell. */
        public void finishEdit() {
            this.rowItem = null;
        }

        protected @Nullable T getRowItem() {
            return rowItem;
        }
    }
}

package org.telekit.ui.test;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.telekit.base.fx.Controller;
import org.telekit.base.i18n.Messages;
import org.telekit.controls.ContextMenuPolicy;
import org.telekit.controls.richtextfx.RichTextFXContextMenu;
import org.telekit.controls.richtextfx.RichTextFXHelper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PreviewController extends Controller {

    public @FXML ChoiceBox<String> choiceBoxA;
    public @FXML ChoiceBox<String> choiceBoxB;
    public @FXML ComboBox<String> comboBoxA;
    public @FXML ComboBox<String> comboBoxB;
    public @FXML Spinner<Integer> spinnerA;
    public @FXML Spinner<Integer> spinnerB;
    public @FXML DatePicker datePickerA;
    public @FXML TextArea textAreaA;
    public @FXML VirtualizedScrollPane<StyleClassedTextArea> styleClassedTextAreaPane;
    public @FXML StyleClassedTextArea styleClassedTextArea;
    public @FXML VirtualizedScrollPane<CodeArea> codeAreaPane;
    public @FXML CodeArea codeArea;
    public @FXML ListView<String> listViewA;
    public @FXML TreeView<Book> treeViewA;
    public @FXML TableView<Book> tableViewA;
    public @FXML TableColumn<Book, String> tableColAC0;
    public @FXML TableColumn<Book, String> tableColAC1;
    public @FXML TableColumn<Book, String> tableColAC2;
    public @FXML TreeTableView<Book> treeTableViewA;
    public @FXML TreeTableColumn<Book, String> treeTableColAC1;
    public @FXML TreeTableColumn<Book, String> treeTableColAC2;

    private static final Random randomGen = new Random();
    private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused");
    private static final String LOREM_IPSUM = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec in lorem ipsum.\
            Nulla malesuada elit justo, eu volutpat ex pellentesque vitae.""";

    @FXML
    public void initialize() {
        choiceBoxA.getItems().addAll(List.of("foo", "bar", "baz"));
        choiceBoxB.getItems().addAll(List.of("baz", "bar", "foo"));

        comboBoxA.getItems().addAll(List.of("foo", "bar", "baz"));
        comboBoxB.getItems().addAll(List.of("baz", "bar", "foo"));

        datePickerA.setValue(LocalDate.now());
        textAreaA.setText((LOREM_IPSUM + "\n").repeat(42));
        listViewA.getItems().addAll(List.of("foo", "bar", "baz"));

        initStyleClassedTextArea();
        initCodeArea();

        initTreeViewA();
        initTableViewA();
        initTreeTableViewA();
    }

    private void initCodeArea() {
        RichTextFXHelper.addFocusedStateListener(codeArea, codeAreaPane);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.replaceText(0, 0, (LOREM_IPSUM + "\n").repeat(42));
        codeArea.showParagraphAtTop(0);
    }

    private void initStyleClassedTextArea() {
        RichTextFXHelper.addFocusedStateListener(styleClassedTextArea, styleClassedTextAreaPane);
        styleClassedTextArea.setWrapText(false);
        styleClassedTextArea.setEditable(true);

        styleClassedTextArea.replaceText(0, 0, (LOREM_IPSUM + "\n").repeat(42));
        styleClassedTextArea.showParagraphAtTop(0);

        RichTextFXContextMenu contextMenu = new RichTextFXContextMenu(
                Messages.getInstance(),
                new ContextMenuPolicy(false, true)
        );
        styleClassedTextArea.setContextMenu(contextMenu);
    }

    private void initTreeViewA() {
        TreeItem<Book> root = new TreeItem<>(createBook());
        root.setExpanded(true);

        List<TreeItem<Book>> items = createBooks(10).stream()
                .map(TreeItem::new)
                .collect(Collectors.toList());
        root.getChildren().addAll(items);
        treeViewA.setRoot(root);
    }

    private void initTableViewA() {
        tableColAC0.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        tableColAC0.setText("ISBN");
        tableColAC1.setCellValueFactory(new PropertyValueFactory<>("author"));
        tableColAC1.setText("Author");
        tableColAC2.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableColAC2.setText("Title");
        tableViewA.setItems(FXCollections.observableArrayList(createBooks(10)));
    }

    private void initTreeTableViewA() {
        TreeItem<Book> treeTableViewARoot = new TreeItem<>(createBook());
        treeTableViewARoot.setExpanded(true);
        List<TreeItem<Book>> treeTableViewAItems = createBooks(10).stream()
                .map(TreeItem::new)
                .collect(Collectors.toList());
        treeTableViewARoot.getChildren().addAll(treeTableViewAItems);
        treeTableColAC1.setCellValueFactory(new TreeItemPropertyValueFactory<>("author"));
        treeTableColAC1.setText("Author");
        treeTableColAC2.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
        treeTableColAC2.setText("Title");
        treeTableViewA.setRoot(treeTableViewARoot);
    }

    @Override
    public void reset() {}

    private static Book createBook() {
        Map<String, String> books = Map.ofEntries(
                new AbstractMap.SimpleEntry<>("Don Quixote", "Miguel de Cervantes"),
                new AbstractMap.SimpleEntry<>("A Tale of Two Cities", "Charles Dickens"),
                new AbstractMap.SimpleEntry<>("The Lord of the Rings", "John Tolkien"),
                new AbstractMap.SimpleEntry<>("The Little Prince", "Antoine de Saint-Exupery"),
                new AbstractMap.SimpleEntry<>("Harry Potter and the Sorcerer’s Stone", "Joanne Rowling"),
                new AbstractMap.SimpleEntry<>("And Then There Were None", "Agatha Christie"),
                new AbstractMap.SimpleEntry<>("The Dream of the Red Chamber", "Cao Xueqin"),
                new AbstractMap.SimpleEntry<>("The Hobbit", "John Tolkien"),
                new AbstractMap.SimpleEntry<>("She: A History of Adventure", "Henry Haggard"),
                new AbstractMap.SimpleEntry<>("The Lion, the Witch and the Wardrobe", "Clive Lewis")
        );

        List<String> keys = new ArrayList<>(books.keySet());
        String randomTitle = keys.get(randomGen.nextInt(keys.size()));
        return new Book(UUID.randomUUID().toString(), books.get(randomTitle), randomTitle);
    }

    private static List<Book> createBooks(int size) {
        List<Book> books = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            books.add(createBook());
        }
        return books;
    }

    public static class Book {

        private SimpleStringProperty isbn;
        private SimpleStringProperty author;
        private SimpleStringProperty title;

        public Book(String isbn, String author, String title) {
            this.isbn = new SimpleStringProperty(isbn);
            this.author = new SimpleStringProperty(author);
            this.title = new SimpleStringProperty(title);
        }

        public String getIsbn() {
            return isbn.get();
        }

        public void setIsbn(String isbn) {
            this.isbn.set(isbn);
        }

        public String getAuthor() {
            return author.get();
        }

        public void setAuthor(String author) {
            this.author.set(author);
        }

        public String getTitle() {
            return title.get();
        }

        public void setTitle(String title) {
            this.title.set(title);
        }

        @Override
        public String toString() {
            return getAuthor() + " \"" + getTitle() + "\"";
        }
    }
}

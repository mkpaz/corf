package corf.base.desktop;

import javafx.scene.control.TextFormatter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class RegexUnaryOperator implements UnaryOperator<TextFormatter.Change> {

    protected final Pattern pattern;

    public RegexUnaryOperator(Pattern pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
    }

    @Override
    public TextFormatter.@Nullable Change apply(TextFormatter.Change change) {
        String newText = change.getControlNewText();
        if (newText.isEmpty() || pattern.matcher(newText).matches()) {
            return change;
        } else {
            return null;
        }
    }

    public static TextFormatter<String> createTextFormatter(Pattern pattern) {
        return new TextFormatter<>(new RegexUnaryOperator(pattern));
    }
}

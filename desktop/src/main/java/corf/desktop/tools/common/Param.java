package corf.desktop.tools.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import corf.base.common.KeyValue;
import corf.base.common.NumberUtils;
import corf.base.text.PasswordGenerator;
import corf.base.text.StringUtils;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import static corf.base.text.PasswordGenerator.ASCII_LOWER_UPPER_DIGITS;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Param implements Comparable<Param> {

    public static final String PLACEHOLDER_CHARACTERS = "[a-zA-Z0-9_\\-]+";

    public enum Type {
        CHOICE,
        CONSTANT,
        DATAFAKER,
        PASSWORD,
        PASSWORD_BASE64,
        TIMESTAMP,
        UUID
    }

    public static final Comparator<Param> COMPARATOR = Comparator.comparing(Param::getName);
    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int DEFAULT_PASSWORD_LENGTH = 16;

    private String name;
    private Type type = Type.CONSTANT;
    private @Nullable String option;
    private @Nullable String value;

    @SuppressWarnings("NullAway.Init")
    public Param() { }

    public Param(String name,
                 Type type,
                 @Nullable String option,
                 @Nullable String value
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.option = option;
        this.value = value;
    }

    public Param(Param param) {
        this.name = param.name;
        this.type = param.type;
        this.option = param.option;
        this.value = param.value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public @Nullable String getOption() {
        return option;
    }

    public void setOption(@Nullable String option) {
        this.option = option;
    }

    public @Nullable String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    /**
     * Specifies whether param is auto-generated or not. If it does, it can be optionally
     * configured by setting an appropriate {@link #option}. See <code/>isConfigurable</code>.
     */
    @JsonIgnore
    public boolean isAutoGenerated() {
        return !(type == Type.CONSTANT || type == Type.CHOICE);
    }

    /**
     * Specifies whether param supports configuration or not. Do not confuse this with
     * the auto-generation. E.g. PASSWORD is auto-generated and configurable (length) while
     * UUID is just auto-generated.
     */
    @JsonIgnore
    public boolean isConfigurable() {
        return type == Type.CHOICE
                || type == Type.DATAFAKER
                || type == Type.PASSWORD
                || type == Type.PASSWORD_BASE64;
    }

    public KeyValue<String, String> resolve() {
        // auto-generated params
        return switch (getType()) {
            case DATAFAKER -> new KeyValue<>(name, TemplateWorker.FAKER.expression(getOption()));
            case PASSWORD -> new KeyValue<>(name, generatePassword());
            case PASSWORD_BASE64 -> new KeyValue<>(name, StringUtils.toBase64(generatePassword()));
            case TIMESTAMP -> new KeyValue<>(name, String.valueOf(Instant.now().toEpochMilli()));
            case UUID -> new KeyValue<>(name, String.valueOf(UUID.randomUUID()));
            // constant params
            default -> new KeyValue<>(name, Objects.requireNonNullElse(getValue(), ""));
        };
    }

    @Override
    @SuppressWarnings("EqualsGetClass")
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Param param = (Param) o;
        return name.equals(param.name);
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean deepEquals(Param that) {
        if (!Objects.equals(this, that)) { return false; }
        if (!Objects.equals(this.name, that.name)) { return false; }
        if (!Objects.equals(this.type, that.type)) { return false; }
        if (!Objects.equals(this.option, that.option)) { return false; }
        if (!Objects.equals(this.value, that.value)) { return false; }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Param{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", option='" + option + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NotNull Param other) {
        return COMPARATOR.compare(this, other);
    }

    public Param copy() {
        return new Param(this);
    }

    private String generatePassword() {
        int len = DEFAULT_PASSWORD_LENGTH;

        try {
            if (getOption() != null && !getOption().isBlank()) {
                len = NumberUtils.ensureRange(
                        Integer.parseInt(getOption().trim()),
                        MIN_PASSWORD_LENGTH,
                        MAX_PASSWORD_LENGTH,
                        DEFAULT_PASSWORD_LENGTH
                );
            }
        } catch (Exception ignored) { /* ignore */ }

        return PasswordGenerator.random(len, ASCII_LOWER_UPPER_DIGITS);
    }
}

package corf.desktop.tools.common;

import corf.desktop.i18n.DM;

import java.util.ArrayList;
import java.util.List;

import static corf.base.i18n.I18n.t;

public final class ReplacementCheckResult {

    private long maxSize = 0;
    private long actualSize = 0;
    private boolean containsBlankValues;
    private boolean hasVariableRowLength;
    private final List<String> invalidLines = new ArrayList<>();

    private ReplacementCheckResult() { }

    public boolean exceedsMaxSize() {
        return actualSize > maxSize;
    }

    public boolean containsBlankValues() {
        return containsBlankValues;
    }

    public boolean hasVariableRowLength() {
        return hasVariableRowLength;
    }

    public boolean containsPlaceholders() {
        return invalidLines.size() > 0;
    }

    public boolean passed() {
        return !exceedsMaxSize()
                && !containsBlankValues()
                && !hasVariableRowLength()
                && !containsPlaceholders();
    }

    public List<String> getWarnings() {
        var warnings = new ArrayList<String>();

        if (exceedsMaxSize()) {
            warnings.add(t(DM.TPL_MSG_VALIDATION_CSV_THRESHOLD_EXCEEDED, maxSize));
        }

        if (containsBlankValues()) {
            warnings.add(t(DM.TPL_MSG_VALIDATION_BLANK_PARAM_VALUES));
        }

        if (hasVariableRowLength()) {
            warnings.add(t(DM.TPL_MSG_VALIDATION_VARIABLE_CSV_LENGTH));
        }

        if (containsPlaceholders()) {
            warnings.add(t(DM.TPL_MSG_VALIDATION_UNRESOLVED_PLACEHOLDERS));
        }

        return warnings;
    }

    ///////////////////////////////////////////////////////////////////////////

    public static class Builder {

        private final ReplacementCheckResult check;

        public Builder() {
            check = new ReplacementCheckResult();
        }

        public void setSizeThresholdExceeded(long actualSize, long maxSize) {
            check.actualSize = actualSize;
            check.maxSize = maxSize;
        }

        public void setContainsBlankValues(boolean b) {
            check.containsBlankValues = b;
        }

        public void setHasVariableRowLength(boolean b) {
            check.hasVariableRowLength = b;
        }

        public void addInvalidLine(String s) {
            check.invalidLines.add(s);
        }

        public ReplacementCheckResult build() {
            return check;
        }
    }
}

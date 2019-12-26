package corf.desktop.tools.httpsender;

public record ProgressCounter(int total, int success, int failed) {

    public static final ProgressCounter EMPTY = new ProgressCounter(0, 0, 0);

    public ProgressCounter {
        if (total < 0 || success < 0 || failed < 0) {
            throw new IllegalArgumentException("Count can't be negative.");
        }
    }

    public double getProgress() {
        return (success + failed) / (double) total;
    }

    public boolean isZero() {
        return total == 0;
    }

    public ProgressCounter incrementSuccessCount() {
        return new ProgressCounter(total, success + 1, failed);
    }

    public ProgressCounter incrementFailedCount() {
        return new ProgressCounter(total, success, failed + 1);
    }
}

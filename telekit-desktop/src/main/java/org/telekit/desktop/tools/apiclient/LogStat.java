package org.telekit.desktop.tools.apiclient;

public record LogStat(int totalCount, int successCount, int failedCount) {

    public static LogStat EMPTY = new LogStat(0, 0, 0);

    public LogStat {
        if (totalCount < 0 || successCount < 0 || failedCount < 0) {
            throw new IllegalArgumentException();
        }
    }

    public boolean isEmpty() {
        return totalCount == 0;
    }

    public double getProgress() {
        return (successCount + failedCount) / (double) totalCount;
    }

    public LogStat withIncrementedSuccessCount() {
        return new LogStat(this.totalCount, this.successCount + 1, this.failedCount);
    }

    public LogStat withIncrementedFailedCount() {
        return new LogStat(this.totalCount, this.successCount, this.failedCount + 1);
    }
}

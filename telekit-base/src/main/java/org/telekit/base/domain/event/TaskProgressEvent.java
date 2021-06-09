package org.telekit.base.domain.event;

import org.telekit.base.event.Event;

// Notifies about task progress or status.
public final class TaskProgressEvent extends Event {

    private final String taskId;
    private final boolean running;
    private final long processedCount;
    private final long totalCount;

    public TaskProgressEvent(String taskId, boolean running) {
        this(taskId, running, 0, 0);
    }

    public TaskProgressEvent(String taskId, boolean running, long processedCount, long totalCount) {
        super();

        this.taskId = taskId;
        this.running = running;
        this.processedCount = processedCount;
        this.totalCount = totalCount;
    }

    public String getTaskId() { return taskId; }

    public boolean isRunning() { return running; }

    public long getProcessedCount() { return processedCount; }

    public long getTotalCount() { return totalCount; }

    public double getProgress() { return (double) totalCount / processedCount; }

    @Override
    public String toString() {
        return "TaskProgressEvent{" +
                "taskId='" + taskId + '\'' +
                ", running=" + running +
                ", processedCount=" + processedCount +
                ", totalCount=" + totalCount +
                "} " + super.toString();
    }
}
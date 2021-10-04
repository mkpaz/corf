package telekit.base.desktop.mvvm;

public interface Command extends Runnable {

    void execute();

    boolean isExecutable();

    @Override
    default void run() {
        execute();
    }
}
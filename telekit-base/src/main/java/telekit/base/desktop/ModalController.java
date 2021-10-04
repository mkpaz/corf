package telekit.base.desktop;

public interface ModalController {

    Runnable getOnCloseRequest();

    void setOnCloseRequest(Runnable r);

    default void close() {
        if (getOnCloseRequest() != null) { getOnCloseRequest().run(); }
    }
}

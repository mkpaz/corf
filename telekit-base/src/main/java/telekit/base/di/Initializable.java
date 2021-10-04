package telekit.base.di;

public interface Initializable {

    /**
     * Note that using this interface can cause conflict with <code>javafx.fxml.FXMLLoader}</code>,
     * which will result that this method will be executed twice. First time from dependency
     * injector and from FXMLLoader after that. This because FXMLLoader just executes any no-arg
     * <code>initialize() method</code>.
     * <p>
     * To avoid such conflict DO NOT MARK classes that supposed to be loaded by using
     * <code>javafx.fxml.FXMLLoader</code> with {@link Initializable} or
     * use <code>javafx.fxml.Initializable</code> instead.
     */
    void initialize();
}

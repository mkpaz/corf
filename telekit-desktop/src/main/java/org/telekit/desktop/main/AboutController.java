package org.telekit.desktop.main;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.telekit.base.Env;
import org.telekit.base.desktop.Component;
import org.telekit.base.desktop.FxmlPath;
import org.telekit.base.util.DesktopUtils;

import java.net.URI;

@FxmlPath("/org/telekit/desktop/main/about.fxml")
public class AboutController implements Component {

    public @FXML GridPane rootPane;
    public @FXML Text lbTitle;
    public @FXML Label lbVersion;
    public @FXML Hyperlink lnkHomepage;

    @FXML
    public void initialize() {
        lbTitle.setText(Env.APP_NAME);
        lbVersion.setText("v." + Env.getAppVersion());
    }

    @FXML
    public void visitHomepage() {
        DesktopUtils.browseQuietly(URI.create(lnkHomepage.getText()));
    }

    @Override
    public Region getRoot() {
        return rootPane;
    }

    @Override
    public void reset() {}
}

package org.telekit.ui.main;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.telekit.base.Env;
import org.telekit.base.fx.Controller;
import org.telekit.base.util.DesktopUtils;

import java.net.URI;

public class AboutController extends Controller {

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
    public void reset() {}
}

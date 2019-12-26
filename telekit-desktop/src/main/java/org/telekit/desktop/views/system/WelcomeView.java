package org.telekit.desktop.views.system;

import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.javafx.FontIcon;
import org.telekit.base.Env;
import org.telekit.base.desktop.mvvm.View;
import org.telekit.base.desktop.routing.Route;
import org.telekit.base.di.Initializable;
import org.telekit.base.util.DesktopUtils;
import org.telekit.controls.util.Containers;
import org.telekit.controls.util.Controls;
import org.telekit.desktop.IconCache;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;

import static org.telekit.base.i18n.I18n.t;
import static org.telekit.desktop.IconCache.ICON_APP;
import static org.telekit.desktop.i18n.DesktopMessages.SYSTEM_PROJECT_PAGE;

@Singleton
public class WelcomeView extends VBox implements Initializable, View<WelcomeViewModel> {

    public static final Route ROUTE = new Route(WelcomeView.class.getCanonicalName());

    Hyperlink projectLink;

    private final WelcomeViewModel model;

    @Inject
    public WelcomeView(WelcomeViewModel model) {
        this.model = model;

        createView();
    }

    private void createView() {
        ImageView icon = Controls.create(() -> new ImageView(IconCache.get(ICON_APP)), "icon");
        Text title = Controls.create(() -> new Text(Env.APP_NAME), "title");
        Text version = Controls.create(() -> new Text("v." + Env.getAppVersion()), "version");

        FontIcon githubIcon = Controls.fontIcon(FontAwesomeBrands.GITHUB);
        projectLink = Controls.create(() -> new Hyperlink(t(SYSTEM_PROJECT_PAGE)));
        HBox projectBox = Containers.create(HBox::new, "project");
        projectBox.setAlignment(Pos.BASELINE_CENTER);
        projectBox.getChildren().addAll(githubIcon, projectLink);

        getChildren().addAll(icon, title, version, projectBox);
        setAlignment(Pos.TOP_CENTER);
        setId("welcome");
    }

    @Override
    public void initialize() {
        projectLink.setOnAction(e -> DesktopUtils.browseQuietly(URI.create(Env.APP_PROJECT_PAGE)));
    }

    @Override
    public Region getRoot() { return this; }

    @Override
    public void reset() { }

    @Override
    public WelcomeViewModel getViewModel() { return model; }
}

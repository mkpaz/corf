package org.telekit.ui.main;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import fontawesomefx.fa.FontAwesomeIcon;
import fontawesomefx.fa.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.telekit.base.Env;
import org.telekit.base.EventBus;
import org.telekit.base.EventBus.Listener;
import org.telekit.base.domain.ProgressIndicatorEvent;
import org.telekit.base.i18n.BaseMessageKeys;
import org.telekit.base.i18n.Messages;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.internal.ExtensionBox;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.plugin.internal.PluginState;
import org.telekit.base.plugin.internal.PluginStateChangedEvent;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.preferences.Security;
import org.telekit.base.preferences.Vault;
import org.telekit.base.ui.*;
import org.telekit.base.util.CollectionUtils;
import org.telekit.base.util.DesktopUtils;
import org.telekit.controls.domain.Dimension;
import org.telekit.ui.Launcher;
import org.telekit.ui.domain.ApplicationEvent;
import org.telekit.ui.domain.BuiltinTool;
import org.telekit.ui.domain.CloseEvent;
import org.telekit.ui.domain.FXMLView;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.telekit.base.Env.DOCS_DIR;
import static org.telekit.base.ui.IconCache.ICON_APP;
import static org.telekit.base.util.CommonUtils.className;
import static org.telekit.base.util.Formatter.byteCountToDisplaySize;
import static org.telekit.ui.MessageKeys.*;

public class MainController extends Controller {

    private static final int VAULT_LOCKED = 0;
    private static final int VAULT_UNLOCKED = 1;
    private static final int VAULT_UNLOCK_FAILED = -1;

    public @FXML StackPane paneContent;
    public @FXML ScrollPane paneWelcome;
    public @FXML VBox paneWelcomeToolsMenu;
    public @FXML TabPane tabPaneTools;

    // welcome screen
    public @FXML Text welcomeAppName;
    public @FXML Text welcomeAppVersion;

    // menu bar
    public @FXML MenuBar menuBar;
    public @FXML Menu menuTools;
    public @FXML Menu menuPlugins;
    public @FXML CheckMenuItem cmAlwaysOnTop;

    // status bar
    public @FXML FontAwesomeIconView vaultStatusIcon;
    public @FXML ProgressBar pbarMemory;
    public @FXML Text txMemory;
    public @FXML HBox hboxProgressIndicator;
    public Timer memoryMonitoringTimer;

    private Stage primaryStage;
    private final Set<String> activeTasks = ConcurrentHashMap.newKeySet();
    private final ApplicationPreferences preferences;
    private final YAMLMapper yamlMapper;
    private final PluginManager pluginManager;
    private final Vault vault;
    private final SimpleIntegerProperty vaultState = new SimpleIntegerProperty(VAULT_LOCKED);

    @Inject
    public MainController(ApplicationPreferences preferences,
                          PluginManager pluginManager,
                          Vault vault,
                          YAMLMapper yamlMapper) {
        this.preferences = preferences;
        this.pluginManager = pluginManager;
        this.vault = vault;
        this.yamlMapper = yamlMapper;
    }

    @FXML
    public void initialize() {
        // subscriber to events
        EventBus.getInstance().subscribe(ProgressIndicatorEvent.class, this::toggleProgressIndicator);
        EventBus.getInstance().subscribe(ApplicationEvent.class, this::onApplicationEvent);
        EventBus.getInstance().subscribe(PluginStateChangedEvent.class, this::onPluginStateChangedEvent);

        // load menus
        reloadToolsMenu();
        reloadPluginsMenu();

        // setup and bring welcome screen to the front
        welcomeAppName.setText(Env.APP_NAME);
        welcomeAppVersion.setText(Messages.get(BaseMessageKeys.VERSION).toLowerCase() + " " + Env.getAppVersion());
        paneWelcome.toFront();

        // hide welcome screen if at least one tab opened
        tabPaneTools.getTabs().addListener((ListChangeListener<Tab>) tab -> {
            List<?> tabs = tabPaneTools.getTabs();
            if (CollectionUtils.isEmpty(tabs)) {
                paneWelcome.toFront();
            } else {
                tabPaneTools.toFront();
            }
        });

        // setup memory monitoring timer
        memoryMonitoringTimer = startMemoryUsageMonitoring();

        // change vault icon when vault state changed
        vaultState.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            vaultStatusIcon.getStyleClass().removeIf(style -> style.equals("error"));
            if (newValue.intValue() != VAULT_UNLOCKED) vaultStatusIcon.setIcon(FontAwesomeIcon.LOCK);
            if (newValue.intValue() == VAULT_UNLOCKED) vaultStatusIcon.setIcon(FontAwesomeIcon.UNLOCK);
            if (newValue.intValue() == VAULT_UNLOCK_FAILED) vaultStatusIcon.getStyleClass().add("error");
        });

        // unlock vault
        try {
            Security security = preferences.getSecurity();
            if (security.isAutoUnlock() && !vault.isUnlocked()) vault.unlock(security.getDerivedVaultPassword());
            if (vault.isUnlocked()) vaultState.set(VAULT_UNLOCKED); // also handles newly created vault
        } catch (Exception e) {
            vaultState.set(VAULT_UNLOCK_FAILED);
        }
    }

    private void reloadToolsMenu() {
        ToolsMenuHelper helper = ToolsMenuHelper.createForBuiltinTools();
        menuTools.getItems().setAll(helper.createTopMenu(this::openTool));
        paneWelcomeToolsMenu.getChildren().setAll(helper.createWelcomeMenu(this::openTool));
    }

    private void reloadPluginsMenu() {
        Collection<ExtensionBox> extensionBoxes = pluginManager.getExtensionsOfType(Tool.class);
        if (extensionBoxes.isEmpty()) {
            menuPlugins.setVisible(false);
            return;
        }
        ToolsMenuHelper helper = ToolsMenuHelper.createForExtensions(extensionBoxes);
        menuPlugins.getItems().setAll(helper.createTopMenu(this::openTool));
        menuPlugins.setVisible(true);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Timer startMemoryUsageMonitoring() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateMemoryUsage();
            }
        }, 0, TimeUnit.MINUTES.toMillis(1));
        return timer;
    }

    public void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double usedMemoryPercentage = ((double) usedMemory / (double) totalMemory) * 100;
        // a positive value between 0 and 1 indicates the percentage of progress where 0 is 0% and is 100%
        pbarMemory.setProgress(usedMemoryPercentage / 100);
        txMemory.setText(
                byteCountToDisplaySize(usedMemory, 1) + " / " + byteCountToDisplaySize(totalMemory, 1)
        );
    }

    @FXML
    public void openTool(ActionEvent event) {
        Object userData = null;
        if (event.getSource() == null) return;
        if (event.getSource() instanceof MenuItem) userData = ((MenuItem) event.getSource()).getUserData();
        if (event.getSource() instanceof Node) userData = ((Node) event.getSource()).getUserData();
        if (!(userData instanceof Tool)) return;

        Tool tool = (Tool) userData;
        Controller controller = Objects.requireNonNull(tool.createController());

        // when builtin tool or plugin is disabled, this is used to find and close all related tabs
        String tabUserData = null;
        if (userData instanceof BuiltinTool) {
            tabUserData = ((BuiltinTool) tool).name();
        } else {
            Optional<Class<? extends Plugin>> pluginClass = pluginManager.whatPluginProvides(tool.getClass());
            if (pluginClass.isPresent()) tabUserData = className(pluginClass.get());
        }

        if (!tool.isModal()) {
            openTab(tool.getName(), controller.getParent(), tabUserData);
        } else {
            openModal(tool.getName(), controller);
        }
    }

    private void openTab(String tabName, Parent parent, String tabUserData) {
        ObservableList<Tab> tabs = tabPaneTools.getTabs();
        tabName = Objects.requireNonNull(tabName).trim();

        // select appropriate tab if tool has been already opened
        for (int tabIndex = 0; tabIndex < tabs.size(); tabIndex++) {
            Tab tab = tabs.get(tabIndex);
            if (tabName.equals(tab.getText())) {
                tabPaneTools.getSelectionModel().select(tabIndex);
                return;
            }
        }

        Tab tab = new Tab(tabName);
        tab.setContent(parent);
        tab.setUserData(tabUserData);
        tabs.add(tab);
        tabPaneTools.getSelectionModel().selectLast();
    }

    private void openModal(String windowTitle, Controller controller) {
        Stage modalWindow = Dialogs.modal(controller.getParent())
                .owner(primaryStage, true)
                .title(windowTitle)
                .icon(IconCache.get(ICON_APP))
                .maxSize(Dimension.of(primaryStage).subtract(UIDefaults.WINDOW_DELTA))
                .resizable(false)
                .build();
        controller.setStage(modalWindow);
        modalWindow.showAndWait();
    }

    private void closeTabs(String tabUserDara) {
        final List<Tab> tabs = tabPaneTools.getTabs();
        if (tabUserDara == null || tabs.isEmpty()) return;

        // don't remove tabs in loop, it will cause ConcurrentModificationException
        List<Tab> tabsToRemove = tabs.stream()
                .filter(tab -> tab.getUserData() != null && tab.getUserData().equals(tabUserDara))
                .collect(Collectors.toList());

        // there is no good way to close tab in JavaFX, the downside of this one is that
        // tab.getOnClosed() won't be called, but it can be done manually
        Platform.runLater(() -> tabPaneTools.getTabs().removeAll(tabsToRemove));
    }

    @FXML
    public void quit() {
        primaryStage.close();
    }

    @FXML
    public void setOnTop() {
        primaryStage.setAlwaysOnTop(cmAlwaysOnTop.isSelected());
    }

    @FXML
    public void runGc() {
        System.gc();
        updateMemoryUsage();
    }

    @FXML
    public void showAboutDialog() {
        Controller controller = UILoader.load(FXMLView.ABOUT.getLocation(), Messages.getInstance());
        Dialogs.modal(controller.getParent())
                .owner(primaryStage, true)
                .title(Messages.get(MAIN_ABOUT))
                .icon(IconCache.get(ICON_APP))
                .resizable(false)
                .build()
                .showAndWait();
    }

    @FXML
    public void showPreferences() {
        Controller controller = UILoader.load(FXMLView.PREFERENCES.getLocation(), Messages.getInstance());
        Dialogs.modal(controller.getParent())
                .owner(primaryStage, true)
                .title(Messages.get(PREFERENCES))
                .icon(IconCache.get(ICON_APP))
                .resizable(false)
                .build()
                .showAndWait();
    }

    @FXML
    public void showPluginManager() {
        Controller controller = UILoader.load(FXMLView.PLUGIN_MANAGER.getLocation(), Messages.getInstance());
        Dialogs.modal(controller.getParent())
                .owner(primaryStage, true)
                .title(Messages.get(MAIN_PLUGIN_MANAGER))
                .icon(IconCache.get(ICON_APP))
                .resizable(false)
                .build()
                .showAndWait();
    }

    @FXML
    public void restartApplication() {
        CloseEvent event = new CloseEvent(Launcher.RESTART_EXIT_CODE);
        event.setWindowSize(UIDefaults.getWindowSize(primaryStage));
        EventBus.getInstance().publish(event);
    }

    @FXML
    public void showHelp() {
        Path docsPath = DOCS_DIR.resolve("ru/index.html");
        DesktopUtils.openQuietly(docsPath.toFile());
    }

    @FXML
    public void openDataDir() {
        DesktopUtils.openQuietly(Env.DATA_DIR.toFile());
    }

    @FXML
    public void openPluginsDir() {
        Path pluginsDir = Env.PLUGINS_DIR;
        if (Files.exists(pluginsDir)) {
            DesktopUtils.openQuietly(pluginsDir.toFile());
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    @Listener
    private synchronized void toggleProgressIndicator(ProgressIndicatorEvent event) {
        if (event.isActive()) {
            activeTasks.add(event.getId());
        } else {
            activeTasks.remove(event.getId());
        }
        hboxProgressIndicator.setVisible(!activeTasks.isEmpty());
    }

    @Listener
    private void onApplicationEvent(ApplicationEvent event) {
        switch (event.getType()) {
            case RESTART_REQUIRED -> primaryStage.setTitle(Env.APP_NAME + " (" + Messages.get(MAIN_RESTART_REQUIRED) + ")");
            case PREFERENCES_CHANGED -> {
                ApplicationPreferences.save(preferences, yamlMapper);
                preferences.resetDirty();
            }
        }
    }

    @Listener
    private void onPluginStateChangedEvent(PluginStateChangedEvent event) {
        if (event.getPluginState() == PluginState.STOPPED) {
            closeTabs(className(event.getPluginClass()));
        }
        reloadPluginsMenu();
    }
}

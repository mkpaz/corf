package org.telekit.ui.main;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.telekit.base.Env;
import org.telekit.base.EventBus;
import org.telekit.base.EventBus.Listener;
import org.telekit.base.ui.IconCache;
import org.telekit.base.ui.UILoader;
import org.telekit.base.domain.ProgressIndicatorEvent;
import org.telekit.base.ui.Controller;
import org.telekit.base.ui.Dialogs;
import org.telekit.base.i18n.Messages;
import org.telekit.base.plugin.Tool;
import org.telekit.base.plugin.internal.ExtensionBox;
import org.telekit.base.plugin.internal.PluginManager;
import org.telekit.base.plugin.internal.PluginState;
import org.telekit.base.plugin.internal.PluginStateChangedEvent;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.util.DesktopUtils;
import org.telekit.ui.Launcher;
import org.telekit.ui.domain.ApplicationEvent;
import org.telekit.ui.domain.CloseEvent;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.ui.IconCache.ICON_APP;
import static org.telekit.base.util.CommonUtils.className;
import static org.telekit.base.util.CommonUtils.objectClassName;
import static org.telekit.ui.main.MessageKeys.*;

public class MainController extends Controller {

    private static final int MB = 1024 * 1024;
    private static final int MINUTE = 1000 * 60;

    public @FXML TabPane tpaneTools;
    public Stage primaryStage;
    public Timer memoryMonitoringTimer;

    // menu bar
    public @FXML MenuBar menuBar;
    public @FXML Menu menuPlugins;
    public @FXML CheckMenuItem cmAlwaysOnTop;

    // status bar
    public @FXML ProgressBar pbarMemory;
    public @FXML Text txMemory;
    public @FXML HBox hboxProgressIndicator;

    public final Set<String> activeTasks = ConcurrentHashMap.newKeySet();
    private final ApplicationPreferences preferences;
    private final YAMLMapper yamlMapper;
    private final PluginManager pluginManager;

    @Inject
    public MainController(ApplicationPreferences preferences, PluginManager pluginManager, YAMLMapper yamlMapper) {
        this.preferences = preferences;
        this.pluginManager = pluginManager;
        this.yamlMapper = yamlMapper;
    }

    @FXML
    public void initialize() {
        EventBus.getInstance().subscribe(ProgressIndicatorEvent.class, this::toggleProgressIndicator);
        EventBus.getInstance().subscribe(ApplicationEvent.class, this::onApplicationEvent);
        EventBus.getInstance().subscribe(PluginStateChangedEvent.class, this::onPluginStateChangedEvent);

        reloadPluginsMenu();

        memoryMonitoringTimer = startMemoryUsageMonitoring();
    }

    private void reloadPluginsMenu() {
        Collection<ExtensionBox> extraTools = pluginManager.getExtensionsOfType(Tool.class);
        if (extraTools.isEmpty()) {
            menuPlugins.setVisible(false);
            return;
        }

        menuPlugins.setVisible(true);
        menuPlugins.getItems().clear();

        SortedMap<String, Menu> menuGroups = new TreeMap<>();
        for (ExtensionBox extension : extraTools) {
            Tool tool = (Tool) extension.getExtension();

            MenuItem menuItem = new MenuItem(tool.getName());
            menuItem.setUserData(extension);
            menuItem.setOnAction(this::openPlugin);

            String groupName = tool.getGroupName();
            if (isNotBlank(groupName)) {
                Menu groupItem = menuGroups.get(groupName);
                if (groupItem == null) {
                    groupItem = new Menu(groupName);
                    menuGroups.put(groupName, groupItem);
                    menuPlugins.getItems().add(groupItem);
                }
                groupItem.getItems().add(menuItem);
            } else {
                menuPlugins.getItems().add(menuItem);
            }
        }

        // sort menu items
        menuPlugins.getItems().sort(Comparator.comparing(MenuItem::getText));
        for (MenuItem menu : menuPlugins.getItems()) {
            if (menu instanceof Menu) {
                ((Menu) menu).getItems().sort(Comparator.comparing(MenuItem::getText));
            }
        }
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
        }, 0, MINUTE);
        return timer;
    }

    public void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double usedMemoryPercentage = ((usedMemory * 1.0) / totalMemory) * 100;
        pbarMemory.setProgress(usedMemoryPercentage / 100);
        txMemory.setText(usedMemory / MB + " MB / " + totalMemory / MB + "MB");
    }

    @FXML
    public void openTool(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        Views resource;
        String tabName;

        switch (source.getId()) {
            case "apiClient":
                resource = Views.API_CLIENT;
                tabName = Messages.get(TOOLS_APICLIENT);
                break;
            case "base64Encoder":
                resource = Views.BASE64_ENCODER;
                tabName = Messages.get(TOOLS_BASE64);
                break;
            case "importFileBuilder":
                resource = Views.IMPORT_FILE_BUILDER;
                tabName = Messages.get(TOOLS_FILEBUILD);
                break;
            case "ipCalculator":
                resource = Views.IP_V4_CALCULATOR;
                tabName = Messages.get(TOOLS_IPCALC);
                break;
            case "passwordGenerator":
                resource = Views.PASSWORD_GENERATOR;
                tabName = Messages.get(TOOLS_PASSGEN);
                break;
            case "sequenceGenerator":
                resource = Views.SEQUENCE_GENERATOR;
                tabName = Messages.get(TOOLS_SEQGEN);
                break;
            case "ss7CICTable":
                resource = Views.SS7_CIC_TABLE;
                tabName = Messages.get(TOOLS_CICTABLE);
                break;
            case "ss7SPCConverter":
                resource = Views.SS7_SPC_CONVERTER;
                tabName = Messages.get(TOOLS_SPCCONV);
                break;
            case "transliterator":
                resource = Views.TRANSLITERATOR;
                tabName = Messages.get(TOOLS_TRANSLIT);
                break;
            default:
                return;
        }

        Controller controller = UILoader.load(resource.getLocation(), Messages.getInstance());
        addTab(tabName, controller.getParent(), objectClassName(controller));
    }

    private void addTab(String tabName, Parent parent, String canonicalClassName) {
        ObservableList<Tab> tabs = tpaneTools.getTabs();

        // select appropriate tab if tool has been already opened
        for (int tabIndex = 0; tabIndex < tabs.size(); tabIndex++) {
            Tab tab = tabs.get(tabIndex);
            if (tabName.equals(trim(tab.getText()))) {
                tpaneTools.getSelectionModel().select(tabIndex);
                return;
            }
        }

        Tab tab = new Tab(tabName);
        tab.setContent(parent);
        // tab user data contains canonical either Controllers or Plugins class name
        // it's necessary to find and close tab if corresponding tool or plugin got disabled
        tab.setUserData(canonicalClassName);
        tabs.add(tab);
        tpaneTools.getSelectionModel().selectLast();

        updateMemoryUsage();
    }

    private void closeTabs(String canonicalClassName) {
        ObservableList<Tab> tabs = tpaneTools.getTabs();
        if (canonicalClassName == null || tabs.isEmpty()) return;

        List<Tab> tabsToRemove = new ArrayList<>();
        for (Tab tab : tabs) {
            if (tab.getUserData() != null && tab.getUserData().equals(canonicalClassName)) {
                tabsToRemove.add(tab);
            }
        }

        // there is no good way to close tab in JavaFX
        // the downside of this method is that  tab.getOnClosed() method won't be called,
        // but it can be done manually
        tabs.removeAll(tabsToRemove);
    }

    @FXML
    public void openPlugin(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        ExtensionBox extensionBox = (ExtensionBox) source.getUserData();
        Objects.requireNonNull(extensionBox);

        Tool tool = (Tool) extensionBox.getExtension();
        Objects.requireNonNull(tool);
        Controller controller = tool.createController();

        if (!tool.isModal()) {
            addTab(tool.getName(), controller.getParent(), className(extensionBox.getPluginClass()));
        } else {
            Stage modalWindow = Dialogs.modal(controller.getParent())
                    .owner(primaryStage, true)
                    .title(tool.getName())
                    .icon(IconCache.get(ICON_APP))
                    .resizable(false)
                    .build();
            controller.setStage(modalWindow);

            modalWindow.setMaxWidth(primaryStage.getWidth() - 50);
            modalWindow.setMaxHeight(primaryStage.getHeight() - 50);
            modalWindow.showAndWait();
        }
    }

    @FXML
    public void quit() {
        primaryStage.close();
    }

    @FXML
    public void showOnTop() {
        primaryStage.setAlwaysOnTop(cmAlwaysOnTop.isSelected());
    }

    @FXML
    public void runGc() {
        System.gc();
        updateMemoryUsage();
    }

    @FXML
    public void showAboutDialog() {
        Controller controller = UILoader.load(Views.ABOUT.getLocation(), Messages.getInstance());
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
        Controller controller = UILoader.load(Views.PREFERENCES.getLocation(), Messages.getInstance());
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
        Controller controller = UILoader.load(Views.PLUGIN_MANAGER.getLocation(), Messages.getInstance());
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
        EventBus.getInstance().publish(new CloseEvent(Launcher.RESTART_EXIT_CODE));
    }

    @FXML
    public void showHelp() {
        Path docPath = Env.DOCS_DIR.resolve("ru/index.html");
        DesktopUtils.openQuietly(docPath.toFile());
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
            case PREFERENCES_CHANGED -> ApplicationPreferences.save(preferences, yamlMapper, ApplicationPreferences.CONFIG_PATH);
        }
    }

    @Listener
    private void onPluginStateChangedEvent(PluginStateChangedEvent event) {
        if (event.getPluginState() == PluginState.STOPPED) {
            closeTabs(className(event.getPluginClass()));
        }
        reloadPluginsMenu();
    }

    @Override
    public void reset() {}
}

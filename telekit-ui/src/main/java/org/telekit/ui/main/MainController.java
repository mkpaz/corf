package org.telekit.ui.main;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.telekit.base.EventBus;
import org.telekit.base.EventBus.Listener;
import org.telekit.base.Settings;
import org.telekit.base.UILoader;
import org.telekit.base.internal.UserPreferences;
import org.telekit.base.domain.ProgressIndicatorEvent;
import org.telekit.base.fx.Controller;
import org.telekit.base.fx.Dialogs;
import org.telekit.base.plugin.Plugin;
import org.telekit.base.plugin.Tool;
import org.telekit.base.util.DesktopUtils;
import org.telekit.ui.Launcher;
import org.telekit.ui.domain.ApplicationEvent;
import org.telekit.ui.domain.CloseEvent;
import org.telekit.ui.domain.PluginContainer;
import org.telekit.ui.domain.PluginContainer.Status;
import org.telekit.ui.service.PluginManager;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.StringUtils.trim;
import static org.telekit.base.Settings.ICON_APP;
import static org.telekit.base.util.CommonUtils.canonicalName;

public class MainController extends Controller {

    private static final int MB = 1024 * 1024;
    private static final int MINUTE = 1000 * 60;
    private static final String TAB_LABEL_GAP = "  "; // because it's simpler than digging into overcomplicated TabPane CSS

    public @FXML TabPane tpaneTools;

    // menu bar
    public @FXML MenuBar menuBar;
    public @FXML Menu menuPlugins;
    public @FXML CheckMenuItem cmAlwaysOnTop;

    // status bar
    public @FXML ProgressBar pbarMemory;
    public @FXML Text txMemory;
    public @FXML HBox hboxProgressIndicator;

    public final Set<String> activeTasks = ConcurrentHashMap.newKeySet();
    public Stage primaryStage;
    public Timer memoryMonitoringTimer;

    private Settings settings;
    private XmlMapper xmlMapper;
    private PluginManager pluginManager;

    @Inject
    public MainController(Settings settings, PluginManager pluginManager, XmlMapper xmlMapper) {
        this.settings = settings;
        this.pluginManager = pluginManager;
        this.xmlMapper = xmlMapper;
    }

    @FXML
    public void initialize() {
        EventBus.getInstance().subscribe(ProgressIndicatorEvent.class, this::toggleProgressIndicator);
        EventBus.getInstance().subscribe(ApplicationEvent.class, this::onApplicationEvent);

        reloadPluginsMenu();

        memoryMonitoringTimer = startMemoryUsageMonitoring();
    }

    private void reloadPluginsMenu() {
        List<PluginContainer> loadedPlugins = pluginManager.getPlugins(EnumSet.of(Status.ENABLED));
        if (loadedPlugins.isEmpty()) {
            menuPlugins.setVisible(false);
            return;
        }

        menuPlugins.setVisible(true);
        menuPlugins.getItems().clear();

        for (PluginContainer container : loadedPlugins) {
            Plugin plugin = container.getPlugin();
            List<Tool> tools = new ArrayList<>(plugin.getTools());
            tools.sort(Comparator.comparing(Tool::getName));

            if (tools.size() == 1) {
                MenuItem item = new MenuItem(plugin.getMetadata().getName());
                item.setUserData(new PluginToolID(plugin.getClass(), tools.get(0).getName()));
                item.setOnAction(this::openPlugin);
                menuPlugins.getItems().add(item);
            }

            if (tools.size() > 1) {
                Menu menu = new Menu(plugin.getMetadata().getName());
                for (Tool tool : tools) {
                    MenuItem item = new MenuItem(tool.getName());
                    item.setUserData(new PluginToolID(plugin.getClass(), tool.getName()));
                    item.setOnAction(this::openPlugin);
                    menu.getItems().add(item);
                }
                menuPlugins.getItems().add(menu);
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
                tabName = "API Client";
                break;
            case "base64Encoder":
                resource = Views.BASE64_ENCODER;
                tabName = "Base64 Encoder";
                break;
            case "importFileBuilder":
                resource = Views.IMPORT_FILE_BUILDER;
                tabName = "Import File Builder";
                break;
            case "ipCalculator":
                resource = Views.IP_V4_CALCULATOR;
                tabName = "IP Calculator";
                break;
            case "passwordGenerator":
                resource = Views.PASSWORD_GENERATOR;
                tabName = "Password Generator";
                break;
            case "sequenceGenerator":
                resource = Views.SEQUENCE_GENERATOR;
                tabName = "Sequence Generator";
                break;
            case "ss7CICTable":
                resource = Views.SS7_CIC_TABLE;
                tabName = "SS7 / CIC Table";
                break;
            case "ss7SPCConverter":
                resource = Views.SS7_SPC_CONVERTER;
                tabName = "SS7 / SPC Converter";
                break;
            case "transliterator":
                resource = Views.TRANSLITERATOR;
                tabName = "Transliterator";
                break;
            default:
                return;
        }

        Controller controller = UILoader.load(resource.getLocation());
        addTab(tabName, controller.getParent(), canonicalName(controller));
    }

    private void addTab(String tabName, Parent parent, String userData) {
        ObservableList<Tab> tabs = tpaneTools.getTabs();

        // select appropriate tab if tool has been already opened
        for (int tabIndex = 0; tabIndex < tabs.size(); tabIndex++) {
            Tab tab = tabs.get(tabIndex);
            if (tabName.equals(trim(tab.getText()))) {
                tpaneTools.getSelectionModel().select(tabIndex);
                return;
            }
        }

        Tab tab = new Tab(TAB_LABEL_GAP + tabName + TAB_LABEL_GAP);
        tab.setContent(parent);
        tab.setUserData(userData);
        tabs.add(tab);
        tpaneTools.getSelectionModel().selectLast();

        updateMemoryUsage();
    }

    private void closeTabs(String userData) {
        ObservableList<Tab> tabs = tpaneTools.getTabs();
        if (userData == null || tabs.isEmpty()) return;

        List<Tab> tabsToRemove = new ArrayList<>();
        for (Tab tab : tabs) {
            if (tab.getUserData() != null && tab.getUserData().equals(userData)) {
                tabsToRemove.add(tab);
            }
        }

        // there're no good way to close tab in JavaFX
        // the downside of this method is that  tab.getOnClosed() method won't be called,
        // but still it can be done manually
        tabs.removeAll(tabsToRemove);
    }

    @FXML
    public void openPlugin(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        PluginToolID id = (PluginToolID) source.getUserData();
        Objects.requireNonNull(id);

        PluginContainer container = pluginManager.find(id.getPluginClass());
        Objects.requireNonNull(container);
        Plugin plugin = container.getPlugin();

        Tool tool = plugin.getTools().stream()
                .filter(elem -> elem.getName().equals(id.getToolName()))
                .findFirst()
                .orElse(null);
        Objects.requireNonNull(tool);

        Controller controller = tool.createController();

        if (!tool.isModal()) {
            addTab(tool.getName(), controller.getParent(), canonicalName(plugin));
        } else {
            Stage modalWindow = Dialogs.modal(controller.getParent())
                    .owner(primaryStage)
                    .title(tool.getName())
                    .icon(Settings.getIcon(ICON_APP))
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
        Controller controller = UILoader.load(Views.ABOUT.getLocation());
        Dialogs.modal(controller.getParent())
                .owner(primaryStage)
                .title("About")
                .icon(Settings.getIcon(ICON_APP))
                .resizable(false)
                .build()
                .showAndWait();
    }

    @FXML
    public void showPluginManager() {
        Controller controller = UILoader.load(Views.PLUGIN_MANAGER.getLocation());
        Dialogs.modal(controller.getParent())
                .owner(primaryStage)
                .title("Plugin Manager")
                .icon(Settings.getIcon(ICON_APP))
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
        Path docPath = Settings.DOCS_DIR.resolve("ru/index.html");
        DesktopUtils.openQuietly(docPath.toFile());
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
            case RESTART_REQUIRED:
                primaryStage.setTitle(Settings.APP_NAME + " (pending restart)");
                break;
            case PLUGINS_STATE_CHANGED:
                List<PluginContainer> plugins = pluginManager.getPlugins(status ->
                        EnumSet.of(Status.DISABLED, Status.UNINSTALLED).contains(status)
                );
                plugins.forEach(container -> closeTabs(canonicalName(container.getPlugin())));
                reloadPluginsMenu();
                break;
            case PREFERENCES_CHANGED:
                UserPreferences preferences = settings.getPreferences();
                UserPreferences.store(preferences, xmlMapper, UserPreferences.CONFIG_PATH);
                break;
        }
    }

    @Override
    public void reset() { /* not yet implemented */ }

    ///////////////////////////////////////////////////////////////////////////

    private static class PluginToolID {

        private final Class<? extends Plugin> pluginClass;
        private final String toolName;

        public PluginToolID(Class<? extends Plugin> pluginClass, String toolName) {
            this.pluginClass = pluginClass;
            this.toolName = toolName;
        }

        public Class<? extends Plugin> getPluginClass() {
            return pluginClass;
        }

        public String getToolName() {
            return toolName;
        }
    }
}

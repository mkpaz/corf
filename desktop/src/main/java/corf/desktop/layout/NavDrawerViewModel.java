package corf.desktop.layout;

import backbonefx.di.Initializable;
import backbonefx.mvvm.ViewModel;
import corf.base.desktop.ChangeList;
import corf.base.plugin.Tool;
import corf.base.plugin.internal.PluginManager;
import corf.base.plugin.internal.PluginStateEvent;
import corf.desktop.service.ToolRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

import static corf.base.plugin.internal.PluginState.STARTED;
import static corf.base.plugin.internal.PluginState.STOPPED;

@Singleton
public final class NavDrawerViewModel implements Initializable, ViewModel {

    private static final Comparator<Tool<?>> TOOL_COMPARATOR = Comparator.comparing(Tool::getName);
    private static final Predicate<Tool<?>> NO_FILTER = NavDrawerViewModel::noFilter;

    private final ToolRegistry toolRegistry;
    private final PluginManager pluginManager;

    @Inject
    public NavDrawerViewModel(ToolRegistry toolRegistry, PluginManager pluginManager) {
        this.toolRegistry = toolRegistry;
        this.pluginManager = pluginManager;
    }

    @Override
    public void init() {
        // register tools for all initially loaded (and started!) plugins
        pluginManager.getExtensionsOfType(Tool.class).stream()
                .map(box -> (Tool<?>) box.getExtension())
                .forEach(toolRegistry::register);

        navMenu.getSortedList().setComparator(TOOL_COMPARATOR);
        updateNavigationMenu();

        filter.addListener((obs, old, val) -> navMenu.getFilteredList().setPredicate(
                val != null ? tool -> isToolMatchesFilter(val, tool) : NO_FILTER
        ));

        // monitor plugins state to reflect changes on navigation menu
        pluginManager.addEventListener(this::onPluginStateChanged);
    }

    private void updateNavigationMenu() {
        navMenu.getItems().setAll(toolRegistry.getAll());
    }

    @SuppressWarnings("unchecked")
    private void onPluginStateChanged(PluginStateEvent event) {
        var pluginState = event.getPluginState();

        if (pluginState == STARTED) {
            pluginManager.getExtensionsOfType(Tool.class).stream()
                    .filter(box -> Objects.equals(box.getPluginClass(), event.getPluginClass()))
                    .map(box -> (Tool<?>) box.getExtension())
                    .forEach(toolRegistry::register);
            Platform.runLater(this::updateNavigationMenu);
        }

        if (pluginState == STOPPED) {
            pluginManager.find(event.getPluginClass()).ifPresent(
                    plugin -> plugin.getExtensionsOfType(Tool.class).forEach(c -> {
                        if (Tool.class.isAssignableFrom(c)) {
                            toolRegistry.unregister((Class<? extends Tool<?>>) c);
                        }
                    }));

            Platform.runLater(this::updateNavigationMenu);
        }
    }

    private boolean isToolMatchesFilter(@Nullable String filter, @Nullable Tool<?> tool) {
        if (filter == null || tool == null) { return true; }
        var s = filter.trim().toLowerCase();
        return s.isEmpty() || tool.getName().toLowerCase().contains(s);
    }

    private static boolean noFilter(Tool<?> t) {
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties                                                            //
    ///////////////////////////////////////////////////////////////////////////

    private final ChangeList<Tool<?>> navMenu = new ChangeList<>();

    public ObservableList<Tool<?>> navMenu() { return navMenu.getSortedList(); }

    private final StringProperty filter = new SimpleStringProperty();

    public StringProperty filterProperty() { return filter; }
}

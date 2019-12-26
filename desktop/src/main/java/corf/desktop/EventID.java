package corf.desktop;

public interface EventID {

    String APP_CLOSE_REQUEST = "app/close";
    String APP_RESTART_PENDING = "app/restart-pending";
    String APP_SHOW_NAVIGATION = "app/show-navigation";
    String APP_HIDE_NAVIGATION = "app/hide-navigation";

    String COMPLETION_UPDATE = "completion/update";

    String TOOL_OPEN_IN_CURRENT_TAB = "tool/open-in-current-tab";
    String TOOL_CREATE_NEW_TAB = "tool/create-new-tab";
    String TOOL_CLOSE_CURRENT_TAB = "tool/close-current-tab";

    String TEMPLATE_MANAGER_SHOW = "template-manager/show";
    String TEMPLATE_MANAGER_RELOAD = "template-manager/reload";
}

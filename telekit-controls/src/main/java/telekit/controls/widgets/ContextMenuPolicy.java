package telekit.controls.widgets;

public class ContextMenuPolicy {

    private final boolean hideInactiveItems;
    private final boolean showIcons;

    public ContextMenuPolicy() {
        this(false, true);
    }

    public ContextMenuPolicy(boolean hideInactiveItems, boolean showIcons) {
        this.hideInactiveItems = hideInactiveItems;
        this.showIcons = showIcons;
    }

    public boolean isHideInactiveItems() {
        return hideInactiveItems;
    }

    public boolean isShowIcons() {
        return showIcons;
    }

    @Override
    public String toString() {
        return "ContextMenuPolicy{" +
                "hideInactiveItems=" + hideInactiveItems +
                ", showIcons=" + showIcons +
                '}';
    }
}
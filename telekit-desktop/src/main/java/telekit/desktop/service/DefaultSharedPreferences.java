package telekit.desktop.service;

import org.jetbrains.annotations.Nullable;
import telekit.base.preferences.Proxy;
import telekit.base.preferences.SharedPreferences;
import telekit.base.preferences.Theme;
import telekit.base.preferences.internal.ApplicationPreferences;
import telekit.base.preferences.SystemPreferences;

public class DefaultSharedPreferences implements SharedPreferences {

    private final ApplicationPreferences preferences;

    public DefaultSharedPreferences(ApplicationPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public @Nullable Proxy getProxy() {
        return preferences.getProxyPreferences().getActiveProxy();
    }

    @Override
    public SystemPreferences getSystemPreferences() {
        return preferences.getSystemPreferences();
    }

    @Override
    public Theme getTheme() {
        return preferences.getTheme();
    }
}

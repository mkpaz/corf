package org.telekit.controls.demo;

import org.jetbrains.annotations.Nullable;
import org.telekit.base.preferences.Proxy;
import org.telekit.base.preferences.SharedPreferences;
import org.telekit.base.preferences.SystemPreferences;
import org.telekit.base.preferences.Theme;
import org.telekit.controls.theme.DefaultTheme;

import java.util.prefs.Preferences;

public class DemoSharedPreferences implements SharedPreferences {

    private static final Theme DEFAULT_THEME = new DefaultTheme();
    private static final Preferences USER_ROOT = Preferences.userRoot().node("TelekitDemo");

    private final SystemPreferences systemPreferences = new SystemPreferences(USER_ROOT);

    @Override
    public @Nullable Proxy getProxy() {
        return null;
    }

    @Override
    public SystemPreferences getSystemPreferences() {
        return systemPreferences;
    }

    @Override
    public Theme getTheme() {
        return DEFAULT_THEME;
    }
}

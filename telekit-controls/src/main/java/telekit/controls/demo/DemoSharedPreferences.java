package telekit.controls.demo;

import org.jetbrains.annotations.Nullable;
import telekit.base.preferences.Proxy;
import telekit.base.preferences.SharedPreferences;
import telekit.base.preferences.SystemPreferences;
import telekit.base.preferences.Theme;
import telekit.controls.theme.DefaultTheme;

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

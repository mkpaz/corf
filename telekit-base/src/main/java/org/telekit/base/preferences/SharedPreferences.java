package org.telekit.base.preferences;

import org.jetbrains.annotations.Nullable;

public interface SharedPreferences {

    @Nullable Proxy getProxy();

    SystemPreferences getSystemPreferences();

    Theme getTheme();
}

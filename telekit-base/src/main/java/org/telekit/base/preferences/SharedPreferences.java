package org.telekit.base.preferences;

import org.jetbrains.annotations.Nullable;
import org.telekit.base.preferences.internal.SystemPreferences;

public interface SharedPreferences {

    @Nullable Proxy getProxy();

    SystemPreferences getSystemPreferences();
}

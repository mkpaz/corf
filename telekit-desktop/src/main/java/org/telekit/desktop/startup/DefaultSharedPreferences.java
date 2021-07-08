package org.telekit.desktop.startup;

import org.jetbrains.annotations.Nullable;
import org.telekit.base.preferences.Proxy;
import org.telekit.base.preferences.SharedPreferences;
import org.telekit.base.preferences.internal.ApplicationPreferences;
import org.telekit.base.preferences.internal.SystemPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

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
}

package corf.desktop.service;

import org.jetbrains.annotations.Nullable;
import corf.base.preferences.Proxy;
import corf.base.preferences.SharedPreferences;
import corf.base.preferences.SystemPreferences;
import corf.base.preferences.internal.ApplicationPreferences;

import java.util.Objects;

public final class DefaultSharedPreferences implements SharedPreferences {

    private final ApplicationPreferences preferences;

    public DefaultSharedPreferences(ApplicationPreferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
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

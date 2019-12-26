package corf.base.preferences;

import org.jetbrains.annotations.Nullable;

/** Application preferences that allowed to be shared with plugins. */
public interface SharedPreferences {

    @Nullable Proxy getProxy();

    SystemPreferences getSystemPreferences();
}

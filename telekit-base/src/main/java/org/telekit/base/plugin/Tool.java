package org.telekit.base.plugin;

import org.telekit.base.fx.Controller;

public interface Tool {

    String getName();

    Controller createController();

    boolean isModal();
}

package org.telekit.base;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.telekit.base.TestUtils.loadResourceBundle;

public class BaseSetup implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        loadResourceBundle();
    }
}
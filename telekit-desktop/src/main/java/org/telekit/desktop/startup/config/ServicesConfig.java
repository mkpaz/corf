package org.telekit.desktop.startup.config;

import org.telekit.base.CompletionRegistry;

public final class ServicesConfig implements Config {

    private final CompletionRegistry completionRegistry;
    private final FileCompletionMonitoringService completionMonitoringService;

    public ServicesConfig() {
        completionRegistry = new CompletionRegistry();
        completionMonitoringService = new FileCompletionMonitoringService(completionRegistry);
    }

    public void startServices() {
        completionMonitoringService.registerAllProviders();
        completionMonitoringService.start();
    }

    public CompletionRegistry getCompletionRegistry() {
        return completionRegistry;
    }

    public FileCompletionMonitoringService getCompletionMonitoringService() {
        return completionMonitoringService;
    }
}

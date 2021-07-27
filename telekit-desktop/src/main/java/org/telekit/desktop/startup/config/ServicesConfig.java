package org.telekit.desktop.startup.config;

import org.telekit.base.service.completion.CompletionRegistry;
import org.telekit.desktop.service.DefaultCompletionRegistry;
import org.telekit.desktop.service.FileCompletionMonitoringService;

public final class ServicesConfig implements Config {

    private final CompletionRegistry completionRegistry;
    private final FileCompletionMonitoringService completionMonitoringService;

    public ServicesConfig() {
        completionRegistry = new DefaultCompletionRegistry();
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

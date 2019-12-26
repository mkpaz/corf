package corf.desktop.startup;

import corf.base.preferences.CompletionRegistry;
import corf.desktop.service.DefaultCompletionRegistry;
import corf.desktop.service.FileCompletionMonitoringService;

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

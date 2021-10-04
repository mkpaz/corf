package telekit.desktop.startup.config;

import telekit.base.service.completion.CompletionRegistry;
import telekit.desktop.service.DefaultCompletionRegistry;
import telekit.desktop.service.FileCompletionMonitoringService;

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

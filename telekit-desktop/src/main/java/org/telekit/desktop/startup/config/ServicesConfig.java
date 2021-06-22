package org.telekit.desktop.startup.config;

import org.telekit.base.CompletionRegistry;
import org.telekit.desktop.ExceptionHandler;

public final class ServicesConfig implements Config {

    private final ExceptionHandler exceptionHandler;
    private final CompletionRegistry completionRegistry;
    private final FileCompletionMonitoringService completionMonitoringService;

    public ServicesConfig(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;

        completionRegistry = new CompletionRegistry();
        completionMonitoringService = new FileCompletionMonitoringService(completionRegistry);
    }

    public void startServices() {
        completionMonitoringService.registerAllProviders();
        completionMonitoringService.start();
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public CompletionRegistry getCompletionRegistry() {
        return completionRegistry;
    }

    public FileCompletionMonitoringService getCompletionMonitoringService() {
        return completionMonitoringService;
    }
}

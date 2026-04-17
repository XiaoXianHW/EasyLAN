package org.xiaoxian.easylan.core.runtime;

import org.xiaoxian.easylan.core.model.EasyLanStatusSnapshot;
import org.xiaoxian.easylan.core.net.LocalHttpApiServer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class EasyLanRuntimeState {
    private final EasyLanStatusSnapshot statusSnapshot = new EasyLanStatusSnapshot();

    private ExecutorService executorService;
    private ScheduledExecutorService updateService;
    private LocalHttpApiServer httpApiServer;
    private boolean shared;
    private String lanPort;
    private Integer httpApiPort;

    public synchronized ExecutorService openExecutorService(int threads) {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(threads);
        }
        return executorService;
    }

    public synchronized ScheduledExecutorService openUpdateService() {
        if (updateService == null || updateService.isShutdown()) {
            updateService = Executors.newSingleThreadScheduledExecutor();
        }
        return updateService;
    }

    public synchronized Integer startHttpApi() throws IOException {
        if (httpApiServer == null) {
            httpApiServer = new LocalHttpApiServer(statusSnapshot);
        }
        httpApiPort = httpApiServer.start();
        return httpApiPort;
    }

    public synchronized void stopHttpApi() {
        if (httpApiServer != null) {
            httpApiServer.stop();
        }
        httpApiPort = null;
    }

    public synchronized void shutdownAll() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (updateService != null) {
            updateService.shutdownNow();
        }
        stopHttpApi();
        statusSnapshot.clear();
        shared = false;
        lanPort = null;
    }

    public EasyLanStatusSnapshot getStatusSnapshot() {
        return statusSnapshot;
    }

    public synchronized boolean isShared() {
        return shared;
    }

    public synchronized void setShared(boolean shared) {
        this.shared = shared;
    }

    public synchronized String getLanPort() {
        return lanPort;
    }

    public synchronized void setLanPort(String lanPort) {
        this.lanPort = lanPort;
    }

    public synchronized Integer getHttpApiPort() {
        return httpApiPort;
    }
}

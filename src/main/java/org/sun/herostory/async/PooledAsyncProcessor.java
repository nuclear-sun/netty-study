package org.sun.herostory.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.MainThreadProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class PooledAsyncProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PooledAsyncProcessor.class);

    private ExecutorService[] executorServices;

    private static final int MAX_THREADS = 8;

    private PooledAsyncProcessor() {
        executorServices = new ExecutorService[MAX_THREADS];
        for (int i = 0; i < executorServices.length; i++) {
            final int threadIndex = i;
            executorServices[i] = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("async-pool-" + threadIndex);
                    return thread;
                }
            });
        }
    }

    private static PooledAsyncProcessor instance = new PooledAsyncProcessor();

    public static PooledAsyncProcessor getInstance() {
        return instance;
    }

    public void process(IAsyncOperation operation) {

        // 如果这里抛出异常，将不再往下执行
        int bind = Math.abs(operation.bind());
        int index = bind % MAX_THREADS;

        executorServices[index].submit(() -> {
            try {
                operation.doAsync();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    MainThreadProcessor.getInstance().process(() -> {
                        operation.finish();
                    });
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }
}

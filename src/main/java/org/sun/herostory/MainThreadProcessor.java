package org.sun.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.handler.CmdHandlerFactory;
import org.sun.herostory.handler.ICmdHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class MainThreadProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MainThreadProcessor.class);

    private static final MainThreadProcessor instance = new MainThreadProcessor();

    public static MainThreadProcessor getInstance() {
        return instance;
    }

    private MainThreadProcessor() {}

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("MainThreadProcessor");
            return thread;
        }
    });

    public void process(ChannelHandlerContext context, GeneratedMessageV3 msg) {

        if(context == null || msg == null) {
            return;
        }

        executorService.submit(() -> {
            ICmdHandler handler = CmdHandlerFactory.create(msg.getClass());

            if(handler == null) {
                logger.info("未找到针对消息 {} 的处理器", msg.getClass().getName());
            } else {

                try {
                    handler.handle(context, cast(msg));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

        });
    }

    private static <TCmd extends GeneratedMessageV3> TCmd cast(Object msg) {
        if(msg == null || !(msg instanceof GeneratedMessageV3)) {
            return null;
        }
        return (TCmd)msg;
    }
}

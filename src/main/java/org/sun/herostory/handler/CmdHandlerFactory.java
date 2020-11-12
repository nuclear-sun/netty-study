package org.sun.herostory.handler;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.msg.GameMsgProtocol;
import org.sun.herostory.util.PackageUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CmdHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(CmdHandlerFactory.class);

    private CmdHandlerFactory() {}

    public static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> handlerMap = new HashMap<>();

    private static void init() {

        String packageName = CmdHandlerFactory.class.getPackage().getName();
        Set<Class<?>> classes = PackageUtil.listSubClazz(packageName, false, ICmdHandler.class);

        if(classes == null || classes.size() <= 0) {
            return;
        }

        for (Class<?> clazz : classes) {

            int modifiers = clazz.getModifiers();
            if((modifiers & Modifier.ABSTRACT) != 0) {
                continue;
            }

            try {

                Method[] declaredMethods = clazz.getDeclaredMethods();
                for (Method method : declaredMethods) {
                    String name = method.getName();
                    Class<?>[] parameterTypes = method.getParameterTypes();

                    if("handle".equals(name) &&
                            parameterTypes != null && parameterTypes.length == 2 &&
                            parameterTypes[0] == ChannelHandlerContext.class &&
                            GeneratedMessageV3.class.isAssignableFrom(parameterTypes[1])) {

                        Class<?> cmdClass = parameterTypes[1];
                        Object handler = clazz.newInstance();

                        handlerMap.put(cmdClass, (ICmdHandler)handler);
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    static {
        init();
    }

    /**
     * TODO 每次返回的是同一个 handler 实例，可能状态间干扰
     * @param msgClass
     * @return
     */
    public static ICmdHandler<? extends GeneratedMessageV3> create(Class<?> msgClass) {

        if(msgClass == null) {
            return null;
        }

        return handlerMap.get(msgClass);
    }

}

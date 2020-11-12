package org.sun.herostory.handler;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.util.PackageUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CmdHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(CmdHandlerFactory.class);

    private CmdHandlerFactory() {}

    public static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> handlerMap = new HashMap<>();

    private static void init() {

        String packageName = CmdHandlerFactory.class.getPackage().getName();
        Set<Class<?>> classes = PackageUtil.listSubClazz(packageName, true, ICmdHandler.class);

        if(classes == null || classes.size() <= 0) {
            return;
        }

        for (Class<?> clazz : classes) {

            int modifiers = clazz.getModifiers();
            if((modifiers & Modifier.ABSTRACT) != 0) {
                continue;
            }

            try {
                Class<?> msgClass = parseMsgClassFor(clazz);
                if(msgClass == null) {
                    continue;
                }
                Object handler = clazz.newInstance();
                handlerMap.put(msgClass, (ICmdHandler)handler);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 根据泛型信息找到 handler 和 msg 类之间的对应关系, 两种方法：
     * 1. 查询 ICmdHandler 泛型参数
     * 2. 查询 继承方法的 参数类型
     */

    // 根据接口的泛型参数获取类型
    private static Class<?> parseMsgClassFor(Class<?> handlerClass) {

        if(handlerClass == null) {
            return null;
        }

        if(!ICmdHandler.class.isAssignableFrom(handlerClass) || ICmdHandler.class == handlerClass) {
            return null;
        }


        Type[] genericInterfaces = handlerClass.getGenericInterfaces();

        if(genericInterfaces == null || genericInterfaces.length != 1) {
            return null;
        }

        Type[] actualTypeArguments = ((ParameterizedType) genericInterfaces[0]).getActualTypeArguments();

        if(actualTypeArguments == null || actualTypeArguments.length != 1) {
            return null;
        }

        return (Class)actualTypeArguments[0];
    }

    // 根据方法的参数类型获取
    private static Class<?> parseMsgClassFor2(Class<?> handlerClass) {

        if(handlerClass == null) {
            return null;
        }
        if(!ICmdHandler.class.isAssignableFrom(handlerClass) || ICmdHandler.class == handlerClass) {
            return null;
        }

        Method[] declaredMethods = handlerClass.getDeclaredMethods();
        if(declaredMethods == null || declaredMethods.length < 1) {
            return null;
        }

        for (Method method : declaredMethods) {
            String name = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();

            // 注意： 虽然子类中只定义了一个handle方法，但是，实际上会生成两个，另一个是泛型导致的，方法签名为：
            // public void handle(ChannelHandlerContext context, GeneratedMessageV3 msg);
            // 要避免获取的是这个方法
            if("handle".equals(name) &&
                    parameterTypes != null && parameterTypes.length == 2 &&
                    parameterTypes[0] == ChannelHandlerContext.class &&
                    parameterTypes[1] != GeneratedMessageV3.class &&
                    GeneratedMessageV3.class.isAssignableFrom(parameterTypes[1])) {

                Class<?> cmdClass = parameterTypes[1];
                return cmdClass;
            }
        }

        return null;
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

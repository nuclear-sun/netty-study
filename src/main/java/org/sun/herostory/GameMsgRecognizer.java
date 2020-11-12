package org.sun.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.msg.GameMsgProtocol;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class GameMsgRecognizer {

    private static final Logger logger = LoggerFactory.getLogger(GameMsgRecognizer.class);

    private GameMsgRecognizer() {}

    private static final Map<Integer, GeneratedMessageV3> codeToCmd;

    private static final Map<Class<?>, Integer> cmdToCode;

    static {

        GameMsgProtocol.MsgCode[] values = GameMsgProtocol.MsgCode.values();

        codeToCmd = new HashMap<>(values.length);
        cmdToCode = new HashMap<>(values.length);


        Map<String, Integer> lowerNameToCode = new HashMap<>(values.length);
        for (GameMsgProtocol.MsgCode msgCode : values) {
            if(msgCode == GameMsgProtocol.MsgCode.UNRECOGNIZED) {
                continue;
            }
            String lower = msgCode.name().replace("_", "").toLowerCase();
            lowerNameToCode.put(lower, msgCode.getNumber());
        }

        Class<?>[] declaredClasses = GameMsgProtocol.class.getDeclaredClasses();

        for (Class<?> declaredClass : declaredClasses) {
            if(GeneratedMessageV3.class.isAssignableFrom(declaredClass)) {
                String simpleName = declaredClass.getSimpleName();
                String lowerCase = simpleName.toLowerCase();
                Integer code = lowerNameToCode.get(lowerCase);

                cmdToCode.put(declaredClass, code);

                try {
                    Method method = declaredClass.getMethod("getDefaultInstance");
                    Object defaultMsg = method.invoke(null);
                    codeToCmd.put(code, (GeneratedMessageV3) defaultMsg);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }


    }

    public static Message.Builder getMsgBuilderByMsgCode(int msgCode) {
        if(msgCode < 0) {
            return null;
        }

        GeneratedMessageV3 defaultInstance = codeToCmd.get(msgCode);
        if(defaultInstance == null) {
            return null;
        }
        Message.Builder builder = defaultInstance.newBuilderForType();
        return builder;
    }

    public static Integer getMsgCodeByMsgClass(Class<?> msgClass) {

        if(msgClass == null || ! GeneratedMessageV3.class.isAssignableFrom(msgClass)) {
            return -1;
        }

        Integer code = cmdToCode.get(msgClass);
        if(code == null) {
            return -1;
        }
        return code;
    }


}

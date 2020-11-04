package org.sun.herostory.handler;

import com.google.protobuf.GeneratedMessageV3;
import org.sun.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

public final class CmdHandlerFactory {

    private CmdHandlerFactory() {}

    public static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> handlerMap;

    static {

        handlerMap = new HashMap<Class<?>, ICmdHandler<? extends GeneratedMessageV3>>();

        handlerMap.put(GameMsgProtocol.UserEntryCmd.class, new UserEntryCmdHandler());
        handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class, new WhoElseIsHereCmdHandler());
        handlerMap.put(GameMsgProtocol.UserMoveToCmd.class, new UserMoveToCmdHandler());
    }

    public static ICmdHandler<? extends GeneratedMessageV3> create(Class<?> msgClass) {

        if(msgClass == null) {
            return null;
        }

        return handlerMap.get(msgClass);
    }

}

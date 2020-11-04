package org.sun.herostory.handler;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;

public interface ICmdHandler<T extends GeneratedMessageV3> {

    void handle(ChannelHandlerContext context, T msg);
}

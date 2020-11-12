package org.sun.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {

    private final static Logger logger = LoggerFactory.getLogger(GameMsgEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if(msg == null || !(msg instanceof GeneratedMessageV3)) {
            super.write(ctx, msg, promise);
            return;
        }

        logger.info("服务端发送消息： {}", msg.toString());

        GeneratedMessageV3 cmd = (GeneratedMessageV3)msg;
        Integer code = GameMsgRecognizer.getMsgCodeByMsgClass(cmd.getClass());
        if(code == null || code.intValue() < 0) {
            logger.error("无法识别的消息类型：{}", cmd.getClass().getSimpleName());
            return;
        }

        byte[] body = cmd.toByteArray();
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeShort((short)body.length);
        buffer.writeShort((short)code.intValue());
        buffer.writeBytes(body);

        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);

        super.write(ctx, frame,promise);
    }
}

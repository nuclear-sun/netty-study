package org.sun.herostory;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMsgDecoder extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(GameMsgDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame)msg;
        ByteBuf content = frame.content();
        short length = content.readShort();
        short code = content.readShort();

        Message.Builder builder = GameMsgRecognizer.getMsgBuilderByMsgCode(code);
        if(builder == null) {
            logger.info("getMsgBuilderByMsgCode for {} failed.", code);
            return;
        }
        builder.clear();

        byte[] body = new byte[content.readableBytes()];
        content.readBytes(body);

        builder.mergeFrom(body);
        Message cmd = builder.build();

        if(cmd != null) {
            ctx.fireChannelRead(cmd);
        }
    }
}

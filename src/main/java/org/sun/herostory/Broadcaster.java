package org.sun.herostory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public final class Broadcaster {

    private final static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Broadcaster(){}

    public static void broadcast(Object msg) {
        if(msg != null) {
            channelGroup.writeAndFlush(msg);
        }
    }

    public static void addChannel(Channel channel) {
        if(channel != null) {
            channelGroup.add(channel);
        }
    }

    public static void removeChannel(Channel channel) {
        if(channel != null) {
            channelGroup.remove(channel);
        }
    }
}

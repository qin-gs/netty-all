package com.example.server.handler;

import com.example.message.GroupChatResponseMessage;
import com.example.message.GroupCreateRequestMessage;
import com.example.message.GroupCreateResponseMessage;
import com.example.server.session.Group;
import com.example.server.session.GroupSession;
import com.example.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        Set<String> members = msg.getMembers();

        // 建一个群
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);

        // 给群主发送是否创建成功消息
        if (group == null) {
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, "创建失败"));
            // 给群成员进行提示
            groupSession.getMembersChannel(groupName)
                    .forEach(channel -> channel.writeAndFlush(new GroupCreateResponseMessage(true, "您已被拉入 " + groupName + " 群聊")));
        } else {
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, "创建成功"));
        }
    }
}

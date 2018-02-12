package net.yuim.web.yutalker.push.service;

import net.yuim.web.yutalker.push.bean.api.base.ResponseModel;
import net.yuim.web.yutalker.push.bean.api.message.MessageCreateModel;
import net.yuim.web.yutalker.push.bean.card.MessageCard;
import net.yuim.web.yutalker.push.bean.card.UserCard;
import net.yuim.web.yutalker.push.bean.db.Group;
import net.yuim.web.yutalker.push.bean.db.Message;
import net.yuim.web.yutalker.push.bean.db.User;
import net.yuim.web.yutalker.push.factory.GroupFactory;
import net.yuim.web.yutalker.push.factory.MessageFactory;
import net.yuim.web.yutalker.push.factory.PushFactory;
import net.yuim.web.yutalker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息发送的入口
 */
@Path("/msg")
public class MessageService extends BaseService {
    // 发送一条消息到服务器
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<MessageCard> pushMessage(MessageCreateModel model) {
        if (!MessageCreateModel.check(model)) {
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        // 查询是否已经在数据库中有了
        Message message = MessageFactory.findById(model.getId());
        if (message != null) {
            // 正常返回
            return ResponseModel.buildOk(new MessageCard(message));
        }

        if (model.getReceiverType() == Message.RECEIVER_TYPE_GROUP) {
            return pushToGroup(self, model);
        } else {
            return pushToUser(self, model);
        }
    }

    // 发送到人
    private ResponseModel<MessageCard> pushToUser(User sender, MessageCreateModel model) {
        User receiver = UserFactory.findById(model.getReceiverId());
        if (receiver == null) {
            return ResponseModel.buildNotFoundUserError("发送失败，未找到接收者！");
        }
        if (receiver.getId().equalsIgnoreCase(sender.getId())) {
            return ResponseModel.buildCreateError(ResponseModel.ERROR_CREATE_MESSAGE);
        }
        // 存储数据库
        Message message = MessageFactory.add(sender, receiver, model);
        // 走推送的逻辑
        return buildAndPushResponse(sender, message);
    }

    // 发送到群
    private ResponseModel<MessageCard> pushToGroup(User sender, MessageCreateModel model) {
        // 找到群，必须判断我在不在这个群中
        Group group = GroupFactory.findById(sender, model.getReceiverId());
        if (group == null) {
            return ResponseModel.buildNotFoundUserError("发送失败，未找到接收群！");
        }
        // 存储数据库
        Message message = MessageFactory.add(sender, group, model);
        // 走推送的逻辑
        return buildAndPushResponse(sender, message);
    }

    // 推送并构建一个返回信息
    private ResponseModel<MessageCard> buildAndPushResponse(User sender, Message message) {
        if (message == null) {
            return ResponseModel.buildCreateError(ResponseModel.ERROR_CREATE_MESSAGE);
        }

        // 进行推送
        PushFactory.pushNewMessage(sender, message);
        // 返回
        return ResponseModel.buildOk(new MessageCard(message));
    }
}
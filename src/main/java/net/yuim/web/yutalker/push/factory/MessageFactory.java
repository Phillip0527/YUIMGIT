package net.yuim.web.yutalker.push.factory;

import net.yuim.web.yutalker.push.bean.api.message.MessageCreateModel;
import net.yuim.web.yutalker.push.bean.db.Group;
import net.yuim.web.yutalker.push.bean.db.Message;
import net.yuim.web.yutalker.push.bean.db.User;
import net.yuim.web.yutalker.push.utils.Hib;
import org.hibernate.Session;

/**
 * 消息数据存储的类
 */
public class MessageFactory {
    /**
     * 根据id查询消息
     *
     * @param id 消息id
     * @return 消息
     */
    public static Message findById(String id) {
        // 普通写法回调接口
//        Hib.query(new Hib.Query<Message>() {
//            @Override
//            public Message query(Session session) {
//                return session.get(Message.class, id);
//            }
//        });
        return Hib.query(session -> session.get(Message.class, id));
    }

    /**
     * 添加一条普通消息
     *
     * @param sender   发送者
     * @param receiver 接收者
     * @param model    消息创建卡片
     * @return 消息
     */
    public static Message add(User sender, User receiver, MessageCreateModel model) {
        Message message = new Message(sender, receiver, model);
        return save(message);
    }

    /**
     * 添加一条群消息
     *
     * @param sender 发送者
     * @param group  群
     * @param model  消息创建卡片
     * @return 消息
     */
    public static Message add(User sender, Group group, MessageCreateModel model) {
        Message message = new Message(sender, group, model);
        return save(message);
    }

    private static Message save(Message message) {
        return Hib.query(session -> {
            session.save(message);
            // 写入到数据库
            session.flush();
            // 紧接着从数据库中查询出来
            session.refresh(message);
            return message;
        });
    }
}
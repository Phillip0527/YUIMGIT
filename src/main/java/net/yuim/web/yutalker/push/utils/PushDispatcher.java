package net.yuim.web.yutalker.push.utils;

import com.gexin.rp.sdk.base.IBatch;
import com.gexin.rp.sdk.base.IIGtPush;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.LinkTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.google.common.base.Strings;
import net.yuim.web.yutalker.push.bean.api.base.PushModel;
import net.yuim.web.yutalker.push.bean.db.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 消息推送工具类
 */
public class PushDispatcher {
    private final static String appId = "w2xQKuYE6g7SoIwWN6E4d1";
    private final static String appKey = "hj4ToQzAsXANUMcYBPXIL5";
    private final static String masterSecret = "OCW2cBEKE9A7TQWcLew9d3";
    private final static String host = "http://sdk.open.api.igexin.com/apiex.htm";

    private final IIGtPush pusher;

    // 要收到消息的人和内容的列表
    private final List<BatchBean> beans = new ArrayList<>();

    public PushDispatcher() {
        // 最根本的发送者
        pusher = new IGtPush(host, appKey, masterSecret);
    }

    /**
     * 添加一条消息
     * @param receiver 接收者
     * @param model 接收的推送model
     * @return 是否添加成功
     */
    public boolean add(User receiver, PushModel model) {
        // 基础检查，必须有接收者、设备Id
        if (receiver == null || Strings.isNullOrEmpty(receiver.getPushId()) || model == null)
            return false;
        String pushString = model.getPushString();
        if (Strings.isNullOrEmpty(pushString))
            return false;

        BatchBean bean = buildMessages(receiver.getPushId(), pushString);
        beans.add(bean);
        return true;
    }

    /**
     * 对要发送的数据进行封装
     * @param pushId 接收者的设备Id
     * @param pushString 要接收的数据
     * @return BatchBean
     */
    private BatchBean buildMessages(String pushId, String pushString) {
        // 透传消息，不是通知栏显示，而是在MessageReceiver收到
        TransmissionTemplate template = new TransmissionTemplate();
        template.setAppId(appId);
        template.setAppkey(appKey);
        template.setTransmissionContent(pushString);
        template.setTransmissionType(0); // 这个Type为int型，填写1则自动启动app
        // 构建消息
        SingleMessage message = new SingleMessage();
        message.setData(template);// 把透传消息设置到单消息模版中
        message.setOffline(true);// 是否允许离线发送
        message.setOfflineExpireTime(24 * 3600 * 1000);// 离线消息时长
        // 设置推送目标，填入appid和clientId
        Target target = new Target();
        target.setAppId(appId);
        target.setClientId(pushId);
        //返回一个封装类
        return new BatchBean(message, target);
    }

    // 进行消息最终发送
    public boolean submit() {
        // 构建打包的工具类
        IBatch batch = pusher.getBatch();

        // 是否有数据需要发送
        boolean haveData = false;

        for (BatchBean bean : beans) {
            try {
                batch.add(bean.message, bean.target);
                haveData = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 没有数据就直接返回
        if (!haveData)
            return false;

        IPushResult result = null;
        try {
            result = batch.submit();
        } catch (IOException e) {
            e.printStackTrace();

            // 失败情况下尝试重复发送一次
            try {
                batch.retry();
            } catch (IOException e1) {
                e.printStackTrace();
            }
        }

        if (result != null) {
            try {
                Logger.getLogger("PushDispatcher").log(Level.INFO, (String) result.getResponse().get("result"));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Logger.getLogger("PushDispatcher").log(Level.WARNING, "推送服务器响应异常！");
        return false;
    }



    // 给每个人发送消息的一个Bean封装
    private static class BatchBean {
        SingleMessage message;
        Target target;

        public BatchBean(SingleMessage message, Target target) {
            this.message = message;
            this.target = target;
        }
    }

}
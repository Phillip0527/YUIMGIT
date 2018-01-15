package net.yuim.web.yutalker.push.service;

import com.google.common.base.Strings;
import net.yuim.web.yutalker.push.bean.api.base.ResponseModel;
import net.yuim.web.yutalker.push.bean.api.user.UpdateInfoModel;
import net.yuim.web.yutalker.push.bean.card.UserCard;
import net.yuim.web.yutalker.push.bean.db.User;
import net.yuim.web.yutalker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 用户信息处理的service
 */
// 127.0.0.1/api/user/...
@Path("/user")
public class UserService extends BaseService {

    @PUT
//    @Path("") 不需要写 就是当前目录
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(UpdateInfoModel model) {
        if (!UpdateInfoModel.check(model)) {
            return ResponseModel.buildParameterError();
        }
        // 拿到自己的信息
        User self = getSelf();
        // 更新用户信息
        self = model.updateToUser(self);
        self = UserFactory.update(self);
        // 构架自己的个人信息
        UserCard userCard = new UserCard(self, true);
        // 返回
        return ResponseModel.buildOk(userCard);
    }

}
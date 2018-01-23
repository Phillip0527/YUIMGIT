package net.yuim.web.yutalker.push.service;

import com.google.common.base.Strings;
import net.yuim.web.yutalker.push.bean.api.base.ResponseModel;
import net.yuim.web.yutalker.push.bean.api.user.UpdateInfoModel;
import net.yuim.web.yutalker.push.bean.card.UserCard;
import net.yuim.web.yutalker.push.bean.db.User;
import net.yuim.web.yutalker.push.bean.db.UserFollow;
import net.yuim.web.yutalker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息处理的service
 */
// 127.0.0.1/api/user/...
@Path("/user")
public class UserService extends BaseService {

    // 更新
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

    // 获取联系人
    @GET
    @Path("/contact")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> contact() {
        User self = getSelf();
        // 拿到我的联系人
        List<User> users = UserFactory.contacts(self);
        // 转换为UserCard
        List<UserCard> userCards = users.stream()
                .map(user -> new UserCard(user, true))// map相当于转置操作 User->UserCard
                .collect(Collectors.toList());
        // 返回
        return ResponseModel.buildOk(userCards);
    }

    // 关注人
    @PUT
    @Path("/follow/{followId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> follow(@PathParam("followId") String followId) {
        User self = getSelf();
        // 自己不能关注自己
        if (self.getId().equalsIgnoreCase(followId) || Strings.isNullOrEmpty(followId)) {
            // 返回参数异常
            return ResponseModel.buildParameterError();
        }
        // 获取我要关注的人
        User followUser = UserFactory.findById(followId);
        if (followUser == null) {
            // 未找到user
            return ResponseModel.buildNotFoundUserError(null);
        }

        // 备注默认没有，后期可扩展
        followUser = UserFactory.follow(self, followUser, null);
        if (followUser == null) {
            // 关注失败返回服务器异常
            return ResponseModel.buildServiceError();
        }

        // TODO 通知我关注的人，我关注了他

        // 返回关注人的信息
        return ResponseModel.buildOk(new UserCard(followUser, true));
    }

    // 获取某人信息
    @GET
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> getUser(@PathParam("id") String id) {
        if (Strings.isNullOrEmpty(id)) {
            // 返回参数异常
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        if (self.getId().equalsIgnoreCase(id)) {
            // 返回自己
            return ResponseModel.buildOk(new UserCard(self, true));
        }

        User user = UserFactory.findById(id);
        if (user == null) {
            // 没找到，返回没找到用户
            return ResponseModel.buildNotFoundUserError(null);
        }

        // 如果关注记录有，则我已关注
        boolean isFollow = UserFactory.getUserFollow(self, user) != null;
        return ResponseModel.buildOk(new UserCard(user, isFollow));
    }

    // 获取搜索人的实现
    // 分页只返回20条数据
    @GET
    @Path("/search/{name:(.*)?}") //正则：名字为任意字符，可以为空
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> contact(@DefaultValue("") @PathParam("name") String name) {
        User self = getSelf();
        // 先查询数据
        List<User> searchUsers = UserFactory.search(name);
        // 把查询的人封装为UserCard
        // 判断这些人是否有我已经关注的
        // 如果有则返回关注状态中应该已经设置好的状态

        //拿出我的联系人
        final List<User> contacts = UserFactory.contacts(self);

        // 把User转换为UserCard
        List<UserCard> userCards = searchUsers.stream()
                .map(user -> {
                    // 判断这个用户是否是我自己，或者是否在我的联系人中
                    boolean isFollow = user.getId().equalsIgnoreCase(self.getId())
                            // 进行联系人的任意匹配
                            || contacts.stream().anyMatch(
                            contactUser -> contactUser.getId()
                                    .equalsIgnoreCase(user.getId()));
                    return new UserCard(user, isFollow);
                }).collect(Collectors.toList());
        // 返回
        return ResponseModel.buildOk(userCards);
    }

}
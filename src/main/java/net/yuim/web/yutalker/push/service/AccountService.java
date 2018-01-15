package net.yuim.web.yutalker.push.service;

import com.google.common.base.Strings;
import net.yuim.web.yutalker.push.bean.api.account.LoginModel;
import net.yuim.web.yutalker.push.bean.api.account.RegisterModel;
import net.yuim.web.yutalker.push.bean.api.base.AccountRspModel;
import net.yuim.web.yutalker.push.bean.api.base.ResponseModel;
import net.yuim.web.yutalker.push.bean.db.User;
import net.yuim.web.yutalker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 账户处理
 * Created by Phillip on 2017/12/23.
 */
// 127.0.0.1/api/account/...
@Path("/account")
public class AccountService extends BaseService{

    @POST
    @Path("/login")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> login(LoginModel model) {
        if (!LoginModel.check(model)) {
            return ResponseModel.buildParameterError();
        }

        User user = UserFactory.login(model.getAccount(), model.getPassword());
        if (user != null) {
            // 如果有携带pushId
            if (!Strings.isNullOrEmpty(model.getPushId())) {
                return bind(user, model.getPushId());
            }

            // 返回当前登录账户
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        } else {
            // 登录失败
            return ResponseModel.buildLoginError();
        }

    }


    @POST
    @Path("/register")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> register(RegisterModel model) {
        if (!RegisterModel.check(model)) {
            return ResponseModel.buildParameterError();
        }

        User user = UserFactory.findByPhone(model.getAccount().trim());
        if (user != null) {
            // 已有账户
            return ResponseModel.buildHaveAccountError();
        }

        user = UserFactory.findByName(model.getName().trim());
        if (user != null) {
            // 已有用户名
            return ResponseModel.buildHaveNameError();
        }

        // 开始注册的逻辑
        user = UserFactory.register(model.getAccount(), model.getPassword(), model.getName());

        if (user != null) {
            // 如果有携带pushId
            if (!Strings.isNullOrEmpty(model.getPushId())) {
                return bind(user, model.getPushId());
            }
            // 返回当前的账户
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        } else {
            // 注册异常
            return ResponseModel.buildRegisterError();
        }

    }

    // 绑定设备
    @POST
    @Path("/bind/{pushId}")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // 从请求头中获取token字段
    public ResponseModel<AccountRspModel> bind(@HeaderParam("token") String token, @PathParam("pushId") String pushId) {
        if (Strings.isNullOrEmpty(token) || Strings.isNullOrEmpty(pushId)) {
            // 返回参数异常
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        // 进行设备id绑定的操作
        return bind(self, pushId);

    }


    /**
     * 绑定的操作
     *
     * @param self   自己
     * @param pushId 设备id
     * @return User
     */
    private ResponseModel<AccountRspModel> bind(User self, String pushId) {
        // 进行设备id绑定的操作
        User user = UserFactory.bindPushId(self, pushId);
        if (user == null) {
            // 绑定失败，服务器异常
            return ResponseModel.buildServiceError();
        }
        // 返回当前登录账户,并且绑定成功
        AccountRspModel rspModel = new AccountRspModel(user, true);
        return ResponseModel.buildOk(rspModel);
    }


}

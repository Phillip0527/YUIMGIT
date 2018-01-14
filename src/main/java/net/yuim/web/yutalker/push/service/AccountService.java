package net.yuim.web.yutalker.push.service;

import net.yuim.web.yutalker.push.bean.api.account.RegisterModel;
import net.yuim.web.yutalker.push.bean.api.base.AccountRspModel;
import net.yuim.web.yutalker.push.bean.api.base.ResponseModel;
import net.yuim.web.yutalker.push.bean.card.UserCard;
import net.yuim.web.yutalker.push.bean.db.User;
import net.yuim.web.yutalker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Phillip on 2017/12/23.
 */
// 127.0.0.1/api/account/...
@Path("/account")
public class AccountService {

    @POST
    @Path("/register")
    //指定请求与返回的相应体为JSON
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> register(RegisterModel model) {

        User user = UserFactory.findByPhone(model.getAccount().trim());
        if(user!=null){
            // 已有账户
            return ResponseModel.buildHaveAccountError();
        }

        user = UserFactory.findByName(model.getName().trim());
        if(user!=null){
            // 已有用户名
            return ResponseModel.buildHaveNameError();
        }

        // 开始注册的逻辑
        user = UserFactory.register(model.getAccount(), model.getPassword(), model.getName());

        if (user != null) {
            // 返回当前的账户
            AccountRspModel rspModel=new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        }else{
            // 注册异常
            return ResponseModel.buildRegisterError();
        }

    }
}

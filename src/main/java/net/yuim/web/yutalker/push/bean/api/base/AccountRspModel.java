package net.yuim.web.yutalker.push.bean.api.base;

import com.google.gson.annotations.Expose;
import net.yuim.web.yutalker.push.bean.card.UserCard;
import net.yuim.web.yutalker.push.bean.db.User;

public class AccountRspModel {

    // 用户基本信息
    @Expose
    private UserCard user;

    // 当前登陆的帐号
    @Expose
    private String account;

    // 当前登录成功后获取的Token
    // 可以通过Token获取用户的所有信息
    @Expose
    private String token;

    // 表示是否已经绑定到了设备PushId
    @Expose
    private boolean isBind;

    public AccountRspModel(User user) {
        // 默认无绑定
        this(user, false);
    }

    public AccountRspModel(User user, Boolean isBind) {
        this.user = new UserCard(user);
        this.account = user.getPhone();
        this.token = user.getToken();
        this.isBind = isBind;
    }

    public UserCard getUser() {
        return user;
    }

    public void setUser(UserCard user) {
        this.user = user;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }
}
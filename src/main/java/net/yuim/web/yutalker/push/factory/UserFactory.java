package net.yuim.web.yutalker.push.factory;

import com.google.common.base.Strings;
import net.yuim.web.yutalker.push.bean.db.User;
import net.yuim.web.yutalker.push.bean.db.UserFollow;
import net.yuim.web.yutalker.push.utils.Hib;
import net.yuim.web.yutalker.push.utils.TextUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserFactory {

    /**
     * 根据手机查找用户
     *
     * @param phone phone
     * @return User
     */
    public static User findByPhone(String phone) {
        // 普通写法回调接口
//        return Hib.query(new Hib.Query<User>() {
//            @Override
//            public User query(Session session) {
//                return (User) session
//                        .createQuery("from User where phone=:inPhone")
//                        .setParameter("inPhone", phone)
//                        .uniqueResult();
//            }
//        });

        // lambda表达式写法回调接口
        return Hib.query(session -> (User) session
                .createQuery("from User where phone=:inPhone")
                .setParameter("inPhone", phone)
                .uniqueResult());
    }

    /**
     * 根据用户名查找用户
     *
     * @param name name
     * @return User
     */
    public static User findByName(String name) {
        return Hib.query(session -> (User) session
                .createQuery("from User where name=:name")
                .setParameter("name", name)
                .uniqueResult());
    }

    /**
     * 根据id查找用户
     *
     * @param id id
     * @return User
     */
    public static User findById(String id) {
        // 通过id查询用户
        return Hib.query(session -> session.get(User.class, id));
    }

    /**
     * 根据Token查找用户
     * 只能自己使用，查询的是个人信息，非他人信息
     *
     * @param token token
     * @return User
     */
    public static User findByToken(String token) {
        return Hib.query(session -> (User) session
                .createQuery("from User where token=:token")
                .setParameter("token", token)
                .uniqueResult());
    }


    /**
     * 更新用户信息到数据库操作
     *
     * @param user User
     * @return User
     */
    public static User update(User user) {
        return Hib.query(session -> {
            session.saveOrUpdate(user);
            return user;
        });
    }

    /**
     * 登录
     *
     * @param account
     * @param password
     * @return
     */
    public static User login(String account, String password) {
        final String accountStr = account.trim();
        final String passwordStr = encodePassword(password);
        User user = Hib.query(session -> (User) session
                .createQuery("from User where phone=:account and password=:password")
                .setParameter("account", accountStr)
                .setParameter("password", passwordStr)
                .uniqueResult());
        if (user != null) {
            // 对User进行登录操作，更新Token
            user = login(user);
        }
        return user;
    }

    /**
     * 给当前账户绑定PushId
     *
     * @param user   自己的user
     * @param pushId 自己设备的pushId
     * @return
     */
    public static User bindPushId(User user, String pushId) {
        if (Strings.isNullOrEmpty(pushId))
            return null;
        // 第一步查询是否有其他账户绑定了这个设备
        // 取消绑定 避免推送混乱
        // 查询列表不能包括自己
        Hib.queryOnly(session -> {
            @SuppressWarnings("unchecked")
            List<User> userList = (List<User>) session.createQuery("from User where lower(pushId) =:pushId and id!=:userId")
                    .setParameter("pushId", pushId.toLowerCase())
                    .setParameter("userId", user.getId())
                    .list();
            for (User u : userList) {
                u.setPushId(null);
                session.saveOrUpdate(u);
            }
        });

        if (pushId.equalsIgnoreCase(user.getPushId())) {
            // 如果当前需要绑定的设备id之前已经绑定过了
            // 直接返回用户，不需要再次绑定
            return user;
        } else {
            // 如果当前账户之前的设备id和需要绑定的不同
            // 那么需要点击登录，让之前的设备退出登录
            // 给之前的设备推送一条退出的消息
            if (Strings.isNullOrEmpty(user.getPushId())) {
                // TODO 推送一条退出的消息
            }
            // 更新新的设备id
            user.setPushId(pushId);
            return update(user);

        }
    }

    /**
     * 用户注册
     * 注册的操作需要写入数据库，并返回数据库中的User信息
     *
     * @param account  账户
     * @param password 密码
     * @param name     用户名
     * @return User
     */
    public static User register(String account, String password, String name) {

        // 去除账户首尾的空格
        account = account.trim();
        // 处理密码
        password = encodePassword(password);

        User user = createUser(account, password, name);

        if (user != null) {
            login(user);
        }
        return user;
    }

    /**
     * 注册里面的 新建用户逻辑
     *
     * @param account  帐号(用手机号)
     * @param password 密码
     * @param name     用户名
     * @return 用户
     */
    private static User createUser(String account, String password, String name) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        // 账户就是手机号
        user.setPhone(account);

        // 普通接口回调写法
//        return Hib.query(new Hib.Query<User>() {
//            @Override
//            public User query(Session session) {
//                session.save(user);
//                return user;
//            }
//        });

        // lambda表达式写法
        // 数据库中存储用户
        return Hib.query(session -> {
            session.save(user);
            return user;
        });

    }

    /**
     * 进行登录操作
     * 就是对token进行操作
     *
     * @param user
     * @return User
     */
    private static User login(User user) {
        // 使用随机UUID值充当Token
        String newToken = UUID.randomUUID().toString();
        // 进行一次base64加密
        newToken = TextUtil.encodeBase64(newToken);
        user.setToken(newToken);

        // 进行更新保存 saveOrUpdate 没有返回值 所以自己返回user实体
        return update(user);
    }


    /**
     * 处理密码，加密密码
     *
     * @param password 密码
     * @return 加密后的字符串
     */
    private static String encodePassword(String password) {
        // 密码去除首尾空格
        password = password.trim();
        // 进行MD5非对称加密，加盐会更安全（盐也需要存储）
        password = TextUtil.getMD5(password);
        // 再进行一次对称的Base64加密，当然可以采取加盐的方案(加盐就是密码字符串加上一个随机的时间然后一起进行MD5非对称加密)
        password = TextUtil.encodeBase64(password);
        return password;
    }

    /**
     * 获取我的联系人的列表
     *
     * @param self User
     * @return List<User>
     */
    public static List<User> contacts(User self) {
        return Hib.query(session -> {
            // 重新加载一次用户信息到self中，和当前session绑定
            session.load(self, self.getId());
            // 获取我关注的人
            Set<UserFollow> flows = self.getFollowing();
            // java8 简写方式
            return flows
                    .stream()
                    .map(UserFollow::getTarget)
                    .collect(Collectors.toList());
        });
    }

    /**
     * 进行关注的操作
     *
     * @param origin 发起者
     * @param target 被关注的人
     * @param alias  备注名称
     * @return 被关注的人信息
     */
    public static User follow(final User origin, final User target, final String alias) {
        UserFollow follow = getUserFollow(origin, target);
        if (follow != null) {
            return follow.getTarget();
        }
        return Hib.query(session -> {
            session.load(origin, origin.getId());
            session.load(target, target.getId());

            // 我关注他的时候同时他也关注我所以两个UserFollow
            UserFollow originFollow = new UserFollow();
            originFollow.setOrigin(origin);
            originFollow.setTarget(target);
            // 备注是我对他的备注
            originFollow.setAlias(alias);

            UserFollow targetFollow = new UserFollow();
            targetFollow.setOrigin(target);
            targetFollow.setTarget(origin);

            session.save(originFollow);
            session.save(targetFollow);

            return target;
        });
    }

    /**
     * 查询关注状态
     *
     * @param origin 发起者
     * @param target 被关注的人
     * @return 关注的状态类 UserFollow
     */
    public static UserFollow getUserFollow(final User origin, final User target) {
        return Hib.query(session -> (UserFollow) session
                .createQuery("from UserFollow where originId=:originId and targetId=:targetId")
                .setParameter("originId", origin.getId())
                .setParameter("targetId", target.getId())
                .setMaxResults(1)
                .uniqueResult());
    }

    /**
     * 搜索联系人的实现
     *
     * @param name 名字
     * @return List<User> 查询结果集合, 如果name为空则返回最近的用户
     */
    @SuppressWarnings("unchecked")
    public static List<User> search(String name) {
        if (Strings.isNullOrEmpty(name))
            name = ""; // 保证name不能为null
        final String searchName = "%" + name + "%";
        return Hib.query(session -> {
            // 查询条件：name不区分大小写，name模糊查询，头像和描述不能为空
            return (List<User>) session.createQuery("from User where lower(name) like :name and portrait is not null and description is not null")
                    .setParameter("name", searchName)
                    .setMaxResults(20)// 最多20条
                    .list();
        });
    }
}
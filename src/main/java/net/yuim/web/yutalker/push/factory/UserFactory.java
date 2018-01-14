package net.yuim.web.yutalker.push.factory;

import net.yuim.web.yutalker.push.bean.db.User;
import net.yuim.web.yutalker.push.utils.Hib;
import net.yuim.web.yutalker.push.utils.TextUtil;
import org.hibernate.Session;

public class UserFactory {

    public static User findByPhone(String phone) {
        return Hib.query(session -> (User) session
                .createQuery("from User where phone=:inPhone")
                .setParameter("inPhone", phone)
                .uniqueResult());
    }

    public static User findByName(String name) {
        return Hib.query(session -> (User) session
                .createQuery("from User where name=:name")
                .setParameter("name", name)
                .uniqueResult());
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

        User user = new User();
        user.setName(name);
        user.setPassword(password);
        // 账户就是手机号
        user.setPhone(account);

        // 进行数据库操作
        // 创建会话
        Session session = Hib.session();
        // 开启事务
        session.beginTransaction();
        try {
            // 保存操作
            session.save(user);
            // 提交事务
            session.getTransaction().commit();
            return user;
        } catch (Exception e) {
            // 失败回滚事务
            session.getTransaction().rollback();
            return null;
        }

    }


    /**
     * 处理密码，加密密码
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
}
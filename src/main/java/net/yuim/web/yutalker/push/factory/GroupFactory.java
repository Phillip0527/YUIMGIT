package net.yuim.web.yutalker.push.factory;

import net.yuim.web.yutalker.push.bean.db.Group;
import net.yuim.web.yutalker.push.bean.db.GroupMember;
import net.yuim.web.yutalker.push.bean.db.User;

import java.util.Set;

/**
 * 群数据库处理
 */
public class GroupFactory {
    public static Group findById(String groupId) {
        // TODO 查询一个群
        return null;
    }

    public static Group findById(User sender, String groupId) {
        // TODO 查询一个群，同时sender必须为该群的成员，否则返回null
        return null;
    }

    public static Set<GroupMember> getMembers(Group group) {
        // TODO 查找一个群的成员
        return null;
    }
}
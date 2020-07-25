package com.cn.hzm.server.meta;

/**
 * Created by yuyang04 on 2020/7/25.
 */
public final class HzmPermission {

    public interface PermissionType {
        String USER_MANAGER = "user_manager";
    }

    public interface UserManager {
        /**
         * 查看用户列表
         */
        String LIST_USER = "list_user";

        /**
         * 编辑用户
         */
        String EDIT_USER = "edit_user";

        /**
         * 删除用户
         */
        String DEL_USER = "del_user";
    }
}

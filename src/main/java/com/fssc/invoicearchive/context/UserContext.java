package com.fssc.invoicearchive.context;

import com.fssc.invoicearchive.entity.RoleType;
import com.fssc.invoicearchive.entity.SysUser;

import java.util.Set;

public class UserContext {

    private static final ThreadLocal<SysUser> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(SysUser user) {
        CURRENT_USER.set(user);
    }

    public static SysUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static Long getCurrentUserId() {
        SysUser user = CURRENT_USER.get();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUserName() {
        SysUser user = CURRENT_USER.get();
        return user != null ? user.getRealName() : null;
    }

    public static String getCurrentUserDept() {
        SysUser user = CURRENT_USER.get();
        return user != null ? user.getDepartment() : null;
    }

    public static Set<RoleType> getCurrentUserRoles() {
        SysUser user = CURRENT_USER.get();
        return user != null ? user.getRoles() : null;
    }

    public static boolean hasRole(RoleType roleType) {
        Set<RoleType> roles = getCurrentUserRoles();
        return roles != null && roles.contains(roleType);
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}

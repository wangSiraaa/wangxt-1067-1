package com.fssc.invoicearchive.service;

import com.fssc.invoicearchive.common.BusinessException;
import com.fssc.invoicearchive.context.UserContext;
import com.fssc.invoicearchive.entity.RoleType;
import com.fssc.invoicearchive.entity.SysUser;
import com.fssc.invoicearchive.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private SysUserRepository sysUserRepository;

    public SysUser login(String username, String password) {
        SysUser user = sysUserRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!user.getEnabled()) {
            throw new BusinessException("用户已被禁用");
        }

        if (!user.getPassword().equals(password)) {
            throw new BusinessException("密码错误");
        }

        UserContext.setCurrentUser(user);
        return user;
    }

    public SysUser getCurrentUser() {
        return UserContext.getCurrentUser();
    }

    public List<SysUser> getAllUsers() {
        return sysUserRepository.findAll();
    }

    public SysUser getUserById(Long id) {
        return sysUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    public SysUser createUser(SysUser user) {
        if (sysUserRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        return sysUserRepository.save(user);
    }

    public void checkRole(RoleType roleType) {
        if (!UserContext.hasRole(roleType)) {
            throw new BusinessException("权限不足，需要" + roleType.name() + "角色");
        }
    }

    public boolean canViewDepartment(String department) {
        Set<RoleType> roles = UserContext.getCurrentUserRoles();
        if (roles == null) {
            return false;
        }
        if (roles.contains(RoleType.AUDITOR) || roles.contains(RoleType.ARCHIVIST)) {
            return true;
        }
        String userDept = UserContext.getCurrentUserDept();
        return department != null && department.equals(userDept);
    }

    public void initDefaultUsers() {
        if (sysUserRepository.count() > 0) {
            return;
        }

        SysUser accountant = new SysUser();
        accountant.setUsername("accountant");
        accountant.setPassword("123456");
        accountant.setRealName("张会计");
        accountant.setDepartment("财务部");
        Set<RoleType> accountantRoles = new HashSet<>();
        accountantRoles.add(RoleType.ACCOUNTANT);
        accountant.setRoles(accountantRoles);
        sysUserRepository.save(accountant);

        SysUser handler = new SysUser();
        handler.setUsername("handler");
        handler.setPassword("123456");
        handler.setRealName("李经办");
        handler.setDepartment("销售部");
        Set<RoleType> handlerRoles = new HashSet<>();
        handlerRoles.add(RoleType.DEPT_HANDLER);
        handler.setRoles(handlerRoles);
        sysUserRepository.save(handler);

        SysUser archivist = new SysUser();
        archivist.setUsername("archivist");
        archivist.setPassword("123456");
        archivist.setRealName("王档案员");
        archivist.setDepartment("档案部");
        Set<RoleType> archivistRoles = new HashSet<>();
        archivistRoles.add(RoleType.ARCHIVIST);
        archivist.setRoles(archivistRoles);
        sysUserRepository.save(archivist);

        SysUser auditor = new SysUser();
        auditor.setUsername("auditor");
        auditor.setPassword("123456");
        auditor.setRealName("赵审计");
        auditor.setDepartment("审计部");
        Set<RoleType> auditorRoles = new HashSet<>();
        auditorRoles.add(RoleType.AUDITOR);
        auditor.setRoles(auditorRoles);
        sysUserRepository.save(auditor);
    }
}

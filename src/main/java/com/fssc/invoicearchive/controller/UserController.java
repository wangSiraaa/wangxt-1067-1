package com.fssc.invoicearchive.controller;

import com.fssc.invoicearchive.common.Result;
import com.fssc.invoicearchive.config.AuthInterceptor;
import com.fssc.invoicearchive.entity.SysUser;
import com.fssc.invoicearchive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        SysUser user = userService.login(username, password);
        user.setPassword(null);

        String token = AuthInterceptor.generateToken();
        AuthInterceptor.TOKEN_STORE.put(token, user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);

        return Result.success("登录成功", result);
    }

    @GetMapping("/current")
    public Result<SysUser> getCurrentUser() {
        SysUser user = userService.getCurrentUser();
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<List<SysUser>> getAllUsers() {
        List<SysUser> users = userService.getAllUsers();
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = userService.getUserById(id);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("退出成功");
    }
}

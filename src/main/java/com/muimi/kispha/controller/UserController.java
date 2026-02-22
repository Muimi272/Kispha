package com.muimi.kispha.controller;

import com.muimi.kispha.entity.User;
import com.muimi.kispha.service.UserService;
import com.muimi.kispha.utils.StatusCodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        logger.info("【用户注册接口】接收用户注册请求，请求参数：用户名={}，邮箱={}",
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null);
        User newUser = userService.register(user);
        if (newUser != null) {
            logger.info("【用户注册接口】注册请求处理成功，返回用户uid：{}", newUser.getUid());
            return ResponseEntity.ok(newUser);
        }
        logger.warn("【用户注册接口】注册请求处理失败，返回400错误");
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        logger.info("【用户登录接口】接收用户登录请求，请求参数：uid={}",
                user != null ? user.getUid() : null);
        User loginUser = userService.login(user);
        if (loginUser != null) {
            logger.info("【用户登录接口】登录请求处理成功，返回用户uid：{}", loginUser.getUid());
            return ResponseEntity.ok(loginUser);
        }
        logger.warn("【用户登录接口】登录请求处理失败，返回400错误");
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        logger.info("【用户更新接口】接收用户更新请求，请求参数：uid={}",
                user != null ? user.getUid() : null);
        User updatedUser = userService.updateUser(user);
        if (updatedUser != null) {
            logger.info("【用户更新接口】更新请求处理成功，返回用户uid：{}", updatedUser.getUid());
            return ResponseEntity.ok(updatedUser);
        }
        logger.warn("【用户更新接口】更新请求处理失败，返回400错误");
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/work")
    public ResponseEntity<String> work(@RequestBody User user) {
        logger.info("【用户工作接口】接收用户工作请求，请求参数：uid={}，状态码={}",
                user != null ? user.getUid() : null,
                user != null ? user.getStatusCode() : null);

        // 1. 空值保护
        if (user == null || user.getUid() == null || !StringUtils.hasText(user.getStatusCode())) {
            logger.warn("【用户工作接口】请求处理失败，uid为空或状态码为空，返回400错误");
            return ResponseEntity.badRequest().build();
        }
        // 2. 检查用户是否存在
        User existingUser = userService.findByUid(user.getUid());
        if (existingUser == null) {
            logger.warn("【用户工作接口】请求处理失败，用户不存在，uid={}，返回400错误", user.getUid());
            return ResponseEntity.badRequest().build();
        }
        // 3. 校验状态码是否匹配（先做表面匹配）
        if (!user.getStatusCode().equals(existingUser.getStatusCode())) {
            logger.warn("【用户工作接口】请求处理失败，传入状态码与数据库不一致，uid={}，传入状态码={}，数据库状态码={}",
                    user.getUid(), user.getStatusCode(), existingUser.getStatusCode());
            return ResponseEntity.badRequest().build();
        }

        try {
            // 核心修改：调用你封装的checkStatusCode方法
            String newStatusCode = StatusCodeManager.checkStatusCode(user.getStatusCode(), user.getUid());
            // 验证失败（返回空字符串）则返回400
            if (!StringUtils.hasText(newStatusCode)) {
                logger.warn("【用户工作接口】请求处理失败，状态码验证失败（过期/uid不匹配/解析失败），uid={}，状态码={}",
                        user.getUid(), user.getStatusCode());
                return ResponseEntity.badRequest().build();
            }

            // 4. 更新状态码并保存（必须调用save，否则数据不会持久化）
            existingUser.setStatusCode(newStatusCode);
            userService.updateUser(existingUser); // 复用updateUser方法保存状态码
            logger.info("【用户工作接口】请求处理成功，uid={}，生成新状态码={}，返回200：worked",
                    user.getUid(), newStatusCode);
            return ResponseEntity.ok("worked");
        } catch (Exception e) {
            logger.error("【用户工作接口】请求处理异常，uid={}，状态码={}，异常信息：{}",
                    user.getUid(), user.getStatusCode(), e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/delete/{uid}")
    public ResponseEntity<String> deleteUser(@PathVariable Long uid, @RequestBody User admin) {
        logger.info("【删除用户接口】接收删除用户请求，待删除uid={}，操作管理员uid={}",
                uid, admin != null ? admin.getUid() : null);
        if (userService.deleteByUid(uid, admin)) {
            logger.info("【删除用户接口】删除请求处理成功，待删除uid={}，返回200：deleted", uid);
            return ResponseEntity.ok("deleted");
        }
        logger.warn("【删除用户接口】删除请求处理失败，待删除uid={}，返回400错误", uid);
        return ResponseEntity.badRequest().build();
    }
}
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

/**
 * 用户管理控制层
 *
 * 此类处理所有与用户相关的 HTTP 请求，包括用户注册、登录、信息更新和删除等操作。
 * 所有端点都返回 HTTP 200 状态码（成功）或 400 状态码（失败）。
 *
 * 端点说明：
 * - POST /users/register       : 用户注册
 * - POST /users/login          : 用户登录
 * - POST /users/update         : 更新用户信息
 * - POST /users/work           : 用户工作状态处理（状态码验证）
 * - POST /users/delete/{uid}   : 删除用户（管理员权限）
 *
 * @author Muimi272
 * @version 1.0
 * @since 2026-02-22
 */
@RestController
@RequestMapping("/users")
public class UserController {

    // ...existing code...

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * 构造方法
     *
     * 通过构造注入方式获取 UserService 的实例，用于处理业务逻辑。
     *
     * @param userService 用户业务逻辑服务实例
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册接口
     *
     * HTTP POST 端点：/users/register
     *
     * 处理用户注册请求。接收包含用户信息的 JSON 对象，调用业务层进行注册处理。
     *
     * 请求格式：
     * {
     *     "username": "用户名",
     *     "email": "邮箱",
     *     "password": "密码",
     *     "role": "user"
     * }
     *
     * 响应说明：
     * - 200 OK: 注册成功，返回完整的用户对象（包含生成的 uid 和 statusCode）
     * - 400 Bad Request: 注册失败（参数校验失败、用户名/邮箱重复等）
     *
     * @param user 包含用户注册信息的 JSON 对象
     * @return 成功返回 HTTP 200 及用户对象，失败返回 HTTP 400
     */
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

    /**
     * 用户登录接口
     *
     * HTTP POST 端点：/users/login
     *
     * 处理用户登录请求。验证用户身份（uid 和密码），登录成功后为用户生成新的状态码。
     *
     * 请求格式：
     * {
     *     "uid": 用户ID,
     *     "password": "密码"
     * }
     *
     * 响应说明：
     * - 200 OK: 登录成功，返回用户对象（包含新生成的 statusCode）
     * - 400 Bad Request: 登录失败（用户不存在、密码错误等）
     *
     * @param user 包含用户 uid 和密码的 JSON 对象
     * @return 成功返回 HTTP 200 及用户对象，失败返回 HTTP 400
     */
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

    /**
     * 更新用户信息接口
     *
     * HTTP POST 端点：/users/update
     *
     * 处理用户信息更新请求。更新用户的用户名、邮箱等信息。
     * 更新时需验证用户身份（密码校验）和 statusCode 有效性。
     *
     * 请求格式：
     * {
     *     "uid": 用户ID,
     *     "username": "新用户名",
     *     "email": "新邮箱",
     *     "password": "密码",
     *     "role": "user",
     *     "statusCode": "当前状态码"
     * }
     *
     * 响应说明：
     * - 200 OK: 更新成功，返回更新后的用户对象（包含新的 statusCode）
     * - 400 Bad Request: 更新失败（身份验证失败、状态码过期等）
     *
     * @param user 包含用户更新信息的 JSON 对象
     * @return 成功返回 HTTP 200 及用户对象，失败返回 HTTP 400
     */
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

    /**
     * 用户工作状态处理接口
     *
     * HTTP POST 端点：/users/work
     *
     * 处理用户的工作请求，主要用于状态码的验证和更新。这是一个核心接口，用来：
     * 1. 验证用户提供的 statusCode 是否合法（uid 匹配、未过期）
     * 2. 为用户生成新的 statusCode
     * 3. 返回新的 statusCode 供下次请求使用
     *
     * 这个接口适用于所有需要状态码验证的操作，如定时任务、长连接等。
     *
     * 请求格式：
     * {
     *     "uid": 用户ID,
     *     "statusCode": "当前状态码"
     * }
     *
     * 响应说明：
     * - 200 OK: 处理成功，返回字符串 "worked"
     * - 400 Bad Request: 处理失败（状态码过期、uid 不匹配、用户不存在等）
     *
     * 处理流程：
     * 1. 验证请求参数（uid 和 statusCode 不为空）
     * 2. 查询用户是否存在
     * 3. 验证传入的 statusCode 与数据库中的 statusCode 一致
     * 4. 调用 StatusCodeManager.checkStatusCode 验证 statusCode（uid 匹配、未过期）
     * 5. 生成新的 statusCode
     * 6. 保存新的 statusCode 并返回成功
     *
     * @param user 包含用户 uid 和当前 statusCode 的 JSON 对象
     * @return 成功返回 HTTP 200 及字符串 "worked"，失败返回 HTTP 400
     */
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
            // 4. 验证 statusCode 的有效性（uid 匹配、未过期）
            String newStatusCode = StatusCodeManager.checkStatusCode(user.getStatusCode(), user.getUid());
            // 验证失败（返回空字符串）则返回400
            if (!StringUtils.hasText(newStatusCode)) {
                logger.warn("【用户工作接口】请求处理失败，状态码验证失败（过期/uid不匹配/解析失败），uid={}，状态码={}",
                        user.getUid(), user.getStatusCode());
                return ResponseEntity.badRequest().build();
            }

            // 5. 更新状态码并保存
            existingUser.setStatusCode(newStatusCode);
            userService.updateUser(existingUser);
            logger.info("【用户工作接口】请求处理成功，uid={}，生成新状态码={}，返回200：worked",
                    user.getUid(), newStatusCode);
            return ResponseEntity.ok("worked");
        } catch (Exception e) {
            logger.error("【用户工作接口】请求处理异常，uid={}，状态码={}，异常信息：{}",
                    user.getUid(), user.getStatusCode(), e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除用户接口
     *
     * HTTP POST 端点：/users/delete/{uid}
     *
     * 处理删除用户的请求。只有拥有 "admin" 角色的管理员才可以删除其他用户。
     * 删除前需验证管理员的身份（uid 和密码）。
     *
     * 请求路径参数：
     * - uid: 待删除的用户 ID
     *
     * 请求体格式：
     * {
     *     "uid": 管理员用户ID,
     *     "password": "管理员密码"
     * }
     *
     * 响应说明：
     * - 200 OK: 删除成功，返回字符串 "deleted"
     * - 400 Bad Request: 删除失败（权限不足、管理员验证失败等）
     *
     * 删除流程：
     * 1. 验证待删除 uid 和管理员参数
     * 2. 查询管理员是否存在
     * 3. 验证管理员拥有 "admin" 角色
     * 4. 验证管理员密码正确
     * 5. 验证待删除用户存在
     * 6. 执行删除操作
     *
     * @param uid 待删除的用户 ID（路径参数）
     * @param admin 执行删除操作的管理员用户对象，包含 uid 和密码（请求体）
     * @return 成功返回 HTTP 200 及字符串 "deleted"，失败返回 HTTP 400
     */
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
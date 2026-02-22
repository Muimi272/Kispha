package com.muimi.kispha.service;

import com.muimi.kispha.entity.User;

import java.util.List;

/**
 * 用户业务逻辑层接口
 *
 * 此接口定义了用户相关的业务操作，包括查询、注册、登录、更新和删除用户等核心功能。
 * 具体实现由 UserServiceImpl 类提供。
 *
 * @author Muimi272
 * @version 1.0
 * @since 2026-02-22
 */
public interface UserService {

    /**
     * 查询所有用户
     *
     * @return 系统中所有用户的列表
     */
    List<User> findAll();

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param uid 用户唯一标识
     * @return 对应的用户对象，如果不存在则返回 null
     */
    User findByUid(Long uid);

    /**
     * 根据用户名模糊查询用户
     *
     * 查询所有用户名包含指定关键词的用户。
     *
     * @param username 用户名关键词
     * @return 符合条件的用户列表
     */
    List<User> findByUsernameContaining(String username);

    /**
     * 用户注册
     *
     * 创建新用户并分配唯一的 uid 和加密状态码。
     * 注册时会验证用户名和邮箱的唯一性，检查用户角色是否合法（必须为 "user"），
     * 并确保状态码为空。
     *
     * @param user 待注册的用户对象
     * @return 注册成功返回包含生成的 uid 和 statusCode 的用户对象，失败返回 null
     */
    User register(User user);

    /**
     * 更新用户信息
     *
     * 更新用户的用户名、邮箱和角色信息。更新前会验证用户身份（密码校验）
     * 和状态码有效性。
     *
     * @param user 包含待更新信息的用户对象（必须包含有效的 uid 和 statusCode）
     * @return 更新成功返回更新后的用户对象，失败返回 null
     */
    User updateUser(User user);

    /**
     * 用户登录
     *
     * 验证用户的身份（uid 和密码），登录成功后为用户生成新的状态码。
     *
     * @param user 包含用户 uid 和密码的用户对象
     * @return 登录成功返回包含新 statusCode 的用户对象，失败返回 null
     */
    User login(User user);

    /**
     * 删除用户
     *
     * 只有拥有 "admin" 角色的管理员才可以删除用户。删除前需验证管理员身份和密码。
     *
     * @param uid 待删除的用户 ID
     * @param admin 执行删除操作的管理员用户对象（包含 uid 和密码）
     * @return 删除成功返回 true，失败返回 false
     */
    boolean deleteByUid(Long uid, User admin);
}

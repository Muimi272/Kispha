package com.muimi.kispha.repository;

import com.muimi.kispha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户数据访问层（DAO）
 *
 * 此接口提供用户数据的持久化操作，继承自 Spring Data JPA 的 JpaRepository，
 * 自动提供基础的 CRUD 操作。同时定义了针对用户的自定义查询方法。
 *
 * @author Muimi272
 * @version 1.0
 * @since 2026-02-22
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户 ID 查询用户
     *
     * @param uid 用户唯一标识
     * @return 如果找到用户则返回该用户对象，否则返回 null
     */
    User findByUid(Long uid);

    /**
     * 根据用户名精确查询用户
     *
     * @param username 用户名
     * @return 如果找到用户则返回该用户对象，否则返回 null
     */
    User findByUsername(String username);

    /**
     * 根据邮箱精确查询用户
     *
     * @param email 用户邮箱
     * @return 如果找到用户则返回该用户对象，否则返回 null
     */
    User findByEmail(String email);

    /**
     * 根据用户名模糊查询用户
     *
     * 查询所有用户名包含指定关键词的用户。
     *
     * @param username 用户名关键词
     * @return 符合条件的用户列表，如果没有则返回空列表
     */
    List<User> findByUsernameContaining(String username);
}

package com.muimi.kispha.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;

/**
 * 用户实体类
 *
 * 此类代表系统中的用户对象，映射到数据库中的 user 表。
 * 每个用户拥有唯一的 uid、username 和 email，通过 role 区分身份（user/admin）。
 * statusCode 字段用于存储用户的加密状态码，用于会话管理和身份验证。
 *
 * @author Muimi272
 * @version 1.0
 * @since 2026-02-22
 */
@Setter
@Getter
@ToString
@Entity
public class User {

    /**
     * 用户唯一标识（主键）
     *
     * 由数据库自��生成的自增主键，用于唯一标识每个用户。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    /**
     * 用户名
     *
     * 用户的登录名称，在系统中必须唯一且不能为空。
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 用户邮箱
     *
     * 用户的电子邮件地址，在系统中必须唯一且不能为空。
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 用户密码
     *
     * 用户的登录密码，存储为明文（实际应用中应使用加密存储）。
     */
    @Column(nullable = false)
    private String password;

    /**
     * 用户角色
     *
     * 用户的身份角色，可为 "user"（普通用户）或 "admin"（管理员）。
     */
    @Column(nullable = false)
    private String role;

    /**
     * 用户状态码
     *
     * 用于会话管理的加密状态码，包含用户的 uid 和生成时间戳。
     * 有效期为 1 小时，每次用户请求后会自动更新。
     */
    @Column
    private String statusCode;
}

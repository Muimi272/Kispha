package com.muimi.kispha.service.impl;

import com.muimi.kispha.entity.User;
import com.muimi.kispha.repository.UserRepository;
import com.muimi.kispha.service.UserService;
import com.muimi.kispha.utils.StatusCodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAll() {
        logger.info("【用户查询】查询所有用户信息");
        return userRepository.findAll();
    }

    @Override
    public User findByUid(Long uid) {
        logger.debug("【用户查询】根据uid查询用户，uid：{}", uid);
        if (uid == null) {
            logger.warn("【用户查询】查询用户失败，uid为空");
            return null;
        }
        return userRepository.findByUid(uid);
    }

    @Override
    public List<User> findByUsernameContaining(String username) {
        logger.debug("【用户查询】根据用户名模糊查询，用户名关键词：{}", username);
        return userRepository.findByUsernameContaining(username);
    }

    @Override
    public User register(User user) {
        logger.info("【用户注册】开始处理用户注册请求，用户名：{}，邮箱：{}",
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null);

        // 1. 空值保护：避免NPE
        if (user == null) {
            logger.warn("【用户注册】注册失败，请求参数为空");
            return null;
        }
        // 2. 校验uid（新增用户uid应为null/0）
        if (user.getUid() != null && user.getUid() != 0) {
            logger.warn("【用户注册】注册失败，新增用户uid不能非0，当前uid：{}", user.getUid());
            return null;
        }
        // 3. 校验role（避免null调用equals）
        if (!"user".equals(user.getRole())) {
            logger.warn("【用户注册】注册失败，角色非法，当前角色：{}", user.getRole());
            return null;
        }
        // 4. 校验statusCode（新增用户应为空，先判null再判空）
        if (StringUtils.hasText(user.getStatusCode())) {
            logger.warn("【用户注册】注册失败，新增用户状态码需为空，当前状态码：{}", user.getStatusCode());
            return null;
        }
        // 5. 校验用户名和邮箱唯一性
        if (!checkUserNameAndEmailUniqueness(user)) {
            logger.warn("【用户注册】注册失败，用户名或邮箱已存在，用户名：{}，邮箱：{}", user.getUsername(), user.getEmail());
            return null;
        }

        try {
            // 6. 保存用户（JPA会自动赋值uid）
            User savedUser = userRepository.save(user);
            logger.debug("【用户注册】用户基础信息保存成功，生成uid：{}", savedUser.getUid());

            // 7. 生成状态码（使用save后自动赋值的uid）
            String statusCode = StatusCodeManager.generateStatusCode(savedUser.getUid());
            savedUser.setStatusCode(statusCode);

            // 8. 保存状态码
            User finalUser = userRepository.save(savedUser);
            logger.info("【用户注册】用户注册成功，uid：{}，生成状态码：{}", finalUser.getUid(), statusCode);
            return finalUser;
        } catch (Exception e) {
            logger.error("【用户注册】用户注册失败，用户名：{}，邮箱：{}，异常信息：{}",
                    user.getUsername(), user.getEmail(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public User updateUser(User user) {
        logger.info("【用户更新】开始处理用户更新请求，uid：{}", user != null ? user.getUid() : null);

        // 1. 空值保护
        if (user == null || user.getUid() == null) {
            logger.warn("【用户更新】更新失败，请求参数为空或uid为空");
            return null;
        }
        // 2. 检查用户是否存在
        User existingUser = userRepository.findByUid(user.getUid());
        if (existingUser == null) {
            logger.warn("【用户更新】更新失败，用户不存在，uid：{}", user.getUid());
            return null;
        }
        // 3. 校验密码（避免null调用equals）
        if (!StringUtils.hasText(user.getPassword())
                || !user.getPassword().equals(existingUser.getPassword())) {
            logger.warn("【用户更新】更新失败，密码为空或密码错误，uid：{}", user.getUid());
            return null;
        }
        // 4. 校验role
        if (!existingUser.getRole().equals(user.getRole())) {
            logger.warn("【用户更新】更新失败，角色不允许修改，uid：{}，原角色：{}，新角色：{}",
                    user.getUid(), existingUser.getRole(), user.getRole());
            return null;
        }
        // 5. 校验statusCode
        if (!StringUtils.hasText(user.getStatusCode())) {
            logger.warn("【用户更新】更新失败，状态码为空，uid：{}", user.getUid());
            return null;
        }

        String dbStatusCode = existingUser.getStatusCode();
        // 数据库状态码为空 → 比对失败
        if (!StringUtils.hasText(dbStatusCode)) {
            logger.warn("【用户更新】更新失败，数据库中无状态码，uid：{}", user.getUid());
            return null;
        }
        // 传入状态码与数据库状态码不一致 → 比对失败
        if (!user.getStatusCode().equals(dbStatusCode)) {
            logger.warn("【用户更新】更新失败，状态码与数据库不一致，uid：{}，传入状态码：{}，数据库状态码：{}",
                    user.getUid(), user.getStatusCode(), dbStatusCode);
            return null;
        }
        logger.debug("【用户更新】状态码与数据库比对成功，uid：{}", user.getUid());

        try {
            // 核心修改：调用你封装的checkStatusCode方法
            String newStatusCode = StatusCodeManager.checkStatusCode(user.getStatusCode(), user.getUid());
            // 验证失败（返回空字符串）则直接返回null
            if (!StringUtils.hasText(newStatusCode)) {
                logger.warn("【用户更新】更新失败，状态码验证失败，uid：{}，状态码：{}", user.getUid(), user.getStatusCode());
                return null;
            }


            // 6. 更新状态码
            existingUser.setStatusCode(newStatusCode);
            // 7. 校验用户名和邮箱唯一性（更新时需排除自身）
            if (!checkUserNameAndEmailUniquenessForUpdate(user)) {
                logger.warn("【用户更新】用户名/邮箱已存在（排除自身），uid：{}，用户名：{}，邮箱：{}",
                        user.getUid(), user.getUsername(), user.getEmail());
                return existingUser;
            }
            // 8. 更新用户其他信息
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());
            // 9. 保存更新
            User updatedUser = userRepository.save(existingUser);
            logger.info("【用户更新】用户更新成功，uid：{}，更新后状态码：{}", updatedUser.getUid(), newStatusCode);
            return updatedUser;
        } catch (Exception e) {
            logger.error("【用户更新】用户更新失败，uid：{}，异常信息：{}", user.getUid(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public User login(User user) {
        logger.info("【用户登录】开始处理用户登录请求，uid：{}", user != null ? user.getUid() : null);

        // 1. 空值保护
        if (user == null || user.getUid() == null || !StringUtils.hasText(user.getPassword())) {
            logger.warn("【用户登录】登录失败，请求参数为空、uid为空或密码为空");
            return null;
        }
        // 2. 查找用户
        User existingUser = userRepository.findByUid(user.getUid());
        if (existingUser == null) {
            logger.warn("【用户登录】登录失败，用户不存在，uid：{}", user.getUid());
            return null;
        }
        // 3. 校验密码
        if (!user.getPassword().equals(existingUser.getPassword())) {
            logger.warn("【用户登录】登录失败，密码错误，uid：{}", user.getUid());
            return null;
        }

        try {
            // 4. 生成新状态码
            String newStatusCode = StatusCodeManager.generateStatusCode(user.getUid());
            existingUser.setStatusCode(newStatusCode);
            // 5. 保存状态码
            User loginUser = userRepository.save(existingUser);
            logger.info("【用户登录】用户登录成功，uid：{}，生成新状态码：{}", loginUser.getUid(), newStatusCode);
            return loginUser;
        } catch (Exception e) {
            logger.error("【用户登录】用户登录失败，uid：{}，异常信息：{}", user.getUid(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteByUid(Long uid, User admin) {
        logger.info("【删除用户】开始处理删除用户请求，待删除uid：{}，操作管理员uid：{}",
                uid, admin != null ? admin.getUid() : null);

        // 1. 空值保护
        if (uid == null || admin == null || admin.getUid() == null) {
            logger.warn("【删除用户】删除失败，待删除uid为空、管理员参数为空或管理员uid为空");
            return false;
        }
        User adminUser = userRepository.findByUid(admin.getUid());
        if (adminUser == null) {
            logger.warn("【删除用户】删除失败，管理员不存在，管理员uid：{}", admin.getUid());
            return false;
        }
        // 2. 校验管理员权限
        if (!"admin".equals(adminUser.getRole())) {
            logger.warn("【删除用户】删除失败，非管理员权限，管理员uid：{}，角色：{}", admin.getUid(), adminUser.getRole());
            return false;
        }
        // 3. 校验管理员密码
        if (!admin.getPassword().equals(adminUser.getPassword())) {
            logger.warn("【删除用户】删除失败，管理员密码错误，管理员uid：{}", admin.getUid());
            return false;
        }
        // 4. 校验待删除用户是否存在
        User user = userRepository.findByUid(uid);
        if (user == null) {
            logger.warn("【删除用户】删除失败，待删除用户不存在，uid：{}", uid);
            return false;
        }

        try {
            // 5. 删除用户
            userRepository.delete(user);
            logger.info("【删除用户】用户删除成功，uid：{}，操作管理员uid：{}", uid, admin.getUid());
            return true;
        } catch (Exception e) {
            logger.error("【删除用户】删除用户失败，待删除uid：{}，管理员uid：{}，异常信息：{}",
                    uid, admin.getUid(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 新增用户时校验用户名和邮箱唯一性
     */
    private boolean checkUserNameAndEmailUniqueness(User user) {
        if (user == null) {
            logger.debug("【唯一性校验】校验失败，用户参数为空");
            return false;
        }
        // 用户名非空且已存在 → 不唯一
        if (StringUtils.hasText(user.getUsername())
                && userRepository.findByUsername(user.getUsername()) != null) {
            logger.debug("【唯一性校验】用户名已存在，用户名：{}", user.getUsername());
            return false;
        }
        // 邮箱非空且已存在 → 不唯一
        if (StringUtils.hasText(user.getEmail())
                && userRepository.findByEmail(user.getEmail()) != null) {
            logger.debug("【唯一性校验】邮箱已存在，邮箱：{}", user.getEmail());
            return false;
        }
        logger.debug("【唯一性校验】用户名和邮箱均唯一，用户名：{}，邮箱：{}", user.getUsername(), user.getEmail());
        return true;
    }

    /**
     * 更新用户时校验用户名和邮箱唯一性（排除自身）
     */
    private boolean checkUserNameAndEmailUniquenessForUpdate(User user) {
        if (user == null || user.getUid() == null) {
            logger.debug("【更新唯一性校验】校验失败，用户参数为空或uid为空");
            return false;
        }
        // 校验用户名：存在且不是当前用户 → 不唯一
        if (StringUtils.hasText(user.getUsername())) {
            User userByUsername = userRepository.findByUsername(user.getUsername());
            if (userByUsername != null && !user.getUid().equals(userByUsername.getUid())) {
                logger.debug("【更新唯一性校验】用户名已存在（非自身），uid：{}，用户名：{}", user.getUid(), user.getUsername());
                return false;
            }
        }
        // 校验邮箱：存在且不是当前用户 → 不唯一
        if (StringUtils.hasText(user.getEmail())) {
            User userByEmail = userRepository.findByEmail(user.getEmail());
            if (userByEmail != null && !user.getUid().equals(userByEmail.getUid())) {
                logger.debug("【更新唯一性校验】邮箱已存在（非自身），uid：{}，邮箱：{}", user.getUid(), user.getEmail());
                return false;
            }
        }
        logger.debug("【更新唯一性校验】用户名和邮箱均唯一（排除自身），uid：{}", user.getUid());
        return true;
    }
}
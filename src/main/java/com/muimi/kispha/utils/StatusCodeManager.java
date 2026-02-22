package com.muimi.kispha.utils;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * 登录验证状态码管理类
 * 提供生成和解析包含uid和时间戳的加密状态码的静态方法
 */
public class StatusCodeManager {
    private static final Logger logger = LoggerFactory.getLogger(StatusCodeManager.class);

    // AES加密密钥（必须是16位，建议替换为环境变量/配置文件读取）
    private static final String AES_KEY = "KisphaAESKeyCode";
    // AES偏移量（必须是16位，建议替换）
    private static final String AES_IV = "Muimi_KisphaCode";
    // 分隔符，用于拼接uid和时间戳
    private static final String SEPARATOR = "\\|";
    // 状态码有效期（毫秒）：1小时
    private static final long VALID_PERIOD = 60 * 60 * 1000L;

    // 静态代码块：校验密钥/偏移量长度（避免隐性错误）
    static {
        if (AES_KEY.getBytes(StandardCharsets.UTF_8).length != 16) {
            String errorMsg = "AES_KEY必须是16位字符串！当前长度：" + AES_KEY.length();
            logger.error("【状态码初始化】{}", errorMsg);
            throw new RuntimeException(errorMsg);
        }
        if (AES_IV.getBytes(StandardCharsets.UTF_8).length != 16) {
            String errorMsg = "AES_IV必须是16位字符串！当前长度：" + AES_IV.length();
            logger.error("【状态码初始化】{}", errorMsg);
            throw new RuntimeException(errorMsg);
        }
        logger.info("【状态码初始化】AES密钥/偏移量校验通过，初始化完成");
    }

    /**
     * 生成状态码：根据uid和当前时间戳生成加密的状态码
     *
     * @param uid 用户唯一标识（Long类型）
     * @return 加密后的状态码字符串
     */
    public static String generateStatusCode(Long uid) {
        if (uid == null) {
            logger.warn("【状态码生成】生成失败，uid为空");
            throw new IllegalArgumentException("uid不能为空");
        }
        long currentTime = System.currentTimeMillis();
        String rawContent = uid + SEPARATOR + currentTime;
        try {
            byte[] encryptedBytes = aesEncrypt(rawContent.getBytes(StandardCharsets.UTF_8));
            String statusCode = Base64.getEncoder().encodeToString(encryptedBytes);
            logger.debug("【状态码生成】成功生成状态码，uid={}，生成时间={}，状态码={}",
                    uid, new Date(currentTime), statusCode);
            return statusCode;
        } catch (Exception e) {
            logger.error("【状态码生成】生成失败，uid={}，异常信息：{}", uid, e.getMessage(), e);
            throw new RuntimeException("生成状态码失败", e);
        }
    }

    /**
     * 解析状态码：从加密的状态码中提取uid和生成时间（改为public，方便调试）
     *
     * @param statusCode 加密的状态码字符串
     * @return 包含uid和生成时间的StatusInfo对象
     */
    public static StatusInfo parseStatusCode(String statusCode) {
        if (statusCode == null || statusCode.isEmpty()) {
            logger.warn("【状态码解析】解析失败，状态码为空");
            throw new IllegalArgumentException("状态码不能为空");
        }
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(statusCode);
            byte[] decryptedBytes = aesDecrypt(encryptedBytes);
            String rawContent = new String(decryptedBytes, StandardCharsets.UTF_8);
            String[] parts = rawContent.split(SEPARATOR);
            if (parts.length != 2) {
                logger.warn("【状态码解析】解析失败，状态码格式错误，解析出的内容：{}", rawContent);
                throw new IllegalArgumentException("状态码格式错误，解析出的内容：" + rawContent);
            }
            Long uid = Long.parseLong(parts[0]);
            long generateTime = Long.parseLong(parts[1]);
            StatusInfo info = new StatusInfo(uid, generateTime);
            logger.debug("【状态码解析】成功解析状态码，状态码={}，解析结果：uid={}，生成时间={}",
                    statusCode, uid, new Date(generateTime));
            return info;
        } catch (Exception e) {
            logger.error("【状态码解析】解析失败，状态码={}，异常信息：{}", statusCode, e.getMessage(), e);
            throw new RuntimeException("解析状态码失败：statusCode=" + statusCode, e);
        }
    }

    /**
     * 校验状态码：验证uid匹配+未过期，通过则返回新状态码
     *
     * @param statusCode 待校验的状态码
     * @param uid        待验证的uid
     * @return 验证通过返回新状态码，失败返回空字符串
     */
    public static String checkStatusCode(String statusCode, Long uid) {
        logger.debug("【状态码校验】开始校验状态码，uid={}，状态码={}", uid, statusCode);

        if (uid == null) {
            logger.warn("【状态码校验】校验失败，uid为空");
            return "";
        }
        try {
            // 1. 解析状态码
            StatusInfo info = parseStatusCode(statusCode);

            // 2. 验证uid匹配
            if (!info.getUid().equals(uid)) {
                logger.warn("【状态码校验】校验失败，uid不匹配，解析出的uid={}，传入的uid={}",
                        info.getUid(), uid);
                return "";
            }

            // 3. 验证未过期
            long currentTime = System.currentTimeMillis();
            boolean isExpired = info.getGenerateTime() + VALID_PERIOD < currentTime;
            if (isExpired) {
                logger.warn("【状态码校验】校验失败，状态码已过期，uid={}，生成时间={}，当前时间={}，有效期1小时",
                        uid, new Date(info.getGenerateTime()), new Date(currentTime));
                return "";
            }

            // 4. 验证通过，生成新状态码
            String newStatusCode = generateStatusCode(uid);
            logger.info("【状态码校验】校验成功，uid={}，状态码未过期，生成新状态码={}", uid, newStatusCode);
            return newStatusCode;
        } catch (Exception e) {
            logger.error("【状态码校验】校验失败，解析异常，uid={}，状态码={}，异常信息：{}",
                    uid, statusCode, e.getMessage(), e);
            return "";
        }
    }

    /**
     * AES加密核心方法
     */
    private static byte[] aesEncrypt(byte[] content) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(AES_IV.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(content);
    }

    /**
     * AES解密核心方法
     */
    private static byte[] aesDecrypt(byte[] encryptedBytes) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(AES_IV.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(encryptedBytes);
    }

    /**
     * 状态码解析结果封装类
     */
    @Getter
    public static class StatusInfo {
        private final Long uid;          // 用户ID
        private final long generateTime; // 状态码生成时间（毫秒时间戳）

        public StatusInfo(Long uid, long generateTime) {
            this.uid = uid;
            this.generateTime = generateTime;
        }

        @Override
        public String toString() {
            return "StatusInfo{" +
                    "uid=" + uid +
                    ", generateTime=" + generateTime +
                    "(" + new Date(generateTime) + ")" +
                    '}';
        }
    }
}
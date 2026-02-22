# Kispha

Spring Boot 用户管理系统，提供用户注册、登录、信息更新、工作流程等功能，内置状态码加密验证机制。

## 项目信息

- **框架**: Spring Boot 4.0.3
- **Java 版本**: 17
- **数据库**: MySQL
- **主要依赖**: Spring Data JPA、Lombok、Spring Web

## 快速开始

### 前置条件

- Java 17 及以上
- MySQL 5.7+（数据库名: Kispha）
- Maven 3.6+（仅需重新构建时）

### 运行应用

```bash
java -jar Kispha.jar
```

默认启动端口：**8090**

### 配置数据库

默认的数据库配置为：

```
数据库地址：http://localhost:3306
数据库：Mysql
数据库名：Kispha
管理员：admin
管理员密码：123456
```

如果需要修改，请修改源代码中的`application.properties`文件，然后重新构建项目。
### 重新构建

```bash
mvn clean package
```

构建输出: `target/Kispha.jar`

---

## API 接口说明

### 基础地址
```
http://localhost:8090
```

### 1. 用户注册
**端点**: `POST /users/register`

**请求体**:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "123456",
  "role": "user"
}
```

**响应** (成功 200):
```json
{
  "uid": 1,
  "username": "testuser",
  "email": "test@example.com",
  "password": "123456",
  "role": "user",
  "statusCode": "加密的状态码字符串"
}
```

**说明**:
- `uid`: 自动生成（注册时无需传递）
- `role`: 必须为 `user`
- `statusCode`: 系统自动生成，初次注册后自动分配
- 用户名和邮箱具有唯一性约束

---

### 2. 用户登录
**端点**: `POST /users/login`

**请求体**:
```json
{
  "uid": 1,
  "password": "123456"
}
```

**响应** (成功 200):
```json
{
  "uid": 1,
  "username": "testuser",
  "email": "test@example.com",
  "password": "123456",
  "role": "user",
  "statusCode": "新的加密状态码"
}
```

**说明**:
- 登录时需传递 `uid` 和 `password`
- 登录成功会生成新的 `statusCode`，用于后续操作

---

### 3. 用户更新
**端点**: `POST /users/update`

**请求体**:
```json
{
  "uid": 1,
  "username": "newusername",
  "email": "newemail@example.com",
  "password": "123456",
  "role": "user",
  "statusCode": "当前有效的状态码"
}
```

**响应** (成功 200):
```json
{
  "uid": 1,
  "username": "newusername",
  "email": "newemail@example.com",
  "password": "123456",
  "role": "user",
  "statusCode": "新生成的状态码"
}
```

**说明**:
- 必须提供当前有效的 `statusCode`
- 密码必须与数据库中的密码一致
- `role` 不允许修改
- 状态码校验成功后自动生成新的状态码
- 状态码有效期为 **1 小时**

---

### 4. 工作接口（仅测试使用，未添加实质性功能）
**端点**: `POST /users/work`

**请求体**:
```json
{
  "uid": 1,
  "statusCode": "当前有效的状态码"
}
```

**响应** (成功 200):
```
worked
```

**说明**:
- 用于验证用户的 `statusCode` 有效性
- 需要传递 `uid` 和有效的 `statusCode`
- 验证成功会生成新的 `statusCode`
- 常用于需要权限检查的长流程操作

---

### 5. 删除用户
**端点**: `POST /users/delete/{uid}`

**路径参数**:
- `uid`: 待删除用户的 ID

**请求体**:
```json
{
  "uid": 2,
  "password": "admin密码",
  "role": "admin"
}
```

**响应** (成功 200):
```
deleted
```

**说明**:
- 仅 `role` 为 `admin` 的用户可删除其他用户
- 必须提供管理员的正确密码
- 删除操作需要管理员身份和密码双重验证

---

## 日志配置

### 日志级别

在 `application.properties` 中配置：

```properties
# 全局日志级别
logging.level.root=INFO

# 应用日志级别
logging.level.com.muimi.kispha=INFO
```

### 日志输出

#### 1. 控制台格式
```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n
```

**示例**:
```
2024-01-15 14:23:45.123 [main] INFO com.muimi.kispha.service.impl.UserServiceImpl - 【用户注册】用户注册成功，uid：1，生成状态码：abc123...
```

#### 2. 文件日志配置
```properties
# 日志文件位置
logging.file.name=./logs/kispha.log

# 单个日志文件最大大小
logging.logback.rollingpolicy.max-file-size=100MB

# 日志文件保留天数
logging.logback.rollingpolicy.max-history=30

# 所有日志文件总大小上限
logging.logback.rollingpolicy.total-size-cap=1GB
```

#### 3. 文件格式
```properties
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%PID] %-5level %logger{50} - %msg%n
```

**示例**:
```
2024-01-15 14:23:45.123 [main] [12345] INFO com.muimi.kispha.service.impl.UserServiceImpl - 【用户注册】用户注册成功，uid：1，生成状态码：abc123...
```

### 日志位置

- **输出目录**: `./logs/kispha.log`
- **文件滚动**: 日志文件按日期自动分割，历史日志保留 30 天
- **总容量**: 所有日志文件合计不超过 1GB

### 日志特点

项目在各个关键操作点添加了详细日志，包括：

| 操作 | 日志标签 | 级别 |
|------|---------|------|
| 用户注册 | `【用户注册】` | INFO |
| 用户登录 | `【用户登录】` | INFO |
| 用户更新 | `【用户更新】` | INFO |
| 删除用户 | `【删除用户】` | INFO |
| 工作流程 | `【用户工作接口】` | INFO |
| 状态码生成 | `【状态码生成】` | DEBUG |
| 状态码解析 | `【状态码解析】` | DEBUG |
| 状态码校验 | `【状态码校验】` | DEBUG |
| 异常错误 | 相应操作标签 | ERROR |

---

## 状态码机制

### 工作原理

1. **生成**: 用户注册或登录时自动生成加密状态码
2. **内容**: 状态码包含 `uid` 和生成时间戳，通过 AES-CBC 加密
3. **校验**: 需要用户操作时验证状态码的有效性
4. **更新**: 每次验证成功后自动生成新状态码

### 关键参数

- **加密算法**: AES-256 (CBC 模式)
- **有效期**: 1 小时
- **编码**: Base64

### 状态码流程

```
注册/登录 → 生成初始状态码
          ↓
用户操作 → 提交 statusCode
          ↓
验证状态码 (uid + 时间戳 + 过期检查)
          ↓
验证通过 → 生成新状态码
验证失败 → 返回 400 错误
```

---

## 数据库表结构

### User 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| uid | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(255) | NOT NULL, UNIQUE | 用户名 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | 邮箱 |
| password | VARCHAR(255) | NOT NULL | 密码 |
| role | VARCHAR(255) | NOT NULL | 角色（user/admin） |
| statusCode | TEXT | NULLABLE | 加密状态码 |

---

## 常见问题

### Q1: 状态码过期了怎么办？
**A**: 需要重新登录以获取新的状态码。状态码有效期为 1 小时。

### Q2: 如何修改日志级别？
**A**: 修改 `application.properties` 中的 `logging.level.com.muimi.kispha` 值：
- `INFO`: 仅显示关键操作
- `DEBUG`: 显示详细的调试信息
- `ERROR`: 仅显示错误

### Q3: 数据库连接失败怎么办？
**A**: 检查以下内容：
1. MySQL 是否已启动
2. 数据库名称是否为 `Kispha`
3. 用户名/密码是否正确（默认: admin/123456）
4. 检查日志文件 `./logs/kispha.log` 查看具体错误

### Q4: 非管理员可以删除用户吗？
**A**: 不可以。删除用户操作仅限 `role` 为 `admin` 的用户执行，且必须验证管理员密码。

---

## 联系方式

`muimi_mail@163.com`

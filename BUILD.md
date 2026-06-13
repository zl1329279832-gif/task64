# 构建与部署指南 — 高校教师电子名片系统

## 环境要求

| 组件 | 版本 |
|------|------|
| JDK | 8+ |
| Maven | 3.6+ |
| Node.js | 12.x（Vue 2 + node-sass 限制） |
| MySQL | 5.7+ / 8.0 |
| Docker | 20+（可选） |
| Docker Compose | 1.29+（可选） |

---

## 一、构建方式

### 方式 A：完整构建（npm + Maven）

适合首次构建或修改了前端代码后：

```bash
# 1. 构建前端
cd gaoxiaojiaoshidianzimingpian/src/main/resources/admin/admin
npm install
npm run build
# 输出在 dist/ 目录

# 2. 构建后端（回到项目根目录）
cd gaoxiaojiaoshidianzimingpian
mvn clean package -DskipTests
# 输出: target/gaoxiaojiaoshidianzimingpian-0.0.1-SNAPSHOT.jar
```

### 方式 B：仅后端构建（前端未修改）

如果 `dist/` 目录已经是最新的（已提交到仓库），直接：

```bash
cd gaoxiaojiaoshidianzimingpian
mvn clean package -DskipTests
```

### 方式 C：Docker Compose 一键构建

```bash
cp .env.example .env
# 编辑 .env 填写实际密码
docker-compose up -d --build
```

---

## 二、运行

### 本地开发

```bash
cd gaoxiaojiaoshidianzimingpian
mvn spring-boot:run
# 访问 http://localhost:8080/gaoxiaojiaoshidianzimingpian/front/front/index.html
# 管理后台 http://localhost:8080/gaoxiaojiaoshidianzimingpian/admin/dist/index.html
```

### 生产环境（JAR + Nginx）

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-mysql-host
export DB_PASSWORD=your-strong-password
export UPLOAD_BASE_PATH=/data/uploads

# 启动
java -jar gaoxiaojiaoshidianzimingpian-0.0.1-SNAPSHOT.jar
```

Nginx 配置见 `deploy/nginx.conf`。

### Docker Compose 部署

```bash
cp .env.example .env
vim .env  # 修改密码等配置
docker-compose up -d
# 访问 http://your-server/gaoxiaojiaoshidianzimingpian/front/front/index.html
```

---

## 三、部署 Checklist

### 3.1 数据库初始化

```sql
CREATE DATABASE gaoxiaojiaoshidianzimingpian DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

导入 `sql/migration_v1.sql`（如为首次部署还需导入完整建表 SQL）。

### 3.2 百度 AI 密钥配置

百度 AI 相关密钥（`baidu_ditu_ak`、`APIKey`、`SecretKey`）通过管理后台的 **"配置管理"** 页面设置，存储在数据库 `config` 表中，**不需要写入配置文件**。

部署后以管理员登录 → 系统管理 → 配置管理 → 添加/修改以下配置项：

| name | 说明 |
|------|------|
| `baidu_ditu_ak` | 百度地图 AK |
| `APIKey` | 百度 AI API Key |
| `SecretKey` | 百度 AI Secret Key |

### 3.3 上传目录

- 开发环境：默认 `./static/upload/`（JAR 运行目录下的相对路径）
- 生产环境：设置 `UPLOAD_BASE_PATH` 指向 NAS 挂载点或持久化目录
- Docker：已通过 volume 自动挂载

### 3.4 反向代理

参考 `deploy/nginx.conf` 配置 Nginx 反向代理，注意：
- `client_max_body_size` 需匹配 Spring 的 multipart 限制（当前 1000M）
- 上传文件由 Nginx 直接提供（alias 到上传目录），模板 XLS fallback 到 Spring Boot
- **不需要 WebSocket 支持**（系统未使用 WebSocket）

---

## 四、环境变量一览

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SPRING_PROFILES_ACTIVE` | Spring Profile | `prod` |
| `DB_HOST` | MySQL 主机 | `localhost` |
| `DB_PORT` | MySQL 端口 | `3306` |
| `DB_NAME` | 数据库名 | `gaoxiaojiaoshidianzimingpian` |
| `DB_USER` | 数据库用户 | `root` |
| `DB_PASSWORD` | 数据库密码 | **必填** |
| `UPLOAD_BASE_PATH` | 上传文件根目录 | `/data/uploads` |

---

## 五、前端开发

```bash
cd gaoxiaojiaoshidianzimingpian/src/main/resources/admin/admin

# 安装依赖
npm install

# 开发模式（热更新，自动代理后端）
npm run serve
# 访问 http://localhost:8081

# 生产构建
npm run build
# 输出到 dist/，由 Spring Boot 作为静态资源提供
```

> **注意**：本项目使用 Vue 2 + node-sass，Node.js 版本需为 12.x。高版本 Node 请使用 `npm install --force` 或考虑迁移到 `sass`（dart-sass）。

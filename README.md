# 校园小卖部购物与配送系统

一个前后端分离的校园电商项目：Spring Boot 3.2 + MyBatis + MySQL 8 + Redis + Vue 3 + Element Plus，另外接了 Spring AI 做导购助手，并实现了限时秒杀链路。

主要看点：

- **AI 导购 Agent**：Spring AI `ChatClient` + 6 个 `@Tool` 工具调用，模型自主查 MySQL 真实商品数据；`ChatMemory` 多轮记忆；SSE 流式返回（`GET /ai/chat`）。
- **限时秒杀**：Redis + Lua 原子扣库存与「一人一单」前置拦截，`seckill_record` 唯一索引兜底，`@Async` 线程池异步落库，失败补偿回滚库存；管理端可配置活动。
- **Redis 商品缓存**：Cache-Aside 模式，空值防穿透、互斥锁防击穿、随机 TTL 防雪崩，事务提交后再删缓存。
- **超时自动关单**：定时扫描 `close_deadline`，条件更新保证幂等，退款与库存回补统一走一条链路。
- **并发与算法**：下单用 `UPDATE ... WHERE store >= num` 原子扣库存、`account >= amount` 原子扣余额，取消/关单与骑手接单都用条件更新保证幂等；另有 Dijkstra 校园配送最短路径（`vue/src/utils/campusMap.js`）、`ConcurrentHashMap` + TTL 验证码缓存、订单/充值事务。

---
---

## 环境要求

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | `pom.xml` 里 `java.version=17` |
| Maven | 3.8+ | 项目自带 `mvnw.cmd`，也可用本机 `mvn` |
| MySQL | 8.0+ | 库名 `code2026`，账号密码在 `application.yml` |
| Redis | 5+ | 端口 6379，秒杀库存与商品缓存都依赖它 |
| Node.js | 18+ | 前端 Vite 4 |

---

## 快速启动（全新安装，5 步）

以下命令均在**仓库根目录**执行，示例为 Windows Git Bash。

**① 建库建表**. `schema.sql` 里自带 `CREATE DATABASE IF NOT EXISTS` 和 `USE`，不需要手动建库：

```bash
mysql -uroot -p < code2026/sql/schema.sql
mysql -uroot -p < code2026/sql/data.sql
```

`schema.sql` 包含全部 15 张表（含秒杀表、`orders.close_deadline` 列和所有索引），全部用 `CREATE TABLE IF NOT EXISTS`，**不含 DROP**，可重复执行；`data.sql` 用 `INSERT IGNORE` 灌入管理员、分类、商品、用户、轮播图、骑手等静态数据，同样可重复执行。两个脚本都不含运行期业务数据，新库是干净的。

**② 按需改配置** `code2026/src/main/resources/application.yml`：

- `spring.datasource.username / password`：默认 `root / 888888`，改成你本机的。
- `spring.ai.openai.api-key`：默认读环境变量 `${DEEPSEEK_API_KEY:sk-demo}`。**不要把真实密钥写进这个文件**，它会跟着代码进版本库。IDEA 里在 Run Configuration → Environment variables 填 `DEEPSEEK_API_KEY=sk-xxx`；命令行用 `DEEPSEEK_API_KEY=sk-xxx mvn spring-boot:run`。不配也能启动，只是 AI 助手会鉴权失败，其余功能正常。
- `spring.data.redis.host / port`：默认 `localhost:6379`。
- `order.close-minutes`：超时自动关单时间，默认 15 分钟；想快速看效果可改成 `1`，`close-enabled: false` 可整体关掉。
- `security.password.migrate-on-startup`：启动时把历史明文密码升级成 BCrypt，默认 `true`。设 `false` 也不影响使用——用户登录校验通过时会就地升级，只是没登录过的账号会一直是明文。

**③ 启动 Redis**（Windows 免安装版示例，路径按你本机改）：

```bash
cd /d/Redis && ./redis-server.exe redis.conf
/d/Redis/redis-cli.exe ping      # 返回 PONG 即可
```

**④ 启动后端**：

```bash
cd code2026
mvn spring-boot:run
```

成功标志：日志出现 `Tomcat started on port(s): 9090` 和 `Started Code2026Application`。

**⑤ 启动前端**：

```bash
cd vue
npm install
npm run dev
```

浏览器打开 Vite 提示的地址（通常 `http://localhost:5173`）。前端已把 `/api` 和 `/files` 代理到 9090，不用另配跨域。

> **图片显示不出来？** `FileController` 从「后端进程工作目录」下的 `files/` 读文件。若你在 `code2026/` 目录里启动后端，推荐在 `application.yml` 里显式指定目录，指向仓库根目录的图片：
>
> ```yaml
> file:
>   upload-dir: D:/桌面/文件/code/files
> ```
>
> 也可以直接复制一份：`cp -r files/. code2026/files/`。根目录的 `files/` 已随仓库保留，不要删。

---

## 已有旧库升级（不要跑 schema.sql）

如果你已经有一个旧版本的 `code2026` 库，按顺序执行增量脚本即可，不要重建库：

```bash
mysql -uroot -p code2026 < code2026/sql/patch_delivery_phone.sql
mysql -uroot -p code2026 < code2026/sql/patch_orders_delivery.sql
mysql -uroot -p code2026 < code2026/sql/seckill.sql
mysql -uroot -p code2026 < code2026/sql/order_timeout.sql
```

- `patch_*.sql`：早期版本缺字段，报 `Duplicate column name` 说明已存在，可忽略。
- `seckill.sql`：建秒杀表 + 插入一场示例活动，用 `@need` 判断，重复执行不会重复划拨商品库存。
- `order_timeout.sql`：只给 `orders` 加列和索引，**不回填历史数据**；历史订单 `close_deadline` 保持 NULL，定时任务不会误取消老订单。

---

## 演示账号

> 密码用 BCrypt（自带随机盐）存储，登录响应里不会出现密码字段。首次启动会自动把 `data.sql` 灌入的历史明文密码升级为密文，所以下表密码照常可用，不需要改。

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | `admin` | `admin` |
| 普通用户 | `111` | `111`（余额充足，可直接下单/秒杀） |
| 普通用户 | `123` | `123` |
| 骑手/配送员 | `222` | `333` |

管理端路径：左侧「信息管理」→ **限时秒杀** → 「新增秒杀活动」，选商品、填活动件数与起止时间，前台顶部导航「限时秒杀」立刻可见。活动件数从商品库存划拨，删除无人抢过的活动会归还库存。

---

## 主要接口

| 模块 | 接口 |
|---|---|
| AI 导购 | `GET /ai/chat?prompt=...&chatId=...`（SSE 流式） |
| 秒杀 | `GET /seckill/list`、`POST /seckill/{id}`、`GET /seckill/result`、`POST /seckill/preheat/{id}` |
| 秒杀管理 | `POST /seckill/activity`、`PUT /seckill/activity/{id}/status`、`DELETE /seckill/activity/{id}` |
| 商品 | `GET /goods/selectAll`、`GET /goods/selectById/{id}`、`GET /goods/cacheKeys`（缓存调试） |
| 文件 | `POST /files/upload`、`GET /files/download/{fileName}` |

Redis 默认是纯内存，重启后 `seckill:*` 与商品缓存会丢失；访问 `/seckill/list` 会按数据库自动预热重建，不会出错。生产环境应开启 RDB/AOF。

---

## 并发压测结论（本机实测）

把商品库存临时设为 5 件，用 20 个并发请求同时各下单 1 件（`POST /orders/add`）：

- **5 单成功、15 单返回「库存不足」**，库存 5 → 0，没有出现负数或超卖；
- 销量 +5、用户余额精确扣减 5 × 单价；取消其中一单后库存 +1、余额 +单价，重复取消不会重复退款；
- 关键在于库存和余额都用带条件的原子 `UPDATE`（`store >= num`、`account >= amount`）并判断影响行数，而不是 `select` 出来改完再 `updateById`。后者在并发下会「丢失更新」：实测修复前 5 单只扣了 1 单的钱。

## 常见启动问题

| 现象 | 原因 / 处理 |
|---|---|
| AI 助手返回 401 / 鉴权失败 | 没配环境变量，用的是占位值 `sk-demo`。注入 `DEEPSEEK_API_KEY` 即可，其它功能不受影响 |
| `Port 13504 was already in use` | 某些终端环境存在 `SERVER__PORT` 变量，被 Spring Boot 松散绑定成 `server.port`。显式指定：`mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"`，或在 IDEA VM options 加 `-Dserver.port=9090` |
| `Port 9090 was already in use` | 已有实例在跑，先停掉旧的，别同时开两个后端 |
| 秒杀库存显示异常 | Redis 重启后还没预热，调一次 `POST /seckill/preheat/{活动id}` |
| 命令行拼中文参数返回 400 | 如 `?status=停用` 需 URL 编码；前端 axios 会自动编码，不受影响 |

---

## 安全说明（提交到公开仓库前必做）

本项目定位是本地演示 / 面试展示。下面区分「已经处理」和「有意保留」两块。

**已处理**

- DeepSeek 密钥改为环境变量 `${DEEPSEEK_API_KEY}`，配置文件里只剩占位默认值。注意：曾以明文形式存在过的密钥仍建议去控制台轮换一次。
- 密码用 `BCryptPasswordEncoder` 加盐存储；`password` / `newPassword` 标为 Jackson `WRITE_ONLY`，只进不出。启动时批量把历史明文升级为密文，登录成功也会就地升级。
  为什么不是 `@JsonIgnore`：`User` 同时是注册/新增的入参，`@JsonIgnore` 会让反序列化也忽略该字段，注册时密码永远是 null。要做到「只写不读」必须用 `@JsonProperty(access = WRITE_ONLY)`。
- `user` / `admin` 的 `updateById` 原本是全字段覆盖，改昵称时不传密码会把密文刷成 NULL；现已改成动态 `<set>`，密码为空就跳过该列。

**有意保留（公开部署前必须补）**

- 接口没有鉴权，`userId` 由前端传入，管理端接口可直接调用。生产需要 JWT + `HandlerInterceptor` 做身份与角色校验。这是本项目**明确知道边界、暂时不做**的部分——面试时应当主动说明，而不是声称做过权限控制。
- 上传已在代码层限制 10MB、只允许图片后缀、UUID 重命名并做路径校验；`spring.servlet.multipart.max-file-size` 仍为 100MB，公开部署应收窄到与代码一致。

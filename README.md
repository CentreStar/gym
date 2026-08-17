# Star GYM 健身房管理系统需求文档

**项目名称：** Star GYM 健身房管理系统  
**版本：** v2.0  
**文档日期：** 2026-08-17  
**状态：** 待评审  

---

## 目录
1. [项目概述](#1-项目概述)
2. [系统功能需求](#2-系统功能需求)
3. [非功能需求](#3-非功能需求)
4. [API 接口设计](#4-api-接口设计)
5. [数据库设计](#5-数据库设计)
6. [技术架构](#6-技术架构)
7. [部署与运维](#7-部署与运维)
8. [迭代计划](#8-迭代计划)

---

## 1. 项目概述

### 1.1 项目背景
Star GYM 是一个面向中小型健身房的数字化综合管理平台。当前健身房行业存在会员管理混乱、课程预约效率低、教练资源闲置、运营数据缺失等痛点。本项目旨在通过一套完整的移动端 + 管理后台 + 后端服务解决方案，帮助健身房实现：
- **会员体验升级**：扫码入场、在线购卡、课程预约、运动数据追踪。
- **运营效率提升**：自动化会员管理、教练入驻审核、通知精准推送。
- **数据驱动决策**：实时运营看板、多维度统计报表。

### 1.2 项目目标
#### 业务目标
| 目标 | 衡量指标 |
| :--- | :--- |
| 会员自助服务率 ≥ 80% | 购卡、预约、退卡在线完成比例 |
| 教练入驻审核周期 ≤ 24 小时 | 从提交申请到审核完成的平均时长 |
| 运营数据可视化 | 看板覆盖 5+ 核心指标 |
| 系统可用性 ≥ 99.5% | 月度服务不可用时间 < 3.6 小时 |

#### 技术目标
- **前后端分离**：移动端（Flutter）+ Web 管理后台（Vue 3）+ RESTful API（Spring Boot）。
- **高可扩展性**：支持未来多场馆、多租户扩展。
- **安全合规**：JWT 鉴权、密码加密、操作审计日志。
- **容器化部署**：支持 Docker + Docker Compose 一键启动。

### 1.3 用户角色与权限矩阵

| 角色 | 标识 | 核心权限 |
| :--- | :--- | :--- |
| **访客** | GUEST | 查看首页、注册、登录 |
| **会员** | USER | 购卡、预约课程、扫码入场、记录运动、查看通知、申请退卡、申请教练入驻 |
| **教练** | COACH | 发布空闲时间、管理私教预约、课程签到、查看课程安排、收入统计 |
| **管理员** | ADMIN | 管理会员（禁用/恢复）、审核教练入驻、发布通知、查看数据看板、处理退款、系统配置 |

---

## 2. 系统功能需求

### 2.1 通用模块

#### 2.1.1 用户认证与授权
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 注册 | 用户名、密码、手机号（必填），用户名唯一 | P0 |
| 登录 | 用户名 + 密码，成功后返回 JWT Token | P0 |
| 修改密码 | 验证旧密码后设置新密码，密码需加密存储 | P1 |
| 找回密码 | 通过手机号验证码重置密码（需集成短信服务） | P1 |
| Token 刷新 | Token 过期前通过 Refresh Token 刷新 | P2 |
| 权限校验 | 请求拦截器验证 Token 有效性及角色权限 | P0 |

#### 2.1.2 统一响应格式
所有 API 返回统一格式：
```json
{
  "code": 0,
  "msg": "success",
  "data": { ... }
}
```
- `code = 0`：成功
- `code > 0`：业务错误（如参数校验失败）
- `code < 0`：系统错误（如服务器异常）

全局异常处理覆盖：
- 参数校验失败（`MethodArgumentNotValidException`）
- 资源不存在（`ResourceNotFoundException`）
- 权限不足（`AccessDeniedException`）
- 业务规则冲突（`BusinessException`）

---

### 2.2 会员端（移动端 - Flutter）

#### 2.2.1 首页
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 欢迎语 | 展示用户名 + 角色中心 | P0 |
| 快捷入口 | 9 宫格布局：购卡、二维码、运动记录、月度统计、我的订单、已约课程、预约私教、退卡申请、通知中心、教练入驻 | P0 |
| 今日推荐课程 | 展示今日热门课程（按预约量排序） | P2 |
| 会员卡到期提醒 | 会员卡即将到期（7 天内）时首页红点提醒 | P1 |
| 扫码入场 | 快捷扫码按钮，调用相机扫码 | P0 |

#### 2.2.2 会员卡管理
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 卡种列表 | 展示所有可购买卡种（月卡/季卡/年卡），含名称、价格、有效期、描述 | P0 |
| 购买会员卡 | 点击购买生成订单，状态为 PAID，自动生成会员权益记录 | P0 |
| 我的订单 | 查看所有订单（状态：PAID/REFUNDED/EXPIRED） | P0 |
| 会员权益查看 | 展示当前有效会员卡（卡名、生效日期、到期日期、状态） | P1 |
| 退卡申请 | 选择已购订单，填写退款原因及说明，提交管理员审核 | P0 |

**会员权益管理逻辑：**
```
购买成功 → 生成 user_memberships 记录：
  - startDate = 当前时间
  - endDate = 当前时间 + validDays
  - status = ACTIVE
入场时校验：
  - 是否存在 status=ACTIVE 且 endDate >= 当前时间的记录
  - 无有效记录则提示"请购买会员卡"
```

#### 2.2.3 扫码入场
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 二维码生成 | 基于 `GYM:{userId}:{timestamp}:{sign}` 生成，有效期 5 分钟 | P0 |
| 入场扫码 | 无活跃在场记录时，创建入场记录（enteredAt = now） | P0 |
| 出场扫码 | 存在活跃在场记录时，更新出场时间并计算本次运动时长（秒） | P0 |
| 防重复入场 | 已入场未出场时再次扫码提示"您已在馆内" | P0 |
| 会员卡校验 | 入场时检查会员卡是否在有效期内 | P1 |
| 扫码反馈 | 成功/失败时有震动反馈和声音提示 | P2 |

#### 2.2.4 运动记录与统计
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 添加训练记录 | 选择部位、类型、动作名称、重量、组数、时长、速度/坡度 | P0 |
| 训练汇总 | 展示总时长、总组数、总消耗卡路里 | P0 |
| 月度统计 | 展示当月到店天数、累计时长、运动日历（高亮到店日） | P0 |
| 月度筛选 | 支持按年月筛选历史统计数据 | P2 |
| 趋势图 | 展示每周运动频率折线图 | P2 |

#### 2.2.5 课程预约
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 课程列表 | 展示所有课程（团课/私教），含标题、类型、教练、时间 | P0 |
| 课程详情 | 展示课程介绍、剩余名额（已预约人数/容量） | P1 |
| 预约课程 | 校验是否满员（count >= capacity），未满员则生成预约记录 | P0 |
| 我的预约 | 展示已预约课程列表，支持取消（状态变为 CANCELLED） | P0 |
| 预约提醒 | 课程开始前 1 小时推送提醒 | P2 |

#### 2.2.6 教练入驻
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 提交申请 | 填写姓名、电话、资质证明（必填）、个人介绍 | P0 |
| 申请状态 | 展示当前申请状态（PENDING/PASSED/REJECTED） | P0 |
| 审核结果通知 | 审核通过/拒绝后推送系统通知 | P2 |

#### 2.2.7 通知中心
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 通知列表 | 按角色展示系统通知（ALL/USER/COACH/ADMIN） | P0 |
| 未读标记 | 通知列表中未读消息红点提示 | P2 |

---

### 2.3 教练端（移动端 - Flutter）

#### 2.3.1 教练工作台
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 今日课程 | 展示当天安排的团课/私教课程列表 | P0 |
| 课程签到 | 教练扫描会员二维码，标记学员已到课 | P1 |
| 收入统计 | 展示本月私教课时数、预估收入（按已确认课时 × 单价） | P2 |

#### 2.3.2 私教预约管理（新增）
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 预约列表 | 查看所有私教预约申请（状态：PENDING/APPROVED/REJECTED） | P1 |
| 确认预约 | 点击"确认"，状态变为 APPROVED，空闲时间标记为 BOOKED | P1 |
| 拒绝预约 | 点击"拒绝"，填写拒绝原因，状态变为 REJECTED | P1 |
| 预约提醒 | 有新预约申请时推送通知 | P2 |

#### 2.3.3 空闲时间管理
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 发布空闲时间 | 选择开始/结束时间（格式：2026-08-20T10:00） | P0 |
| 空闲时间列表 | 查看已发布的空闲时间及状态（OPEN/BOOKED） | P0 |
| 取消发布 | 取消未预约的空闲时间 | P2 |

---

### 2.4 管理后台（Web - Vue 3）

#### 2.4.1 数据看板（增强）
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 全局统计卡片 | 会员总数、今日入场人数、本月新注册、本月总收入 | P1 |
| 月度运动统计 | 输入用户名查询其月度运动天数及累计时长 | P0 |
| 热门课程 TOP 3 | 按预约量排序展示最受欢迎的课程 | P1 |
| 近 7 天入场趋势 | ECharts 折线图展示每日入场人数变化 | P1 |
| 实时数据刷新 | 每 30 秒自动刷新统计数据 | P2 |

#### 2.4.2 会员卡管理
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 卡种列表 | 查看所有会员卡（名称、有效期、价格、上下架状态） | P0 |
| 新增卡种 | 填写卡种信息并保存 | P2 |
| 编辑卡种 | 修改价格、描述、上下架状态 | P2 |

#### 2.4.3 通知发布（增强）
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 发布通知 | 标题 + 内容，支持选择目标角色（ALL/USER/COACH/ADMIN） | P0 |
| 定向推送 | 支持选择特定用户 ID 列表，精准推送 | P1 |
| 通知列表 | 查看已发布的所有通知及发送时间 | P2 |

#### 2.4.4 用户权限管理
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 用户列表 | 展示所有用户（用户名、角色、状态、注册时间） | P0 |
| 禁用/恢复 | 一键切换用户状态（NORMAL ↔ DISABLED） | P0 |
| 用户搜索 | 按用户名搜索用户 | P2 |
| 导出 Excel | 导出用户列表为 Excel 文件（含所有字段） | P2 |

#### 2.4.5 教练入驻审核
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 申请列表 | 展示所有申请（姓名、电话、资质证明、状态、申请时间） | P0 |
| 审核通过 | 点击通过，用户角色变为 COACH，发送通知 | P0 |
| 审核拒绝 | 点击拒绝，填写拒绝原因，发送通知 | P0 |

#### 2.4.6 退款审核（新增）
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 退款列表 | 展示所有 PENDING 状态的退款申请 | P1 |
| 审核通过 | 订单状态变为 REFUNDED，退还会员权益 | P1 |
| 审核拒绝 | 填写拒绝原因，状态变为 REJECTED，推送通知 | P1 |

#### 2.4.7 课程管理
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 课程列表 | 查看所有课程（标题、类型、教练、时间） | P0 |
| 新增课程 | 填写课程信息并保存 | P2 |
| 编辑课程 | 修改课程信息 | P2 |

#### 2.4.8 系统配置（新增）
| 功能点 | 描述 | 优先级 |
| :--- | :--- | :---: |
| 配置列表 | 展示所有系统配置项（key-value） | P2 |
| 修改配置 | 动态调整业务规则（如：最大预约数、退卡截止天数） | P2 |

---

## 3. 非功能需求

### 3.1 技术栈约束
| 层级 | 技术选型 | 版本 | 说明 |
| :--- | :--- | :---: | :--- |
| 移动端 | Flutter | 3.x | 一套代码，iOS/Android 双端运行 |
| 管理后台 | Vue 3 + TypeScript + Vite | 3.x / 5.x / 4.x | 组合式 API，轻量高效 |
| 后端 API | Spring Boot | 3.2+ | RESTful API，JWT 鉴权 |
| ORM | Spring Data JPA | - | 简化数据库操作 |
| 数据库 | MySQL | 8.0+ | 生产环境使用 |
| 缓存 | Redis | 7.0+ | Token 缓存、限流计数器 |
| 数据库迁移 | Flyway | 9.x | 版本化管理 Schema |
| 容器化 | Docker + Docker Compose | - | 一键部署 |

### 3.2 性能要求
| 指标 | 要求 |
| :--- | :--- |
| 并发用户数 | 支持 100+ 并发请求 |
| 接口响应时间 | P95 < 500ms |
| 扫码接口 | P95 < 200ms |
| 数据库连接池 | 最小 10，最大 50 |
| 静态资源缓存 | CDN 或 Nginx 缓存 |

### 3.3 安全要求
| 安全项 | 实现方式 |
| :--- | :--- |
| 密码加密 | BCrypt 加密（强度 10） |
| Token 鉴权 | JWT，有效期 2 小时，支持 Refresh Token |
| 防暴力破解 | 登录接口限流：每分钟最多 10 次尝试 |
| 防刷接口 | 扫码接口限流：每用户每分钟最多 5 次 |
| SQL 注入防护 | JPA 参数化查询 + 输入校验 |
| XSS 防护 | 前端输入过滤 + 后端转义 |
| CORS 配置 | 仅允许白名单域名跨域请求 |

### 3.4 数据一致性
| 场景 | 保证方式 |
| :--- | :--- |
| 课程预约 | 使用数据库唯一约束防止重复预约，分布式锁防止超卖 |
| 订单支付 | 状态机控制状态流转（PAID → REFUNDED） |
| 会员卡权益 | 事务保证订单生成与权益记录原子性 |
| 退卡审核 | 状态流转：PENDING → APPROVED/REJECTED |

### 3.5 日志与审计
| 日志类型 | 记录内容 | 保留时长 |
| :--- | :--- | :---: |
| 操作日志 | 管理员禁用用户、审核教练、处理退款（操作人、时间、IP、操作内容） | 90 天 |
| 登录日志 | 登录成功/失败（用户名、时间、IP、设备信息） | 30 天 |
| 业务日志 | 关键业务操作（购卡、预约、扫码） | 30 天 |
| 异常日志 | 系统异常堆栈，用于排查问题 | 30 天 |

**实现方式：** AOP + 自定义 `@AuditLog` 注解

---

## 4. API 接口设计

### 4.1 接口版本管理
- 所有接口路径包含版本号：`/api/v1/xxx`
- 版本升级时保持 v1 向后兼容至少 3 个月

### 4.2 接口列表

#### 4.2.1 用户模块 (`/user`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| POST | `/v1/register` | 用户注册 | 公开 |
| POST | `/v1/login` | 用户登录 | 公开 |
| POST | `/v1/password/change` | 修改密码 | USER |
| POST | `/v1/password/reset` | 重置密码（验证码） | 公开 |
| POST | `/v1/password/code` | 发送重置验证码 | 公开 |
| GET | `/v1/{username}` | 查询用户信息 | USER/ADMIN |

#### 4.2.2 会员卡模块 (`/api/v1/cards`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| GET | `` | 获取所有卡种 | USER/ADMIN |
| POST | `` | 新增卡种 | ADMIN |
| PUT | `/{id}` | 编辑卡种 | ADMIN |
| POST | `/purchase` | 购买会员卡 | USER |
| GET | `/owned/{username}` | 查询用户有效卡种 | USER/ADMIN |

#### 4.2.3 订单与退款模块 (`/api/v1`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| GET | `/orders/{username}` | 查询用户订单 | USER/ADMIN |
| POST | `/refunds` | 提交退卡申请 | USER |
| GET | `/refunds/pending` | 获取待审核退款列表 | ADMIN |
| PUT | `/refunds/{id}/approve` | 审核通过退款 | ADMIN |
| PUT | `/refunds/{id}/reject` | 审核拒绝退款 | ADMIN |

#### 4.2.4 课程模块 (`/api/v1`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| GET | `/courses` | 获取课程列表 | USER/COACH |
| GET | `/courses/{id}` | 获取课程详情（含剩余名额） | USER/COACH |
| POST | `/courses` | 新增课程 | ADMIN |
| PUT | `/courses/{id}` | 编辑课程 | ADMIN |
| POST | `/courses/{id}/book/{username}` | 预约课程 | USER |
| GET | `/bookings/{username}` | 查询用户预约 | USER |
| PUT | `/bookings/{id}/cancel` | 取消预约 | USER |
| POST | `/courses/{id}/checkin` | 课程签到（教练扫码） | COACH |

#### 4.2.5 教练模块 (`/api/v1/coach`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| POST | `/apply` | 提交入驻申请 | USER |
| GET | `/apply/status/{userId}` | 查询申请状态 | USER |
| GET | `/admin/apply/list` | 获取申请列表 | ADMIN |
| PUT | `/admin/apply/pass/{id}` | 审核通过 | ADMIN |
| PUT | `/admin/apply/reject/{id}` | 审核拒绝 | ADMIN |
| POST | `/{username}/availability` | 发布空闲时间 | COACH |
| GET | `/availability` | 获取空闲时间列表 | USER/COACH |
| GET | `/private-bookings` | 获取私教预约列表 | COACH |
| PUT | `/private-bookings/{id}/approve` | 确认私教预约 | COACH |
| PUT | `/private-bookings/{id}/reject` | 拒绝私教预约 | COACH |
| GET | `/income/{username}` | 获取收入统计 | COACH |

#### 4.2.6 签到模块 (`/api/v1/attendance`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| POST | `/scan` | 扫码入场/出场 | USER |
| GET | `/monthly/{username}` | 月度统计 | USER/ADMIN |
| GET | `/today` | 今日入场人数（实时） | ADMIN |

#### 4.2.7 通知模块 (`/api/v1/notifications`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| GET | `/{role}` | 获取通知列表 | USER/COACH/ADMIN |
| POST | `` | 发布通知 | ADMIN |
| PUT | `/{id}/read` | 标记已读 | USER/COACH/ADMIN |

#### 4.2.8 管理模块 (`/api/v1/admin`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| GET | `/users` | 获取用户列表 | ADMIN |
| PUT | `/users/{id}/status` | 禁用/恢复用户 | ADMIN |
| GET | `/dashboard/stats` | 获取看板统计数据 | ADMIN |
| GET | `/dashboard/trend` | 获取入场趋势数据 | ADMIN |
| GET | `/dashboard/top-courses` | 获取热门课程 | ADMIN |
| GET | `/export/users` | 导出用户 Excel | ADMIN |
| GET | `/export/report` | 导出月度报表 PDF | ADMIN |

#### 4.2.9 系统配置模块 (`/api/v1/config`)
| 方法 | 路径 | 描述 | 权限 |
| :--- | :--- | :--- | :---: |
| GET | `` | 获取所有配置 | ADMIN |
| PUT | `/{key}` | 更新配置 | ADMIN |

---

## 5. 数据库设计

### 5.1 ER 图（核心表关系）
```
users (1) ─── (N) gym_orders
users (1) ─── (N) user_memberships
users (1) ─── (N) course_bookings
users (1) ─── (N) gym_visits
users (1) ─── (N) exercise_records
users (1) ─── (1) coach_apply
users (1) ─── (N) coach_availability
users (1) ─── (N) private_bookings

membership_cards (1) ─── (N) gym_orders
membership_cards (1) ─── (N) user_memberships

gym_courses (1) ─── (N) course_bookings
```

### 5.2 表结构

#### 5.2.1 用户表 `users`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(100) | NOT NULL | BCrypt 加密密码 |
| phone | VARCHAR(20) | UNIQUE | 手机号 |
| role | VARCHAR(20) | DEFAULT 'USER' | USER/COACH/ADMIN |
| status | VARCHAR(20) | DEFAULT 'NORMAL' | NORMAL/DISABLED |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.2 会员卡模板表 `membership_cards`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(50) | NOT NULL | 卡名（月卡/季卡/年卡） |
| valid_days | INT | NOT NULL | 有效期（天） |
| price | DECIMAL(10,2) | NOT NULL | 价格 |
| description | VARCHAR(200) | | 描述 |
| enabled | BOOLEAN | DEFAULT TRUE | 是否上架 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.3 用户会员权益表 `user_memberships`（新增）
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL, FK → users.id | 用户 ID |
| card_id | BIGINT | NOT NULL, FK → membership_cards.id | 会员卡 ID |
| order_id | BIGINT | NOT NULL, FK → gym_orders.id | 订单 ID |
| start_date | DATETIME | NOT NULL | 生效日期 |
| end_date | DATETIME | NOT NULL | 到期日期 |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE/EXPIRED |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| **索引** | `idx_user_id`, `idx_user_status` | | |

#### 5.2.4 订单表 `gym_orders`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL | 用户 ID |
| card_id | BIGINT | | 会员卡 ID |
| type | VARCHAR(20) | | CARD/COURSE |
| title | VARCHAR(100) | | 商品名称 |
| amount | DECIMAL(10,2) | NOT NULL | 金额 |
| status | VARCHAR(20) | DEFAULT 'PAID' | PAID/REFUNDED/EXPIRED |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.5 退卡申请表 `refund_applications`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL | 用户 ID |
| order_id | BIGINT | NOT NULL | 订单 ID |
| card_name | VARCHAR(50) | | 卡名 |
| refund_amount | DECIMAL(10,2) | | 退款金额 |
| reason | VARCHAR(100) | NOT NULL | 退款原因 |
| description | VARCHAR(500) | | 补充说明 |
| reject_reason | VARCHAR(200) | | 拒绝原因（新增） |
| status | VARCHAR(20) | DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.6 运动记录表 `exercise_records`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL | 用户 ID |
| body_part | VARCHAR(20) | | 训练部位 |
| exercise_type | VARCHAR(20) | | 器械类型 |
| action_name | VARCHAR(50) | | 动作名称 |
| weight | DECIMAL(8,2) | | 重量（kg） |
| sets | INT | | 组数 |
| duration_minutes | INT | | 时长（分钟） |
| speed | DECIMAL(6,2) | | 速度 |
| incline | DECIMAL(5,2) | | 坡度 |
| calories | DECIMAL(8,2) | | 消耗卡路里 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.7 课程表 `gym_courses`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| title | VARCHAR(100) | NOT NULL | 课程标题 |
| type | VARCHAR(20) | | GROUP/PT |
| coach_name | VARCHAR(50) | | 教练姓名 |
| start_time | DATETIME | | 开始时间 |
| capacity | INT | DEFAULT 20 | 容量 |
| price | DECIMAL(10,2) | | 价格 |
| description | VARCHAR(500) | | 课程介绍 |
| venue_id | BIGINT | FK → venues.id | 场馆 ID（新增） |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.8 课程预约表 `course_bookings`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL | 用户 ID |
| course_id | BIGINT | NOT NULL | 课程 ID |
| status | VARCHAR(20) | DEFAULT 'BOOKED' | BOOKED/CANCELLED |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| **唯一约束** | `(user_id, course_id)` | | 防止重复预约 |

#### 5.2.9 教练入驻申请表 `coach_apply`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | UNIQUE, NOT NULL | 用户 ID |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| phone | VARCHAR(20) | | 电话 |
| description | VARCHAR(500) | | 个人介绍 |
| proof_material | VARCHAR(1000) | NOT NULL | 资质证明 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | PENDING/PASSED/REJECTED |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 申请时间 |

#### 5.2.10 教练空闲时间表 `coach_availability`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| coach_id | BIGINT | NOT NULL | 教练用户 ID |
| coach_name | VARCHAR(50) | | 教练姓名 |
| start_time | DATETIME | NOT NULL | 开始时间 |
| end_time | DATETIME | NOT NULL | 结束时间 |
| status | VARCHAR(20) | DEFAULT 'OPEN' | OPEN/BOOKED |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.11 私教预约表 `private_bookings`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| availability_id | BIGINT | NOT NULL | 空闲时间 ID |
| user_id | BIGINT | NOT NULL | 会员用户 ID |
| username | VARCHAR(50) | | 会员用户名 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| reject_reason | VARCHAR(200) | | 拒绝原因（新增） |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.12 签到记录表 `gym_visits`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL | 用户 ID |
| entered_at | DATETIME | NOT NULL | 入场时间 |
| exited_at | DATETIME | | 出场时间 |
| duration_seconds | BIGINT | | 运动时长（秒） |
| **索引** | `idx_user_entered (user_id, entered_at)` | | |

#### 5.2.13 通知表 `gym_notifications`
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| title | VARCHAR(100) | NOT NULL | 标题 |
| content | VARCHAR(2000) | NOT NULL | 内容 |
| target_role | VARCHAR(20) | DEFAULT 'ALL' | ALL/USER/COACH/ADMIN |
| target_user_ids | JSON | | 定向用户 ID 列表（新增） |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.14 场馆表 `venues`（新增 - 扩展预留）
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(100) | NOT NULL | 场馆名称 |
| address | VARCHAR(200) | | 地址 |
| phone | VARCHAR(20) | | 联系电话 |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE/INACTIVE |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 5.2.15 系统配置表 `system_configs`（新增）
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| config_key | VARCHAR(50) | UNIQUE, NOT NULL | 配置键 |
| config_value | VARCHAR(500) | NOT NULL | 配置值 |
| description | VARCHAR(200) | | 描述 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**初始配置项：**
| config_key | config_value | description |
| :--- | :--- | :--- |
| `max_booking_per_user` | `3` | 每个用户最大同时预约数 |
| `refund_deadline_days` | `7` | 退卡申请截止天数（购买后） |
| `qr_code_expire_seconds` | `300` | 二维码有效期（秒） |
| `private_lesson_price` | `299.00` | 私教课时单价 |

#### 5.2.16 操作日志表 `audit_logs`（新增）
| 字段 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| operator_id | BIGINT | | 操作人 ID |
| operator_name | VARCHAR(50) | | 操作人用户名 |
| action | VARCHAR(50) | NOT NULL | 操作类型 |
| target | VARCHAR(200) | | 操作目标 |
| details | VARCHAR(2000) | | 详情 JSON |
| ip_address | VARCHAR(50) | | 客户端 IP |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 操作时间 |
| **索引** | `idx_operator`, `idx_action`, `idx_create_time` | | |

---

## 6. 技术架构

### 6.1 整体架构图
```
┌─────────────────────────────────────────────────────────────┐
│                      客户端层                              │
├─────────────────────────────────────────────────────────────┤
│  Flutter App (iOS/Android)  │  Vue 3 管理后台             │
└─────────────────────────────────────────────────────────────┘
                              │ HTTPS
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     网关层 (Nginx)                         │
│           负载均衡 / 静态资源 / SSL 终止                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   应用层 (Spring Boot)                     │
├─────────────────────────────────────────────────────────────┤
│  Controller → Service → Repository                        │
│  JWT Filter / RateLimiter / Audit AOP                     │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌──────────────────┐ ┌──────────────┐ ┌──────────────────┐
│    MySQL 8.0     │ │   Redis 7.0  │ │   文件存储       │
│   主数据库        │ │  缓存/限流   │ │   (头像/图片)    │
└──────────────────┘ └──────────────┘ └──────────────────┘
```

### 6.2 包结构（后端）
```
com.example.gym_server/
├── controller/          # REST 控制器
├── service/             # 业务逻辑层
├── repository/          # JPA 数据访问
├── entity/              # 实体类
├── dto/                 # 数据传输对象
├── config/              # 配置类（Security, Redis, Cors）
├── filter/              # JWT 认证过滤器
├── interceptor/         # 限流拦截器
├── annotation/          # 自定义注解（@AuditLog）
├── aspect/              # AOP 切面（日志审计）
├── exception/           # 全局异常处理
├── util/                # 工具类（JWT, QRCode, Excel）
└── constant/            # 常量定义
```

### 6.3 核心流程

#### 6.3.1 扫码入场流程
```
1. App 生成二维码（含 userId + timestamp + sign）
2. 用户扫码（闸机/前台扫描）
3. 后端验签（有效期 5 分钟，防重放攻击）
4. 校验会员卡有效性（user_memberships 表）
5. 查询是否有活跃在场记录（exited_at IS NULL）
   ├── 无 → 创建入场记录（entered_at = now）
   └── 有 → 更新出场记录（exited_at = now, 计算时长）
6. 返回扫码结果 + 时长信息
```

#### 6.3.2 课程预约流程（防超卖）
```
1. 用户请求预约课程
2. 分布式锁（Redis: lock:course:{courseId}）
3. 查询已预约人数：SELECT COUNT(*) WHERE course_id = ? AND status = 'BOOKED'
4. 校验：count >= capacity → 返回"已满员"
5. 事务开启：
   a. 插入 course_bookings 记录
   b. 更新课程预约计数缓存
6. 释放锁，返回预约成功
```

#### 6.3.3 教练入驻审核流程
```
1. 会员提交申请（状态 PENDING）
2. 管理员查看申请列表
3. 管理员操作：
   ├── 通过：更新用户 role = COACH, 申请状态 = PASSED
   │        发送系统通知：入驻审核通过
   └── 拒绝：填写拒绝原因，申请状态 = REJECTED
             发送系统通知：入驻审核未通过 + 原因
4. 记录操作审计日志
```

---

## 7. 部署与运维

### 7.1 开发环境
| 组件 | 版本 | 启动方式 |
| :--- | :--- | :--- |
| Spring Boot | 3.2+ | `./gradlew bootRun` |
| MySQL | 8.0 | Docker 容器 |
| Redis | 7.0 | Docker 容器 |
| Vue 3 | - | `npm run dev` |
| Flutter | 3.x | `flutter run` |

### 7.2 Docker Compose 配置
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: gym_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7.0-alpine
    ports:
      - "6379:6379"

  gym-server:
    build: ./gym-server
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/gym_db
      SPRING_REDIS_HOST: redis
    ports:
      - "8080:8080"

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./vue-admin/dist:/usr/share/nginx/html
    depends_on:
      - gym-server
```

### 7.3 CI/CD 流水线（GitHub Actions）
```yaml
name: CI/CD Pipeline
on:
  push:
    branches: [main, develop]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Backend Tests
        run: ./gradlew test
      - name: Run Frontend Tests
        run: npm test

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - name: Build Spring Boot Image
        run: docker build -t gym-server:${{ github.sha }}
      - name: Build Vue Image
        run: docker build -t gym-admin:${{ github.sha }}
      - name: Push to Registry
        run: docker push gym-server:${{ github.sha }}

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Server
        run: ssh user@server "docker-compose pull && docker-compose up -d"
```

### 7.4 监控与告警
| 监控项 | 工具 | 告警阈值 |
| :--- | :--- | :--- |
| 应用健康 | Spring Actuator | 连续 3 次失败 |
| 接口响应时间 | Prometheus + Grafana | P95 > 1000ms |
| 数据库连接数 | Prometheus + Grafana | > 80% 连接池 |
| 磁盘空间 | Node Exporter | > 85% |
| JVM 内存 | Micrometer | > 90% |

---

## 8. 迭代计划

### 8.1 版本规划

| 版本 | 功能范围 | 预计工时 | 上线日期 |
| :--- | :--- | :---: | :---: |
| **v1.0** | 基础功能（当前已完成） | - | 已上线 |
| **v2.0** | 容量控制、会员权益、密码修改、统一响应 | 8 人天 | 第 1 周 |
| **v2.1** | 教练端增强（私教预约管理）、退款审核 | 6 人天 | 第 2 周 |
| **v2.2** | 数据看板增强、通知精准推送、课程签到 | 5 人天 | 第 3 周 |
| **v2.3** | Docker 容器化、接口限流、审计日志 | 5 人天 | 第 4 周 |
| **v3.0** | 多场馆支持、数据导出、系统配置 | 10 人天 | 第 6 周 |

### 8.2 优先级矩阵

| 优先级 | 功能 | 模块 | 状态 |
| :---: | :--- | :--- | :---: |
| 🔴 P0 | 课程容量与满员控制 | 课程预约 | 待开发 |
| 🔴 P0 | 会员卡权益管理（生效/过期） | 会员卡 | 待开发 |
| 🔴 P0 | 扫码安全增强（签名 + 有效期） | 签到 | 待开发 |
| 🔴 P0 | 防重复入场校验 | 签到 | 待开发 |
| 🔴 P0 | 统一响应格式 + 全局异常处理 | 后端基础 | 待开发 |
| 🟡 P1 | 密码修改与找回 | 用户 | 待开发 |
| 🟡 P1 | 教练私教预约管理 | 教练端 | 待开发 |
| 🟡 P1 | 管理后台退款审核 | 管理后台 | 待开发 |
| 🟡 P1 | 数据看板增强（图表） | 管理后台 | 待开发 |
| 🟢 P2 | 通知精准推送 | 通知 | 待开发 |
| 🟢 P2 | 课程签到（教练扫码） | 教练端 | 待开发 |
| 🟢 P2 | Docker 容器化 | 运维 | 待开发 |
| 🟢 P2 | 接口限流与防护 | 后端安全 | 待开发 |
| 🟢 P2 | 审计日志（AOP） | 后端基础 | 待开发 |
| 🔵 P3 | 多场馆支持 | 架构扩展 | 规划中 |
| 🔵 P3 | 数据导出 Excel/PDF | 管理后台 | 规划中 |
| 🔵 P3 | 系统配置管理 | 管理后台 | 规划中 |
| 🔵 P3 | 会员端交互优化（首页改版） | 移动端 | 规划中 |
| 🔵 P3 | 预约提醒与推送 | 通知 | 规划中 |
| 🔵 P3 | API 版本管理 | 后端架构 | 规划中 |
| 🔵 P3 | 数据库迁移（Flyway） | 运维 | 规划中 |

---

## 9. 附录

### 9.1 状态流转图

#### 9.1.1 用户状态
```
NORMAL ←→ DISABLED (管理员操作)
```

#### 9.1.2 课程预约状态
```
BOOKED → CANCELLED (用户取消)
```

#### 9.1.3 私教预约状态
```
PENDING → APPROVED (教练确认)
PENDING → REJECTED (教练拒绝)
```

#### 9.1.4 退卡状态
```
PENDING → APPROVED (管理员通过) → 订单状态变更为 REFUNDED
PENDING → REJECTED (管理员拒绝)
```

#### 9.1.5 教练入驻状态
```
PENDING → PASSED (管理员通过) → 用户角色变更为 COACH
PENDING → REJECTED (管理员拒绝)
```

#### 9.1.6 会员权益状态
```
ACTIVE → EXPIRED (到期自动变更，每日定时任务)
ACTIVE → CANCELLED (退卡通过时)
```

### 9.2 错误码定义

| 错误码 | 说明 | HTTP 状态码 |
| :---: | :--- | :---: |
| 0 | 成功 | 200 |
| 1001 | 参数校验失败 | 400 |
| 1002 | 资源不存在 | 404 |
| 1003 | 业务规则冲突 | 409 |
| 2001 | 用户名已存在 | 400 |
| 2002 | 密码错误 | 401 |
| 2003 | Token 无效/过期 | 401 |
| 2004 | 权限不足 | 403 |
| 3001 | 课程已满员 | 409 |
| 3002 | 已预约此课程 | 409 |
| 3003 | 无有效会员卡 | 403 |
| 3004 | 已在馆内 | 409 |
| 4001 | 申请已提交 | 409 |
| 4002 | 退款申请不存在 | 404 |
| 5001 | 系统繁忙 | 503 |
| 5002 | 数据库异常 | 500 |

---

**文档审批**

| 角色 | 姓名 | 日期 | 签字 |
| :--- | :--- | :---: | :--- |
| 产品经理 | ________ | ____/____/____ | ________ |
| 技术负责人 | ________ | ____/____/____ | ________ |
| 项目经理 | ________ | ____/____/____ | ________ |

---

*本文档为 Star GYM 项目最终需求文档，所有后续开发、测试、验收均以此为准。如有变更，需走正式变更流程并更新文档版本。*
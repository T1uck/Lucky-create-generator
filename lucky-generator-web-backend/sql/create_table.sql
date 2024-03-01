# 数据库初始化
-- 创建库
create database if not exists generator_db;

-- 切换库
use generator_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                             null comment '密码',
    email          varchar(256)                           null comment '邮箱',
    phone          varchar(32)                            null comment '手机号',
    accessKey      varchar(256)                           null comment 'accessKey',
    secretKey      varchar(256)                           null comment 'secretKey',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
    ) comment '用户' collate = utf8mb4_unicode_ci;

-- 代码生成器表
create table if not exists generator
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(128)                       null comment '名称',
    description text                               null comment '描述',
    basePackage varchar(128)                       null comment '基础包',
    version     varchar(128)                       null comment '版本',
    author      varchar(128)                       null comment '作者',
    tags        varchar(1024)                      null comment '标签列表（json 数组）',
    picture     varchar(256)                       null comment '图片',
    fileConfig  text                               null comment '文件配置（json字符串）',
    modelConfig text                               null comment '模型配置（json字符串）',
    distPath    text                               null comment '代码生成器产物路径',
    status      int      default 0                 not null comment '状态',
    userId      bigint                             not null comment '创建用户 id',
    view        bigint   default 0                 comment '访问量',
    like        bigint   default 0                 comment '点赞数',
    star        bigint   default 0                 comment '收藏数',
    comment     bigint   default 0                 comment '评论数',
    score       int      default 0                 comment '项目的总分数，是根据浏览、点赞、收藏、评论数计算得来的',
    hot         int      default 0                 comment '项目的热度，有一个定时任务，每小时计算增加的score',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
    ) comment '代码生成器' collate = utf8mb4_unicode_ci;

-- 生成器评论表
create table if not exists generator_comment
(
    id              bigint          auto_increment          not null comment '评论id' primary key,
    generatorId     bigint                                  not null comment '生成器id',
    rootId          bigint          default 0               not null comment '根评论id，-1代表是根评论',
    content         varchar(4096)   default                 null comment '评论内容',
    fromId          bigint                                  not null comment '评论者id',
    toId            bigint          default                 null comment '被评论者id',
    toCommentId     bigint          default                 null comment '这条评论是回复那条评论的，只有子评论才有（子评论的子评论，树形）',
    likeComment     int             default 0               comment '评论点赞数',
    createTime      datetime        default CURRENT_TIMESTAMP not null comment '创建时间',
    isDelete        tinyint         default 0               not null comment '是否删除'
) comment '生成器评论表' collate = utf8mb4_unicode_ci;

-- 生成器点赞表
create table if not exists generator_like
(
    id              bigint          auto_increment          not null comment 'id' primary key,
    generatorId     bigint                                  not null comment '生成器id',
    createBy        bigint                                  not null comment '创建用户id',
    createTime      datetime        default CURRENT_TIMESTAMP not null comment '创建时间'
) comment '生成器点赞表' collate = utf8mb4_unicode_ci;

-- 生成器收藏表
create table if not exists generator_star
(
    id              bigint          auto_increment          not null comment 'id' primary key,
    generatorId     bigint                                  not null comment '生成器id',
    createBy        bigint                                  not null comment '创建用户id',
    bookId          bigint                                  not null comment '收藏夹id',
    createTime      datetime        default CURRENT_TIMESTAMP not null comment '创建时间'
) comment '生成器收藏表' collate = utf8mb4_unicode_ci;

-- 用户收藏名册
create table if not exists star_book
(
    id              bigint          auto_increment          not null comment 'id' primary key,
    name            varchar(128)                            not null comment '收藏夹名称',
    createBy        bigint                                  not null comment '创建用户id',
    count           int             default 0               comment '收藏夹内文件数',
    createTime      datetime        default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint         default 0               not null comment '是否删除'
) comment '用户收藏夹' collate = utf8mb4_unicode_ci;

-- 模拟用户数据
INSERT INTO generator_db.user (id, userAccount, userPassword, userName, userAvatar, userProfile, userRole) VALUES (1, 'luckyone', 'b0dd3697a192885d7c055db46155b26a', '管理员', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我有一头小毛驴我从来也不骑', 'admin');
INSERT INTO generator_db.user (id, userAccount, userPassword, userName, userAvatar, userProfile, userRole) VALUES (2, 'happy', 'b0dd3697a192885d7c055db46155b26a', '普通用户', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', '我有一头小毛驴我从来也不骑', 'user');

-- 模拟代码生成器数据
INSERT INTO generator_db.generator (id, name, description, basePackage, version, author, tags, picture, fileConfig, modelConfig, distPath, status, userId) VALUES (1, 'ACM 模板项目', 'ACM 模板项目生成器', 'com.luckyone', '1.0', '管理员', '["Java"]', 'https://pic.yupi.icu/1/_r0_c1851-bf115939332e.jpg', '{}', '{}', null, 0, 1);
INSERT INTO generator_db.generator (id, name, description, basePackage, version, author, tags, picture, fileConfig, modelConfig, distPath, status, userId) VALUES (2, 'Spring Boot 初始化模板', 'Spring Boot 初始化模板项目生成器', 'com.luckyone', '1.0', '管理员', '["Java"]', 'https://pic.yupi.icu/1/_r0_c0726-7e30f8db802a.jpg', '{}', '{}', null, 0, 1);
INSERT INTO generator_db.generator (id, name, description, basePackage, version, author, tags, picture, fileConfig, modelConfig, distPath, status, userId) VALUES (3, 'lucky外卖', 'lucky外卖项目生成器', 'com.luckyone', '1.0', '管理员', '["Java", "前端"]', 'https://pic.yupi.icu/1/_r1_c0cf7-f8e4bd865b4b.jpg', '{}', '{}', null, 0, 1);
INSERT INTO generator_db.generator (id, name, description, basePackage, version, author, tags, picture, fileConfig, modelConfig, distPath, status, userId) VALUES (4, 'lucky用户中心', 'lucky用户中心项目生成器', 'com.luckyone', '1.0', '管理员', '["Java", "前端"]', 'https://pic.yupi.icu/1/_r1_c1c15-79cdecf24aed.jpg', '{}', '{}', null, 0, 1);
INSERT INTO generator_db.generator (id, name, description, basePackage, version, author, tags, picture, fileConfig, modelConfig, distPath, status, userId) VALUES (5, 'lucky商城', 'lucky商城项目生成器', 'com.luckyone', '1.0', '管理员', '["Java", "前端"]', 'https://pic.yupi.icu/1/_r1_c0709-8e80689ac1da.jpg', '{}', '{}', null, 0, 1);
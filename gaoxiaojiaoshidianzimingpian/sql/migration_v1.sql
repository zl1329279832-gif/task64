-- ============================================================
-- 名片版本化改造 - 数据库迁移脚本
-- ============================================================

-- 1. 扩展 shangxia_types 字典值
INSERT INTO dictionary (dic_code, dic_name, code_index, index_name) VALUES
('shangxia_types', '是否展示', 2, '待审核'),
('shangxia_types', '是否展示', 3, '审核驳回'),
('shangxia_types', '是否展示', 4, '已归档');

-- 2. mingpian 表新增版本号字段
ALTER TABLE mingpian
    ADD COLUMN version_number INT NOT NULL DEFAULT 1
    COMMENT '当前版本号' AFTER mingpian_content;

-- 3. 新建名片版本历史表
CREATE TABLE `mingpian_history` (
  `id`                     INT(11)      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `mingpian_id`            INT(11)      NOT NULL COMMENT '原名片ID',
  `jiaoshi_id`             INT(11)      NOT NULL COMMENT '教师ID',
  `version_number`         INT(11)      NOT NULL DEFAULT 1 COMMENT '版本号',
  `mingpian_name`          VARCHAR(200) DEFAULT NULL COMMENT '名片名称',
  `mingpian_uuid_number`   VARCHAR(200) DEFAULT NULL COMMENT '名片编号',
  `mingpian_xingming`      VARCHAR(200) DEFAULT NULL COMMENT '姓名',
  `mingpian_phone`         VARCHAR(200) DEFAULT NULL COMMENT '联系电话',
  `mingpian_file`          VARCHAR(500) DEFAULT NULL COMMENT '名片文件',
  `sex_types`              INT(11)      DEFAULT NULL COMMENT '性别',
  `zhiwu_types`            INT(11)      DEFAULT NULL COMMENT '职务',
  `mingpian_photo`         VARCHAR(500) DEFAULT NULL COMMENT '名片照片',
  `mingpian_types`         INT(11)      DEFAULT NULL COMMENT '名片类型',
  `xueyuan_types`          INT(11)      DEFAULT NULL COMMENT '学院',
  `bangongshi_types`       INT(11)      DEFAULT NULL COMMENT '办公室',
  `kecheng_types`          INT(11)      DEFAULT NULL COMMENT '主修课程',
  `mingpian_clicknum`      INT(11)      DEFAULT 0    COMMENT '该版本点击数快照',
  `mingpian_content`       TEXT         DEFAULT NULL COMMENT '名片详细介绍',
  `shangxia_types`         INT(11)      DEFAULT NULL COMMENT '快照时的审批状态',
  `snapshot_time`          DATETIME     NOT NULL COMMENT '快照时间',
  `snapshot_reason`        VARCHAR(500) DEFAULT NULL COMMENT '快照原因',
  `create_time`            DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_mingpian_id` (`mingpian_id`),
  KEY `idx_jiaoshi_id` (`jiaoshi_id`),
  KEY `idx_version` (`mingpian_id`, `version_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='名片版本历史';

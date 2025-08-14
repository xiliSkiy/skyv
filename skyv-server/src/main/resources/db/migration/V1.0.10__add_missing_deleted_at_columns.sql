-- ========================================
-- SkyEye 智能监控系统 - 添加缺失的 deleted_at 列
-- 版本: V1.0.10
-- 创建时间: 2025-08-14
-- 描述: 为现有表添加软删除支持的 deleted_at 列
-- ========================================

-- 为 tb_device_areas 表添加 deleted_at 列
ALTER TABLE tb_device_areas 
ADD COLUMN deleted_at TIMESTAMP NULL;

-- 为 tb_device_tags 表添加 deleted_at 列
ALTER TABLE tb_device_tags 
ADD COLUMN deleted_at TIMESTAMP NULL;

-- 创建 deleted_at 索引以提高查询性能
CREATE INDEX idx_device_areas_deleted_at ON tb_device_areas (deleted_at);
CREATE INDEX idx_device_tags_deleted_at ON tb_device_tags (deleted_at);

-- 添加注释
COMMENT ON COLUMN tb_device_areas.deleted_at IS '软删除时间戳';
COMMENT ON COLUMN tb_device_tags.deleted_at IS '软删除时间戳'; 
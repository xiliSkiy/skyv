-- 添加生效时间和失效时间字段到采集任务表
-- 版本: V1.1
-- 描述: 为采集任务表添加生效时间和失效时间字段，支持任务时间范围控制

-- 添加生效时间字段
ALTER TABLE tb_collection_tasks 
ADD COLUMN effective_time TIMESTAMP NULL ;

-- 添加失效时间字段
ALTER TABLE tb_collection_tasks 
ADD COLUMN expire_time TIMESTAMP NULL;

-- 添加索引以提高查询性能
CREATE INDEX idx_collection_tasks_effective_time ON tb_collection_tasks(effective_time);
CREATE INDEX idx_collection_tasks_expire_time ON tb_collection_tasks(expire_time);

-- 添加注释
COMMENT ON COLUMN tb_collection_tasks.effective_time IS '任务生效时间，任务在此时间之后才会开始执行';
COMMENT ON COLUMN tb_collection_tasks.expire_time IS '任务失效时间，任务在此时间之后将停止执行';

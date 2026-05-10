-- ============================================
-- 彻底删除所有 Flowable 相关表（act_ 和 flw_ 开头）
-- ============================================

-- 注意：执行前请确保备份了重要数据！

DO $$ 
DECLARE
    r RECORD;
BEGIN
    -- 禁用触发器
    EXECUTE 'SET session_replication_role = ''replica''';
    
    -- 1. 删除所有 act_ 开头的表
    RAISE NOTICE 'Dropping all act_ tables...';
    FOR r IN 
        SELECT tablename FROM pg_tables 
        WHERE schemaname = 'public' AND tablename LIKE 'act_%'
    LOOP
        RAISE NOTICE 'Dropping table: %', r.tablename;
        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';
    END LOOP;
    
    -- 2. 删除所有 flw_ 开头的表
    RAISE NOTICE 'Dropping all flw_ tables...';
    FOR r IN 
        SELECT tablename FROM pg_tables 
        WHERE schemaname = 'public' AND tablename LIKE 'flw_%'
    LOOP
        RAISE NOTICE 'Dropping table: %', r.tablename;
        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';
    END LOOP;
    
    -- 重新启用触发器
    EXECUTE 'SET session_replication_role = ''origin''';
    
    RAISE NOTICE 'All Flowable tables (act_ and flw_) have been dropped!';
END $$;

-- ============================================
-- 执行完成后，重新启动后端！
-- Flowable 会自动创建全新的表结构
-- ============================================

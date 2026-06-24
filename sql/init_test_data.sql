-- ============================================
-- 健身房私教预约系统 - 测试数据初始化脚本
-- 功能：清空所有表数据，插入测试数据
-- 密码：所有用户密码均为 123456 (BCrypt加密)
-- 执行方式：在 MySQL 客户端中 source 此文件
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 1. 清空所有表 ====================
TRUNCATE TABLE tb_consumption_record;
TRUNCATE TABLE tb_check_in;
TRUNCATE TABLE tb_booking;
TRUNCATE TABLE tb_recharge_record;
TRUNCATE TABLE tb_schedule;
TRUNCATE TABLE tb_course;
TRUNCATE TABLE tb_member;
TRUNCATE TABLE tb_trainer;
TRUNCATE TABLE tb_user;

-- ==================== 2. 插入系统用户 (tb_user) ====================
-- 密码均为 123456 的 BCrypt 加密值
INSERT INTO tb_user (id, username, password, real_name, phone, role, avatar, status) VALUES
(1, 'admin',         '$2b$10$6kBycmOeX5NslTXum27VLeEoaO8A5cODKy15M4nKmwyWcSPv5iJHu', '系统管理员', '13800000001', 'FRONT_DESK', NULL, 1),
(2, 'finance',       '$2b$10$6kBycmOeX5NslTXum27VLeEoaO8A5cODKy15M4nKmwyWcSPv5iJHu', '财务人员',   '13800000002', 'FINANCE',     NULL, 1),
(3, 'zhangsan',      '$2b$10$6kBycmOeX5NslTXum27VLeEoaO8A5cODKy15M4nKmwyWcSPv5iJHu', '张明',       '13900000001', 'MEMBER',      NULL, 1),
(4, 'lisi',          '$2b$10$6kBycmOeX5NslTXum27VLeEoaO8A5cODKy15M4nKmwyWcSPv5iJHu', '李华',       '13900000002', 'MEMBER',      NULL, 1),
(5, 'wangwu',        '$2b$10$6kBycmOeX5NslTXum27VLeEoaO8A5cODKy15M4nKmwyWcSPv5iJHu', '王芳',       '13900000003', 'MEMBER',      NULL, 1),
(6, 'chenjiaolian',  '$2b$10$6kBycmOeX5NslTXum27VLeEoaO8A5cODKy15M4nKmwyWcSPv5iJHu', '陈教练',     '13800000003', 'TRAINER',     NULL, 1),
(7, 'linjiaolian',   '$2b$10$6kBycmOeX5NslTXum27VLeEoaO8A5cODKy15M4nKmwyWcSPv5iJHu', '林教练',     '13800000004', 'TRAINER',     NULL, 1);

-- ==================== 3. 插入会员信息 (tb_member) ====================
INSERT INTO tb_member (id, user_id, real_name, phone, gender, birthday, member_level, remaining_hours, member_expire_date, total_recharge) VALUES
(1, 3, '张明', '13900000001', '男', '1990-05-15', '金卡会员', 30, '2026-12-31', 3000.00),
(2, 4, '李华', '13900000002', '女', '1995-08-20', '银卡会员', 15, '2026-09-30', 1500.00),
(3, 5, '王芳', '13900000003', '女', '1992-03-10', '普通会员', 5,  '2026-07-31', 500.00);

-- ==================== 4. 插入教练信息 (tb_trainer) ====================
INSERT INTO tb_trainer (id, user_id, real_name, phone, gender, age, specialties, price_per_hour, experience_years, certifications, description, status) VALUES
(1, 6, '陈教练', '13800000003', '男', 30, '力量训练,增肌,减脂', 300.00, 8, 'NSCA-CPT认证,ACE认证', '10年健身经验，擅长力量训练和体型塑造，帮助数百名学员达成健身目标', 1),
(2, 7, '林教练', '13800000004', '女', 28, '瑜伽,普拉提,拉伸', 280.00, 5, '全美瑜伽联盟RYT500', '专注瑜伽教学5年，擅长哈他瑜伽和流瑜伽，帮助学员改善体态和柔韧性', 1);

-- ==================== 5. 插入课程 (tb_course) ====================
INSERT INTO tb_course (id, course_name, course_type, trainer_id, max_students, duration_minutes, price_per_session, description, status) VALUES
(1, '一对一增肌训练', 'PRIVATE', 1, 1,  60,  300.00, '个性化增肌训练方案，针对不同体型制定专属训练计划，适合各阶段健身爱好者', 1),
(2, '减脂塑形私教',   'PRIVATE', 1, 1,  60,  300.00, '科学减脂不反弹，高效塑形，一对一指导饮食与运动结合方案', 1),
(3, '瑜伽小班课',     'GROUP',   2, 10, 90,  150.00, '哈他瑜伽与流瑜伽结合课程，包含呼吸法和冥想，适合初学者和进阶者', 1),
(4, '普拉提团体课',   'GROUP',   2, 8,  60,  150.00, '核心力量训练，改善体态和身体控制力，适合久坐办公人群', 1);

-- ==================== 6. 插入排课 (tb_schedule) ====================
-- 日期基于 2026-06-18 开始（当前日期约 2026-06-17）
INSERT INTO tb_schedule (id, trainer_id, schedule_date, start_time, end_time, max_bookings, booked_count, status) VALUES
-- 陈教练(教练1) - 私教课排班
(1, 1, '2026-06-18', '09:00:00', '10:00:00', 1, 1, 1),
(2, 1, '2026-06-18', '10:00:00', '11:00:00', 1, 0, 1),
(3, 1, '2026-06-18', '14:00:00', '15:00:00', 1, 0, 1),
(4, 1, '2026-06-19', '09:00:00', '10:00:00', 1, 0, 1),
(5, 1, '2026-06-19', '15:00:00', '16:00:00', 1, 0, 1),
-- 林教练(教练2) - 团体课排班
(6, 2, '2026-06-18', '09:00:00', '10:30:00', 10, 1, 1),
(7, 2, '2026-06-18', '14:00:00', '15:00:00', 8,  0, 1),
(8, 2, '2026-06-19', '10:00:00', '11:30:00', 10, 0, 1);

-- ==================== 7. 插入预约 (tb_booking) ====================
INSERT INTO tb_booking (id, order_no, member_id, trainer_id, course_id, schedule_id, booking_date, start_time, end_time, hours, amount, status, remark) VALUES
-- 张明 预约了陈教练的增肌训练（已确认）
(1, 'BK20260617001', 1, 1, 1, 1, '2026-06-18', '09:00:00', '10:00:00', 1, 300.00, 1, NULL),
-- 李华 预约了林教练的瑜伽小班课（待确认）
(2, 'BK20260617002', 2, 2, 3, 6, '2026-06-18', '09:00:00', '10:30:00', 1, 150.00, 0, NULL);

-- ==================== 8. 插入签到记录 (tb_check_in) ====================
INSERT INTO tb_check_in (id, booking_id, member_id, trainer_id, course_id, check_in_time, hours_consumed, status) VALUES
(1, 1, 1, 1, 1, '2026-06-17 09:30:00', 1, 1);

-- ==================== 9. 插入消费记录 (tb_consumption_record) ====================
INSERT INTO tb_consumption_record (id, member_id, booking_id, trainer_id, course_id, hours_consumed, amount, record_type, remark) VALUES
(1, 1, 1, 1, 1, 1, 300.00, 'BOOKING', '预约增肌训练消费');

-- ==================== 10. 插入充值记录 (tb_recharge_record) ====================
INSERT INTO tb_recharge_record (id, member_id, amount, hours, pay_method, operator_id, remark) VALUES
(1, 1, 3000.00, 30, '微信支付', 1, '首次充值办理金卡会员，赠送额外课时'),
(2, 2, 1500.00, 15, '支付宝',   1, '银卡会员充值'),
(3, 3,  500.00,  5, '微信支付', 1, '普通会员体验充值');

SET FOREIGN_KEY_CHECKS = 1;

-- ==================== 完成 ====================
-- 登录账号一览：
-- 管理员(前台): admin / 123456
-- 财务人员:     finance / 123456
-- 会员张明:     zhangsan / 123456    金卡会员 30课时
-- 会员李华:     lisi / 123456        银卡会员 15课时
-- 会员王芳:     wangwu / 123456      普通会员 5课时
-- 教练陈教练:   chenjiaolian / 123456  力量训练 ¥300/时
-- 教练林教练:   linjiaolian / 123456   瑜伽普拉提 ¥280/时

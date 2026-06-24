-- 健身房私教预约管理系统 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS gym_booking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gym_booking;

-- ============================================================
-- 用户表
-- ============================================================
DROP TABLE IF EXISTS tb_user;
CREATE TABLE tb_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: MEMBER-会员, TRAINER-教练, FRONT_DESK-前台, FINANCE-财务',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 默认管理员账号: admin / admin123
INSERT INTO tb_user (username, password, real_name, phone, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 'FRONT_DESK', 1),
('finance', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '财务管理员', '13800000001', 'FINANCE', 1),
('trainer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张教练', '13800000002', 'TRAINER', 1),
('trainer02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李教练', '13800000003', 'TRAINER', 1),
('member01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王会员', '13800000004', 'MEMBER', 1),
('member02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵会员', '13800000005', 'MEMBER', 1);

-- ============================================================
-- 会员信息表
-- ============================================================
DROP TABLE IF EXISTS tb_member;
CREATE TABLE tb_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '关联用户ID',
    real_name VARCHAR(50) COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    gender VARCHAR(10) COMMENT '性别',
    birthday DATE COMMENT '生日',
    member_level VARCHAR(20) DEFAULT '普通会员' COMMENT '会员等级: 普通会员,银卡会员,金卡会员,钻石会员',
    remaining_hours INT DEFAULT 0 COMMENT '剩余课时',
    member_expire_date DATE COMMENT '会员到期日',
    total_recharge DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计充值金额',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员信息表';

INSERT INTO tb_member (user_id, real_name, phone, gender, birthday, member_level, remaining_hours, member_expire_date) VALUES
(5, '王会员', '13800000004', '男', '1995-06-15', '金卡会员', 30, '2027-06-15'),
(6, '赵会员', '13800000005', '女', '1998-03-20', '银卡会员', 15, '2027-03-20');

-- ============================================================
-- 私教信息表
-- ============================================================
DROP TABLE IF EXISTS tb_trainer;
CREATE TABLE tb_trainer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '关联用户ID',
    real_name VARCHAR(50) COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    avatar VARCHAR(500) COMMENT '头像URL',
    specialties VARCHAR(500) COMMENT '擅长项目(逗号分隔)',
    price_per_hour DECIMAL(10,2) DEFAULT 0.00 COMMENT '每小时价格',
    description TEXT COMMENT '个人简介',
    certifications VARCHAR(500) COMMENT '证书资质',
    experience_years INT DEFAULT 0 COMMENT '从业年限',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-在职, 0-离职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私教信息表';

INSERT INTO tb_trainer (user_id, real_name, phone, gender, age, specialties, price_per_hour, description, certifications, experience_years, status) VALUES
(3, '张教练', '13800000002', '男', 32, '增肌训练,减脂塑形,体能训练', 300.00, '国家一级健身教练，10年执教经验，擅长增肌和体态矫正', '国家一级健身教练证,NSCA-CPT', 10, 1),
(4, '李教练', '13800000003', '女', 28, '瑜伽,普拉提,产后恢复,体态矫正', 280.00, '高级瑜伽导师，专注女性塑形和产后恢复', '高级瑜伽导师证,普拉提认证', 6, 1);

-- ============================================================
-- 课程表
-- ============================================================
DROP TABLE IF EXISTS tb_course;
CREATE TABLE tb_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_type VARCHAR(20) NOT NULL COMMENT '课程类型: PRIVATE-私教课, GROUP-团体课',
    trainer_id BIGINT COMMENT '教练ID',
    max_students INT DEFAULT 1 COMMENT '最大人数',
    duration_minutes INT DEFAULT 60 COMMENT '时长(分钟)',
    price_per_session DECIMAL(10,2) DEFAULT 0.00 COMMENT '每节价格',
    description TEXT COMMENT '课程描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-上架, 0-下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_trainer (trainer_id),
    INDEX idx_type (course_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

INSERT INTO tb_course (course_name, course_type, trainer_id, max_students, duration_minutes, price_per_session, description, status) VALUES
('一对一增肌私教', 'PRIVATE', 1, 1, 60, 300.00, '个性化增肌训练方案，一对一指导', 1),
('一对一减脂私教', 'PRIVATE', 1, 1, 60, 300.00, '科学减脂方案，饮食+训练结合', 1),
('瑜伽私教课', 'PRIVATE', 2, 1, 60, 280.00, '一对一瑜伽指导，适合各水平学员', 1),
('产后恢复训练', 'PRIVATE', 2, 1, 60, 280.00, '专业产后恢复指导，安全有效', 1),
('团体瑜伽课', 'GROUP', 2, 15, 60, 88.00, '小班瑜伽课，放松身心', 1),
('团体体能训练', 'GROUP', 1, 20, 60, 68.00, '团体体能训练，燃脂塑形', 1);

-- ============================================================
-- 教练排班表
-- ============================================================
DROP TABLE IF EXISTS tb_schedule;
CREATE TABLE tb_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trainer_id BIGINT NOT NULL COMMENT '教练ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    max_bookings INT DEFAULT 1 COMMENT '该时段最大预约数',
    booked_count INT DEFAULT 0 COMMENT '已预约数',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-可预约, 0-已满, 2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_trainer_date (trainer_id, schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练排班表';

-- ============================================================
-- 预约订单表
-- ============================================================
DROP TABLE IF EXISTS tb_booking;
CREATE TABLE tb_booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    trainer_id BIGINT NOT NULL COMMENT '教练ID',
    course_id BIGINT COMMENT '课程ID',
    schedule_id BIGINT COMMENT '排班ID',
    booking_date DATE COMMENT '预约日期',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    hours INT DEFAULT 1 COMMENT '消耗课时',
    amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '金额',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待确认, 1-已确认, 2-已完成, 3-已取消',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_member (member_id),
    INDEX idx_trainer (trainer_id),
    INDEX idx_date (booking_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- ============================================================
-- 签到记录表
-- ============================================================
DROP TABLE IF EXISTS tb_check_in;
CREATE TABLE tb_check_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL COMMENT '预约ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    trainer_id BIGINT COMMENT '教练ID',
    course_id BIGINT COMMENT '课程ID',
    check_in_time DATETIME COMMENT '签到时间',
    hours_consumed INT DEFAULT 1 COMMENT '消耗课时',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-已签到, 2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_booking (booking_id),
    INDEX idx_member (member_id),
    INDEX idx_trainer (trainer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- ============================================================
-- 消费记录表
-- ============================================================
DROP TABLE IF EXISTS tb_consumption_record;
CREATE TABLE tb_consumption_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '会员ID',
    booking_id BIGINT COMMENT '预约ID',
    trainer_id BIGINT COMMENT '教练ID',
    course_id BIGINT COMMENT '课程ID',
    hours_consumed INT DEFAULT 1 COMMENT '消耗课时',
    amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '消费金额',
    record_type VARCHAR(20) DEFAULT 'BOOKING' COMMENT '类型: BOOKING-预约消费, SIGN_IN-签到扣减',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_member (member_id),
    INDEX idx_type (record_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';

-- ============================================================
-- 充值记录表
-- ============================================================
DROP TABLE IF EXISTS tb_recharge_record;
CREATE TABLE tb_recharge_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL COMMENT '会员ID',
    amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '充值金额',
    hours INT DEFAULT 0 COMMENT '充值课时数',
    pay_method VARCHAR(50) COMMENT '支付方式',
    operator_id BIGINT COMMENT '操作员ID',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_member (member_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

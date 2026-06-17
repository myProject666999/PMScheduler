CREATE DATABASE IF NOT EXISTS pm_scheduler DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pm_scheduler;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'ADMIN/LEADER/WORKER',
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(100),
    production_line VARCHAR(100),
    install_date DATE,
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/MAINTENANCE/STOPPED',
    current_runtime DECIMAL(12,1) NOT NULL DEFAULT 0 COMMENT '当前运行时数(小时)',
    last_maintenance_runtime DECIMAL(12,1) NOT NULL DEFAULT 0 COMMENT '上次保养时运行时数',
    last_maintenance_date DATE COMMENT '上次保养日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='设备主数据表';

CREATE TABLE IF NOT EXISTS maintenance_standard (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL COMMENT '保养项目名称',
    `trigger_type` VARCHAR(20) NOT NULL COMMENT 'CALENDAR/RUNTIME',
    cycle_days INT COMMENT '日历周期(天)',
    cycle_hours DECIMAL(10,1) COMMENT '运行时数周期(小时)',
    remind_days_before INT DEFAULT 3 COMMENT '提前提醒天数',
    remind_hours_before DECIMAL(10,1) DEFAULT 50 COMMENT '提前提醒时数',
    content TEXT COMMENT '保养内容描述',
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_trigger_type (trigger_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB COMMENT='保养标准表';

CREATE TABLE IF NOT EXISTS work_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    equipment_id BIGINT NOT NULL,
    standard_id BIGINT,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DISPATCHED/EXECUTING/REVIEWING/COMPLETED/REJECTED',
    dispatch_user_id BIGINT COMMENT '派工人ID',
    execute_user_id BIGINT COMMENT '执行人ID',
    plan_date DATE COMMENT '计划完成日期',
    actual_date DATE COMMENT '实际完成日期',
    actual_hours DECIMAL(6,1) COMMENT '实际工时',
    maintenance_content TEXT COMMENT '保养内容',
    trigger_type VARCHAR(20) COMMENT '触发类型: CALENDAR/RUNTIME',
    dispatch_at DATETIME COMMENT '派工时间',
    execute_at DATETIME COMMENT '开始执行时间',
    complete_at DATETIME COMMENT '完成时间',
    remark TEXT COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_status (status),
    INDEX idx_execute_user_id (execute_user_id),
    INDEX idx_plan_date (plan_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='保养工单表';

CREATE TABLE IF NOT EXISTS work_order_part (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    part_name VARCHAR(200) NOT NULL,
    part_code VARCHAR(50),
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit VARCHAR(20) DEFAULT '个',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_work_order_id (work_order_id)
) ENGINE=InnoDB COMMENT='工单更换零件表';

CREATE TABLE IF NOT EXISTS runtime_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    runtime_value DECIMAL(12,1) NOT NULL COMMENT '登记的运行时数值',
    increment_value DECIMAL(12,1) NOT NULL DEFAULT 0 COMMENT '增量值',
    operator_id BIGINT NOT NULL COMMENT '登记人ID',
    recorded_at DATETIME NOT NULL COMMENT '登记时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_recorded_at (recorded_at)
) ENGINE=InnoDB COMMENT='运行时数登记日志表';

INSERT INTO sys_user (username, password, real_name, role) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN'),
('leader01', 'e10adc3949ba59abbe56e057f20f883e', '张班长', 'LEADER'),
('worker01', 'e10adc3949ba59abbe56e057f20f883e', '李师傅', 'WORKER'),
('worker02', 'e10adc3949ba59abbe56e057f20f883e', '王师傅', 'WORKER');

INSERT INTO equipment (code, name, model, production_line, install_date, status, current_runtime, last_maintenance_runtime, last_maintenance_date) VALUES
('EQ-001', '空压机-A01', 'Atlas Copco GA37', '产线一', '2023-03-15', 'NORMAL', 3250.5, 2780.0, '2026-05-20'),
('EQ-002', '注塑机-B03', '海天MA3200', '产线二', '2022-08-20', 'NORMAL', 15680.0, 15200.0, '2026-06-01'),
('EQ-003', '数控车床-C05', '大连CK6150', '产线三', '2024-01-10', 'MAINTENANCE', 4280.0, 4000.0, '2026-06-10'),
('EQ-004', '冲压机-D02', 'AIDA NC1-2000', '产线一', '2023-06-01', 'NORMAL', 8900.0, 8400.0, '2026-05-28'),
('EQ-005', '焊接机器人-E01', 'FANUC ARC Mate 100iD', '产线二', '2024-05-20', 'NORMAL', 2100.0, 1800.0, '2026-05-15');

INSERT INTO maintenance_standard (equipment_id, item_name, trigger_type, cycle_days, cycle_hours, remind_days_before, remind_hours_before, content) VALUES
(1, '更换润滑油', 'RUNTIME', NULL, 500.0, NULL, 50.0, '检查油质，更换空压机专用润滑油，清洗油过滤器'),
(1, '安全阀检查', 'CALENDAR', 90, NULL, 7, NULL, '检查安全阀动作是否灵敏，校验压力设定值'),
(1, '皮带检查更换', 'CALENDAR', 180, NULL, 14, NULL, '检查传动皮带磨损和张力，必要时更换'),
(2, '螺杆保养', 'RUNTIME', NULL, 1000.0, NULL, 100.0, '拆检螺杆，测量间隙，必要时更换螺杆组件'),
(2, '液压油更换', 'CALENDAR', 180, NULL, 14, NULL, '更换液压油，清洗油箱和滤芯'),
(2, '加热圈检查', 'CALENDAR', 90, NULL, 7, NULL, '检查加热圈和热电偶，校验温度控制精度'),
(3, '导轨润滑', 'CALENDAR', 30, NULL, 3, NULL, '检查导轨润滑系统，补充或更换导轨润滑油'),
(3, '主轴保养', 'RUNTIME', NULL, 2000.0, NULL, 100.0, '检查主轴精度，测量跳动，更换润滑脂'),
(4, '离合器检查', 'CALENDAR', 60, NULL, 7, NULL, '检查离合器摩擦片磨损，调整间隙'),
(4, '润滑系统保养', 'RUNTIME', NULL, 800.0, NULL, 80.0, '检查集中润滑系统，更换润滑脂和滤芯'),
(5, '焊枪保养', 'CALENDAR', 45, NULL, 5, NULL, '清理焊枪喷嘴，检查导电嘴磨损，更换送丝轮'),
(5, '减速机保养', 'RUNTIME', NULL, 3000.0, NULL, 200.0, '检查减速机润滑油位和油质，测量各轴间隙');

INSERT INTO work_order (order_no, equipment_id, standard_id, status, dispatch_user_id, execute_user_id, plan_date, trigger_type, dispatch_at, execute_at, created_at) VALUES
('WO-20260610-001', 3, 7, 'EXECUTING', 2, 3, '2026-06-10', 'CALENDAR', '2026-06-08 09:00:00', '2026-06-10 08:30:00', '2026-06-07 08:00:00'),
('WO-20260612-001', 1, 1, 'DISPATCHED', 2, 3, '2026-06-18', 'RUNTIME', '2026-06-14 10:00:00', NULL, '2026-06-14 08:00:00'),
('WO-20260601-001', 5, 11, 'COMPLETED', 2, 4, '2026-06-01', 'CALENDAR', '2026-05-28 09:00:00', '2026-06-01 08:00:00', '2026-05-26 08:00:00');

INSERT INTO work_order_part (work_order_id, part_name, part_code, quantity, unit) VALUES
(3, '焊枪喷嘴', 'WJ-NZ-001', 2, '个'),
(3, '导电嘴', 'WJ-DZ-002', 4, '个'),
(3, '送丝轮', 'WJ-SL-003', 1, '个');

INSERT INTO runtime_log (equipment_id, runtime_value, increment_value, operator_id, recorded_at) VALUES
(1, 3100.0, 50.0, 3, '2026-06-01 09:00:00'),
(1, 3200.0, 100.0, 3, '2026-06-08 09:00:00'),
(1, 3250.5, 50.5, 3, '2026-06-15 09:00:00'),
(2, 15400.0, 80.0, 4, '2026-06-05 10:00:00'),
(2, 15680.0, 280.0, 4, '2026-06-13 10:00:00'),
(3, 4100.0, 60.0, 3, '2026-06-03 08:00:00'),
(3, 4200.0, 100.0, 3, '2026-06-10 08:00:00'),
(3, 4280.0, 80.0, 3, '2026-06-15 08:00:00');

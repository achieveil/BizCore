DROP DATABASE IF EXISTS bizcore;
CREATE DATABASE bizcore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bizcore;
DROP TABLE IF EXISTS department_managers;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS attendances;
DROP TABLE IF EXISTS department_attendance_rules;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS overtime_requests;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
    security_question VARCHAR(200),
    security_answer VARCHAR(200),
    status TINYINT DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL,
    dept_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    parent_id BIGINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    emp_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    birth_date DATE,
    id_card VARCHAR(18),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    dept_id BIGINT,
    position VARCHAR(100),
    level VARCHAR(50),
    hire_date DATE,
    salary DECIMAL(10, 2),
    address VARCHAR(200),
    status TINYINT DEFAULT 1,
    user_id BIGINT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_emp_dept (dept_id),
    INDEX idx_emp_user (user_id),
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES departments(id) ON DELETE
    SET NULL,
        CONSTRAINT fk_emp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE department_managers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_id BIGINT NOT NULL,
    manager_id BIGINT NOT NULL,
    assigned_by BIGINT,
    assigned_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dept_manager (dept_id, manager_id),
    CONSTRAINT fk_dm_dept FOREIGN KEY (dept_id) REFERENCES departments(id) ON DELETE CASCADE,
    CONSTRAINT fk_dm_emp FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_dm_assigner FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE
    SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE department_attendance_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_id BIGINT NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    late_grace_minutes INT DEFAULT 10,
    early_leave_grace_minutes INT DEFAULT 10,
    require_check_out BOOLEAN DEFAULT TRUE,
    flexible_work_time BOOLEAN DEFAULT FALSE,
    flexible_check_in_start TIME,
    flexible_check_in_end TIME,
    standard_work_hours DOUBLE DEFAULT 8.0,
    remark VARCHAR(500),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rule_dept (dept_id),
    CONSTRAINT fk_rule_dept FOREIGN KEY (dept_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE attendances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    emp_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    status VARCHAR(20) DEFAULT 'NORMAL',
    is_late BOOLEAN DEFAULT FALSE,
    late_minutes INT DEFAULT 0,
    is_early_leave BOOLEAN DEFAULT FALSE,
    early_leave_minutes INT DEFAULT 0,
    work_hours DOUBLE,
    dept_id BIGINT,
    remark VARCHAR(500),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_emp_date (emp_id, attendance_date),
    INDEX idx_att_dept (dept_id),
    CONSTRAINT fk_att_emp FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    emp_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days INT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    approver_id BIGINT,
    approval_comment VARCHAR(500),
    approval_time DATETIME,
    attachment_url VARCHAR(500),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_leave_emp (emp_id),
    CONSTRAINT fk_leave_emp FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE overtime_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    emp_id BIGINT NOT NULL,
    overtime_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    hours DOUBLE NOT NULL,
    overtime_type VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    approver_id BIGINT,
    approval_comment VARCHAR(500),
    approval_time DATETIME,
    compensated BOOLEAN DEFAULT FALSE,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ot_emp (emp_id),
    CONSTRAINT fk_ot_emp FOREIGN KEY (emp_id) REFERENCES employees(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE user_sessions (
    id CHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(128),
    refresh_token_hash VARCHAR(200) NOT NULL,
    user_agent VARCHAR(255),
    ip_address VARCHAR(64),
    expires_at DATETIME NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    last_used_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_session_user (user_id),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
INSERT INTO users (
        username,
        password,
        real_name,
        email,
        phone,
        role,
        security_question,
        security_answer
    )
VALUES (
        'admin',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '系统管理员',
        'admin@bizcore.com',
        '13900000000',
        'ADMIN',
        '最喜欢的颜色?',
        '蓝色'
    );
INSERT INTO departments (dept_name, dept_code, description)
VALUES ('人力资源部', 'D001', '负责招聘与员工关系管理'),
    ('财务部', 'D002', '管理公司财务与预算'),
    ('销售部', 'D003', '负责销售与客户拓展'),
    ('研发部', 'D004', '负责产品研发与技术创新'),
    ('运营部', 'D005', '负责日常运营与流程优化');
INSERT INTO department_attendance_rules (
        dept_id,
        check_in_time,
        check_out_time,
        late_grace_minutes,
        early_leave_grace_minutes,
        require_check_out,
        flexible_work_time,
        flexible_check_in_start,
        flexible_check_in_end,
        standard_work_hours,
        remark
    )
VALUES (
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D001'
        ),
        '09:00:00',
        '18:00:00',
        10,
        10,
        TRUE,
        TRUE,
        '08:50:00',
        '09:20:00',
        8.0,
        '人力资源：上午弹性20分钟，需打卡上下班'
    ),
    (
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D002'
        ),
        '08:45:00',
        '18:00:00',
        5,
        5,
        TRUE,
        FALSE,
        NULL,
        NULL,
        8.5,
        '财务：严格考勤，晚到超过5分钟记迟到'
    ),
    (
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D003'
        ),
        '09:30:00',
        '18:30:00',
        15,
        10,
        TRUE,
        TRUE,
        '09:00:00',
        '09:45:00',
        7.5,
        '销售：外勤较多，上午弹性45分钟'
    ),
    (
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D004'
        ),
        '09:30:00',
        '19:00:00',
        10,
        15,
        TRUE,
        TRUE,
        '09:00:00',
        '10:00:00',
        8.5,
        '研发：弹性制搭配核心工时10:30-18:30'
    ),
    (
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D005'
        ),
        '08:30:00',
        '18:00:00',
        8,
        10,
        TRUE,
        FALSE,
        NULL,
        NULL,
        8.0,
        '运营：需同步仓储作业，允许8分钟宽限'
    );
INSERT INTO users (
        username,
        password,
        real_name,
        email,
        phone,
        role,
        security_question,
        security_answer
    )
VALUES (
        'li.manager',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '李晨',
        'li.manager@bizcore.com',
        '13800000001',
        'MANAGER',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'zhang.finance',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '张帆',
        'zhang.finance@bizcore.com',
        '13800000002',
        'MANAGER',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'wang.sales',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '王楠',
        'wang.sales@bizcore.com',
        '13800000003',
        'MANAGER',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'chen.tech',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '陈嘉',
        'chen.tech@bizcore.com',
        '13800000004',
        'MANAGER',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'liu.ops',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '刘东',
        'liu.ops@bizcore.com',
        '13800000005',
        'MANAGER',
        '最喜欢的颜色?',
        '蓝色'
    );
INSERT INTO employees (
        emp_no,
        name,
        gender,
        dept_id,
        position,
        level,
        phone,
        email,
        hire_date,
        birth_date,
        salary,
        address,
        status,
        user_id
    )
VALUES (
        'M001',
        '李晨',
        '女',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D001'
        ),
        '人力资源经理',
        'M3',
        '13800000001',
        'li.manager@bizcore.com',
        '2018-03-10',
        '1987-05-01',
        18000,
        '北京市朝阳区望京街道',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'li.manager'
        )
    ),
    (
        'M002',
        '张帆',
        '男',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D002'
        ),
        '财务经理',
        'M3',
        '13800000002',
        'zhang.finance@bizcore.com',
        '2018-03-10',
        '1985-04-11',
        19000,
        '上海市浦东新区陆家嘴',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'zhang.finance'
        )
    ),
    (
        'M003',
        '王楠',
        '男',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D003'
        ),
        '销售经理',
        'M3',
        '13800000003',
        'wang.sales@bizcore.com',
        '2019-01-15',
        '1986-07-09',
        20000,
        '广州市天河区珠江新城',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'wang.sales'
        )
    ),
    (
        'M004',
        '陈嘉',
        '女',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D004'
        ),
        '研发经理',
        'M3',
        '13800000004',
        'chen.tech@bizcore.com',
        '2017-05-20',
        '1988-02-23',
        21000,
        '深圳市南山区科技园',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'chen.tech'
        )
    ),
    (
        'M005',
        '刘东',
        '男',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D005'
        ),
        '运营经理',
        'M3',
        '13800000005',
        'liu.ops@bizcore.com',
        '2016-09-01',
        '1984-11-30',
        18500,
        '杭州市西湖区文三路',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'liu.ops'
        )
    );
INSERT INTO department_managers (dept_id, manager_id, assigned_by, assigned_time)
SELECT d.id,
    e.id,
    (
        SELECT id
        FROM users
        WHERE username = 'admin'
    ),
    NOW()
FROM departments d
    JOIN employees e ON e.dept_id = d.id
    AND e.emp_no IN ('M001', 'M002', 'M003', 'M004', 'M005');
INSERT INTO users (
        username,
        password,
        real_name,
        email,
        phone,
        role,
        security_question,
        security_answer
    )
VALUES (
        'zhao.hr',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '赵颖',
        'zhao.d001@bizcore.com',
        '13700100011',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'zhou.hr',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '周可',
        'zhou.d001@bizcore.com',
        '13700100012',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'he.fi',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '何静',
        'he.d002@bizcore.com',
        '13700200011',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'luo.fi',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '罗毅',
        'luo.d002@bizcore.com',
        '13700200012',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'han.sa',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '韩梅',
        'han.d003@bizcore.com',
        '13700300011',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'cheng.sa',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '程一',
        'cheng.d003@bizcore.com',
        '13700300012',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'zou.rd',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '邹宁',
        'zou.d004@bizcore.com',
        '13700400011',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'lin.rd',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '林晨',
        'lin.d004@bizcore.com',
        '13700400012',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'ding.op',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '丁敏',
        'ding.d005@bizcore.com',
        '13700500011',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    ),
    (
        'wei.op',
        '$2b$10$NMC0U.SDmniJ7GvD4RJONexUdsi5W4UO9dOaRGujrz98GVYS5j2i6',
        '魏荣',
        'wei.d005@bizcore.com',
        '13700500012',
        'EMPLOYEE',
        '最喜欢的颜色?',
        '蓝色'
    );
INSERT INTO employees (
        emp_no,
        name,
        gender,
        dept_id,
        position,
        level,
        phone,
        email,
        hire_date,
        birth_date,
        salary,
        address,
        status,
        user_id
    )
VALUES (
        'E00101',
        '赵颖',
        '女',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D001'
        ),
        '员工专员',
        'E1',
        '13700100011',
        'zhao.d001@bizcore.com',
        '2020-02-12',
        '1992-08-12',
        11800,
        '上海市浦东新区世纪大道1288号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'zhao.hr'
        )
    ),
    (
        'E00102',
        '周可',
        '女',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D001'
        ),
        '资深专员',
        'E2',
        '13700100012',
        'zhou.d001@bizcore.com',
        '2020-05-28',
        '1990-10-01',
        12300,
        '上海市静安区南京西路800号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'zhou.hr'
        )
    ),
    (
        'E00201',
        '何静',
        '女',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D002'
        ),
        '员工专员',
        'E1',
        '13700200011',
        'he.d002@bizcore.com',
        '2021-03-19',
        '1991-04-06',
        12600,
        '北京市东城区东长安街1号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'he.fi'
        )
    ),
    (
        'E00202',
        '罗毅',
        '男',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D002'
        ),
        '资深专员',
        'E2',
        '13700200012',
        'luo.d002@bizcore.com',
        '2021-10-05',
        '1989-12-02',
        13200,
        '北京市海淀区知春路56号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'luo.fi'
        )
    ),
    (
        'E00301',
        '韩梅',
        '女',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D003'
        ),
        '员工专员',
        'E1',
        '13700300011',
        'han.d003@bizcore.com',
        '2022-01-11',
        '1993-06-18',
        11850,
        '广州市天河区体育东路138号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'han.sa'
        )
    ),
    (
        'E00302',
        '程一',
        '男',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D003'
        ),
        '资深专员',
        'E2',
        '13700300012',
        'cheng.d003@bizcore.com',
        '2022-04-21',
        '1990-09-09',
        13000,
        '广州市越秀区北京路120号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'cheng.sa'
        )
    ),
    (
        'E00401',
        '邹宁',
        '女',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D004'
        ),
        '高级工程师',
        'E3',
        '13700400011',
        'zou.d004@bizcore.com',
        '2023-02-03',
        '1988-11-25',
        14200,
        '深圳市南山区科技南十二路88号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'zou.rd'
        )
    ),
    (
        'E00402',
        '林晨',
        '男',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D004'
        ),
        '资深工程师',
        'E3',
        '13700400012',
        'lin.d004@bizcore.com',
        '2023-07-18',
        '1987-03-17',
        14500,
        '深圳市宝安区创业二路66号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'lin.rd'
        )
    ),
    (
        'E00501',
        '丁敏',
        '女',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D005'
        ),
        '运营主管',
        'E2',
        '13700500011',
        'ding.d005@bizcore.com',
        '2024-01-15',
        '1994-02-02',
        11000,
        '杭州市西湖区学院路102号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'ding.op'
        )
    ),
    (
        'E00502',
        '魏荣',
        '男',
        (
            SELECT id
            FROM departments
            WHERE dept_code = 'D005'
        ),
        '流程优化专员',
        'E1',
        '13700500012',
        'wei.d005@bizcore.com',
        '2024-05-06',
        '1986-07-30',
        12800,
        '杭州市滨江区江南大道588号',
        1,
        (
            SELECT id
            FROM users
            WHERE username = 'wei.op'
        )
    );
INSERT INTO attendances (
        emp_id,
        attendance_date,
        check_in_time,
        check_out_time,
        status,
        is_late,
        late_minutes,
        is_early_leave,
        early_leave_minutes,
        work_hours,
        dept_id,
        remark
    )
SELECT e.id,
    t.attendance_date,
    t.check_in_time,
    t.check_out_time,
    t.status,
    t.is_late,
    t.late_minutes,
    t.is_early_leave,
    t.early_leave_minutes,
    t.work_hours,
    e.dept_id,
    t.remark
FROM employees e
    JOIN (
        SELECT 'E00501' AS emp_no,
            '2024-05-13' AS attendance_date,
            '09:02:00' AS check_in_time,
            '18:10:00' AS check_out_time,
            'NORMAL' AS status,
            TRUE AS is_late,
            2 AS late_minutes,
            FALSE AS is_early_leave,
            0 AS early_leave_minutes,
            8.13 AS work_hours,
            '上午交通稍堵' AS remark
        UNION ALL
        SELECT 'E00501',
            '2024-05-14',
            '08:55:00',
            '18:05:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.17,
            '准时完成日报'
        UNION ALL
        SELECT 'E00502',
            '2024-05-13',
            '09:15:00',
            '18:30:00',
            'LATE',
            TRUE,
            15,
            FALSE,
            0,
            8.25,
            '处理客户紧急需求'
        UNION ALL
        SELECT 'E00302',
            '2024-05-13',
            '09:05:00',
            '17:35:00',
            'EARLY_LEAVE',
            TRUE,
            5,
            TRUE,
            25,
            7.50,
            '下午出差提前离岗'
        UNION ALL
        SELECT 'E00201',
            '2024-05-13',
            NULL,
            NULL,
            'LEAVE',
            FALSE,
            0,
            FALSE,
            0,
            NULL,
            '年假中'
        UNION ALL
        SELECT 'E00102',
            '2024-05-13',
            '09:10:00',
            '21:00:00',
            'NORMAL',
            TRUE,
            10,
            FALSE,
            0,
            11.50,
            '加班准备新员工培训资料'
    ) AS t ON t.emp_no = e.emp_no;
INSERT INTO leave_requests (
        emp_id,
        start_date,
        end_date,
        days,
        reason,
        status,
        approver_id,
        approval_comment,
        approval_time,
        attachment_url
    )
SELECT e.id,
    t.start_date,
    t.end_date,
    t.days,
    t.reason,
    t.status,
    a.id AS approver_id,
    t.approval_comment,
    t.approval_time,
    t.attachment_url
FROM (
        SELECT 'E00201' AS emp_no,
            '2024-05-20' AS start_date,
            '2024-05-24' AS end_date,
            5 AS days,
            '年假' AS reason,
            'APPROVED' AS status,
            'M002' AS approver_emp_no,
            '通过，请提前做好交接' AS approval_comment,
            '2024-05-15 10:30:00' AS approval_time,
            'https://files.bizcore.com/leave/E00201-202405.pdf' AS attachment_url
        UNION ALL
        SELECT 'E00302',
            '2024-05-16',
            '2024-05-17',
            2,
            '流感病假',
            'PENDING',
            NULL,
            NULL,
            NULL,
            NULL
        UNION ALL
        SELECT 'E00502',
            '2024-05-22',
            '2024-05-22',
            1,
            '家庭事务',
            'REJECTED',
            'M005',
            '业务高峰期暂缓',
            '2024-05-18 09:15:00',
            NULL
        UNION ALL
        SELECT 'E00102',
            '2024-06-03',
            '2024-06-05',
            3,
            '外出培训',
            'APPROVED',
            'M001',
            '培训完成后请分享经验',
            '2024-05-25 14:45:00',
            'https://files.bizcore.com/leave/E00102-training.png'
    ) AS t
    JOIN employees e ON e.emp_no = t.emp_no
    LEFT JOIN employees a ON a.emp_no = t.approver_emp_no;
INSERT INTO attendances (
        emp_id,
        attendance_date,
        check_in_time,
        check_out_time,
        status,
        is_late,
        late_minutes,
        is_early_leave,
        early_leave_minutes,
        work_hours,
        dept_id,
        remark
    )
SELECT e.id,
    s.attendance_date,
    s.check_in_time,
    s.check_out_time,
    s.status,
    s.is_late,
    s.late_minutes,
    s.is_early_leave,
    s.early_leave_minutes,
    s.work_hours,
    e.dept_id,
    s.remark
FROM employees e
    JOIN departments d ON d.id = e.dept_id
    JOIN (
        SELECT 'D001' AS dept_code,
            '2025-10-10' AS attendance_date,
            '09:01:00' AS check_in_time,
            '18:02:00' AS check_out_time,
            'NORMAL' AS status,
            FALSE AS is_late,
            0 AS late_minutes,
            FALSE AS is_early_leave,
            0 AS early_leave_minutes,
            8.17 AS work_hours,
            '入职手续支持' AS remark
        UNION ALL
        SELECT 'D001',
            '2025-10-11',
            '08:59:00',
            '17:55:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            7.93,
            '完成绩效资料校对'
        UNION ALL
        SELECT 'D001',
            '2025-10-12',
            NULL,
            NULL,
            'REST',
            FALSE,
            0,
            FALSE,
            0,
            NULL,
            '周末轮休'
        UNION ALL
        SELECT 'D001',
            '2025-10-13',
            '09:12:00',
            '18:20:00',
            'LATE',
            TRUE,
            12,
            FALSE,
            0,
            8.40,
            '早会后补打卡'
        UNION ALL
        SELECT 'D001',
            '2025-10-14',
            '08:56:00',
            '18:05:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.15,
            '招聘面试协调'
        UNION ALL
        SELECT 'D001',
            '2025-10-15',
            '09:03:00',
            '18:10:00',
            'NORMAL',
            TRUE,
            3,
            FALSE,
            0,
            8.12,
            '员工调研访谈'
        UNION ALL
        SELECT 'D001',
            '2025-10-16',
            '08:58:00',
            '17:20:00',
            'EARLY_LEAVE',
            FALSE,
            0,
            TRUE,
            40,
            7.37,
            '晚间校招宣讲准备'
        UNION ALL
        SELECT 'D001',
            '2025-10-17',
            '09:00:00',
            '18:25:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.42,
            '薪酬评审会务支持'
        UNION ALL
        SELECT 'D001',
            '2025-10-18',
            '08:55:00',
            '19:30:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            9.92,
            '周末校招复盘（加班）'
        UNION ALL
        SELECT 'D001',
            '2025-10-19',
            '09:04:00',
            '18:03:00',
            'NORMAL',
            TRUE,
            4,
            FALSE,
            0,
            8.08,
            '社保稽核整理'
        UNION ALL
        SELECT 'D002',
            '2025-10-10',
            '08:46:00',
            '18:05:00',
            'NORMAL',
            TRUE,
            1,
            FALSE,
            0,
            8.32,
            '付款计划复核'
        UNION ALL
        SELECT 'D002',
            '2025-10-11',
            '08:40:00',
            '18:20:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            9.33,
            '预算滚动调整（加班）'
        UNION ALL
        SELECT 'D002',
            '2025-10-12',
            NULL,
            NULL,
            'REST',
            FALSE,
            0,
            FALSE,
            0,
            NULL,
            '周末轮休'
        UNION ALL
        SELECT 'D002',
            '2025-10-13',
            '08:55:00',
            '18:30:00',
            'LATE',
            TRUE,
            10,
            FALSE,
            0,
            8.58,
            '出纳交接延后'
        UNION ALL
        SELECT 'D002',
            '2025-10-14',
            '08:44:00',
            '18:02:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.30,
            '成本报表审核'
        UNION ALL
        SELECT 'D002',
            '2025-10-15',
            '08:47:00',
            '18:50:00',
            'NORMAL',
            TRUE,
            2,
            FALSE,
            0,
            9.55,
            '外部审计会议（加班）'
        UNION ALL
        SELECT 'D002',
            '2025-10-16',
            '08:42:00',
            '17:50:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.13,
            '资金周报整理'
        UNION ALL
        SELECT 'D002',
            '2025-10-17',
            '08:58:00',
            '17:40:00',
            'EARLY_LEAVE',
            TRUE,
            13,
            TRUE,
            20,
            7.70,
            '下午税务局报送'
        UNION ALL
        SELECT 'D002',
            '2025-10-18',
            '08:41:00',
            '18:10:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.48,
            '月底预提复核'
        UNION ALL
        SELECT 'D002',
            '2025-10-19',
            '08:50:00',
            '18:45:00',
            'NORMAL',
            TRUE,
            5,
            FALSE,
            0,
            9.92,
            '年度决算准备（加班）'
        UNION ALL
        SELECT 'D003',
            '2025-10-10',
            '09:18:00',
            '18:40:00',
            'NORMAL',
            TRUE,
            3,
            FALSE,
            0,
            8.70,
            '大客户拜访复盘'
        UNION ALL
        SELECT 'D003',
            '2025-10-11',
            '09:12:00',
            '19:05:00',
            'NORMAL',
            TRUE,
            12,
            FALSE,
            0,
            9.88,
            '季度促销策划（加班）'
        UNION ALL
        SELECT 'D003',
            '2025-10-12',
            NULL,
            NULL,
            'REST',
            FALSE,
            0,
            FALSE,
            0,
            NULL,
            '周末休市'
        UNION ALL
        SELECT 'D003',
            '2025-10-13',
            '09:25:00',
            '18:20:00',
            'LATE',
            TRUE,
            25,
            FALSE,
            0,
            8.25,
            '交通拥堵迟到'
        UNION ALL
        SELECT 'D003',
            '2025-10-14',
            '09:16:00',
            '18:35:00',
            'NORMAL',
            TRUE,
            1,
            FALSE,
            0,
            8.63,
            '渠道会议总结'
        UNION ALL
        SELECT 'D003',
            '2025-10-15',
            '09:08:00',
            '17:55:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            7.78,
            '签单资料整理'
        UNION ALL
        SELECT 'D003',
            '2025-10-16',
            '09:30:00',
            '17:40:00',
            'EARLY_LEAVE',
            TRUE,
            30,
            TRUE,
            20,
            7.17,
            '前往客户答谢会'
        UNION ALL
        SELECT 'D003',
            '2025-10-17',
            '09:22:00',
            '19:20:00',
            'NORMAL',
            TRUE,
            22,
            FALSE,
            0,
            9.80,
            '新产品说明会（加班）'
        UNION ALL
        SELECT 'D003',
            '2025-10-18',
            '09:10:00',
            '18:15:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.08,
            '区域业绩回顾'
        UNION ALL
        SELECT 'D003',
            '2025-10-19',
            '09:28:00',
            '18:05:00',
            'LATE',
            TRUE,
            28,
            FALSE,
            0,
            7.95,
            '客户陪访总结'
        UNION ALL
        SELECT 'D004',
            '2025-10-10',
            '09:32:00',
            '19:05:00',
            'NORMAL',
            TRUE,
            2,
            FALSE,
            0,
            9.55,
            '版本回归测试（加班）'
        UNION ALL
        SELECT 'D004',
            '2025-10-11',
            '09:28:00',
            '18:45:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.78,
            '算法性能调优'
        UNION ALL
        SELECT 'D004',
            '2025-10-12',
            NULL,
            NULL,
            'REST',
            FALSE,
            0,
            FALSE,
            0,
            NULL,
            '周末自测安排'
        UNION ALL
        SELECT 'D004',
            '2025-10-13',
            '09:40:00',
            '19:30:00',
            'LATE',
            TRUE,
            10,
            FALSE,
            0,
            9.83,
            '晨会后直接上线'
        UNION ALL
        SELECT 'D004',
            '2025-10-14',
            '09:27:00',
            '18:50:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.90,
            '代码走查'
        UNION ALL
        SELECT 'D004',
            '2025-10-15',
            '09:34:00',
            '20:05:00',
            'NORMAL',
            TRUE,
            4,
            FALSE,
            0,
            10.52,
            '发布窗口支撑（加班）'
        UNION ALL
        SELECT 'D004',
            '2025-10-16',
            '09:20:00',
            '18:10:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.17,
            '模块设计讨论'
        UNION ALL
        SELECT 'D004',
            '2025-10-17',
            '09:45:00',
            '17:55:00',
            'EARLY_LEAVE',
            TRUE,
            15,
            TRUE,
            35,
            7.17,
            '外出技术交流'
        UNION ALL
        SELECT 'D004',
            '2025-10-18',
            '09:31:00',
            '19:15:00',
            'NORMAL',
            TRUE,
            1,
            FALSE,
            0,
            9.73,
            '故障应急处理（加班）'
        UNION ALL
        SELECT 'D004',
            '2025-10-19',
            '09:29:00',
            '18:12:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.22,
            '代码合并收尾'
        UNION ALL
        SELECT 'D005',
            '2025-10-10',
            '08:33:00',
            '17:45:00',
            'NORMAL',
            TRUE,
            3,
            FALSE,
            0,
            8.20,
            '运营数据晨会'
        UNION ALL
        SELECT 'D005',
            '2025-10-11',
            '08:29:00',
            '18:05:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            9.10,
            '节前备货调度（加班）'
        UNION ALL
        SELECT 'D005',
            '2025-10-12',
            NULL,
            NULL,
            'REST',
            FALSE,
            0,
            FALSE,
            0,
            NULL,
            '周末轮休'
        UNION ALL
        SELECT 'D005',
            '2025-10-13',
            '08:38:00',
            '17:30:00',
            'NORMAL',
            TRUE,
            8,
            FALSE,
            0,
            7.87,
            '供应商对账'
        UNION ALL
        SELECT 'D005',
            '2025-10-14',
            '08:26:00',
            '17:55:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.48,
            '仓储策略调整'
        UNION ALL
        SELECT 'D005',
            '2025-10-15',
            '08:45:00',
            '18:20:00',
            'NORMAL',
            TRUE,
            15,
            FALSE,
            0,
            9.58,
            '系统切换演练（加班）'
        UNION ALL
        SELECT 'D005',
            '2025-10-16',
            '08:31:00',
            '17:10:00',
            'EARLY_LEAVE',
            TRUE,
            1,
            TRUE,
            50,
            7.32,
            '外出巡仓'
        UNION ALL
        SELECT 'D005',
            '2025-10-17',
            '08:28:00',
            '17:40:00',
            'NORMAL',
            FALSE,
            0,
            FALSE,
            0,
            8.20,
            '流程改进跟进'
        UNION ALL
        SELECT 'D005',
            '2025-10-18',
            '08:36:00',
            '18:35:00',
            'NORMAL',
            TRUE,
            6,
            FALSE,
            0,
            9.98,
            '促销活动支持（加班）'
        UNION ALL
        SELECT 'D005',
            '2025-10-19',
            '08:34:00',
            '17:25:00',
            'NORMAL',
            TRUE,
            4,
            FALSE,
            0,
            7.85,
            '月度指标分析'
    ) AS s ON s.dept_code = d.dept_code;

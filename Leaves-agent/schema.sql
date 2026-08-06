CREATE DATABASE IF NOT EXISTS employee_db;
USE employee_db;

CREATE TABLE IF NOT EXISTS employees (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     VARCHAR(20)  NOT NULL UNIQUE,
    employee_name   VARCHAR(100) NOT NULL,
    password        VARCHAR(100) NOT NULL, -- plaintext, per explicit request; see README security note
    department      VARCHAR(50),
    designation     VARCHAR(50),
    role            VARCHAR(30),
    active          BOOLEAN DEFAULT TRUE,
    joining_date    DATE
);

CREATE TABLE IF NOT EXISTS leave_balance (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     VARCHAR(20) NOT NULL,
    leave_type      VARCHAR(20) NOT NULL,
    balance_days    INT NOT NULL,
    UNIQUE KEY uq_employee_leave (employee_id, leave_type),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE IF NOT EXISTS payslips (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     VARCHAR(20) NOT NULL,
    pay_month       VARCHAR(7)  NOT NULL, -- 'YYYY-MM'
    basic_salary    DECIMAL(12,2) NOT NULL,
    deductions      DECIMAL(12,2) NOT NULL,
    net_salary      DECIMAL(12,2) NOT NULL,
    UNIQUE KEY uq_employee_month (employee_id, pay_month),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

-- Sample data. Password is stored in plaintext here, per explicit request —
-- this is fine for local learning only. See README.md for why this is not
-- safe to carry into anything real.
INSERT INTO employees (employee_id, employee_name, password, department, designation, role, active, joining_date)
VALUES ('E1001', 'Asha Rao', 'password123', 'Engineering', 'Senior Engineer', 'EMPLOYEE', TRUE, '2021-06-01');

INSERT INTO leave_balance (employee_id, leave_type, balance_days) VALUES
    ('E1001', 'ANNUAL', 12),
    ('E1001', 'SICK', 6),
    ('E1001', 'CASUAL', 4);

INSERT INTO payslips (employee_id, pay_month, basic_salary, deductions, net_salary) VALUES
    ('E1001', '2026-06', 90000.00, 12000.00, 78000.00);

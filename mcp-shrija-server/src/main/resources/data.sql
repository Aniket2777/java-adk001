-- Seed data for local development/demo only. Runs on every startup
-- alongside ddl-auto=update (see application.yml) - safe because every
-- insert is idempotent (guarded by a WHERE NOT EXISTS check), so restarts
-- don't create duplicate rows. Remove or gate this behind a profile before
-- any real deployment.

INSERT INTO hr_employee (employee_code, full_name, email, department, designation, employment_status)
SELECT 'EMP1024', 'Asha Rao', 'asha.rao@shrija.ai', 'Engineering', 'Senior Engineer', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_code = 'EMP1024');

INSERT INTO hr_employee (employee_code, full_name, email, department, designation, employment_status)
SELECT 'EMP2001', 'Vikram Shah', 'vikram.shah@shrija.ai', 'Finance', 'Analyst', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_code = 'EMP2001');

INSERT INTO hr_employee (employee_code, full_name, email, department, designation, employment_status)
SELECT 'EMP3050', 'Priya Menon', 'priya.menon@shrija.ai', 'Engineering', 'QA Engineer', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM hr_employee WHERE employee_code = 'EMP3050');

INSERT INTO employee_leave_balance (employee_code, leave_type, total_days, used_days)
SELECT 'EMP1024', 'ANNUAL', 18, 4
WHERE NOT EXISTS (
    SELECT 1 FROM employee_leave_balance WHERE employee_code = 'EMP1024' AND leave_type = 'ANNUAL');

INSERT INTO employee_leave_balance (employee_code, leave_type, total_days, used_days)
SELECT 'EMP1024', 'SICK', 10, 2
WHERE NOT EXISTS (
    SELECT 1 FROM employee_leave_balance WHERE employee_code = 'EMP1024' AND leave_type = 'SICK');

INSERT INTO employee_leave_balance (employee_code, leave_type, total_days, used_days)
SELECT 'EMP1024', 'CASUAL', 8, 0
WHERE NOT EXISTS (
    SELECT 1 FROM employee_leave_balance WHERE employee_code = 'EMP1024' AND leave_type = 'CASUAL');

INSERT INTO employee_leave_balance (employee_code, leave_type, total_days, used_days)
SELECT 'EMP2001', 'ANNUAL', 18, 10
WHERE NOT EXISTS (
    SELECT 1 FROM employee_leave_balance WHERE employee_code = 'EMP2001' AND leave_type = 'ANNUAL');

INSERT INTO employee_lifecycle_task (employee_code, task_type, task_name, status, due_date)
SELECT 'EMP3050', 'ONBOARDING', 'Laptop provisioning', 'COMPLETED', DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM employee_lifecycle_task WHERE employee_code = 'EMP3050' AND task_name = 'Laptop provisioning');

INSERT INTO employee_lifecycle_task (employee_code, task_type, task_name, status, due_date)
SELECT 'EMP3050', 'ONBOARDING', 'Access badge issued', 'COMPLETED', DATE_SUB(CURRENT_DATE, INTERVAL 18 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM employee_lifecycle_task WHERE employee_code = 'EMP3050' AND task_name = 'Access badge issued');

INSERT INTO employee_lifecycle_task (employee_code, task_type, task_name, status, due_date)
SELECT 'EMP3050', 'ONBOARDING', 'Benefits enrollment', 'PENDING', DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM employee_lifecycle_task WHERE employee_code = 'EMP3050' AND task_name = 'Benefits enrollment');

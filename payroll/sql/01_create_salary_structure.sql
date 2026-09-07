USE employee_db;

-- REQUIRED FIRST: MySQL requires a FOREIGN KEY to point at a column that is
-- either a PRIMARY KEY or has a UNIQUE index. employees.employee_id is a
-- plain VARCHAR column, so without this, CREATE TABLE below fails with:
--   "Error Code: 1215. Cannot add foreign key constraint"
-- Safe to re-run: does nothing if the index already exists.
ALTER TABLE employees ADD UNIQUE INDEX uq_employees_employee_id (employee_id);

-- Payroll-specific data, kept separate from the employees table (which owns
-- identity/login/role data). Joined back to employees via employee_id.
CREATE TABLE IF NOT EXISTS salary_structure (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    employee_id       VARCHAR(20) NOT NULL UNIQUE,
    base_salary       DECIMAL(12, 2) NOT NULL,
    hra               DECIMAL(12, 2) NOT NULL DEFAULT 0,
    special_allowance DECIMAL(12, 2) NOT NULL DEFAULT 0,
    effective_from    DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_salary_employee
        FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
        ON DELETE CASCADE
);

-- Seed rows matching the 4 employees currently in your employees table.
-- Adjust the actual figures to whatever your org really pays.
INSERT INTO salary_structure (employee_id, base_salary, hra, special_allowance)
VALUES
    ('EMP001', 50000.00, 15000.00, 5000.00),  -- Prachi Ghatole, Software Engineer
    ('EMP002', 90000.00, 27000.00, 9000.00),  -- Rahul Sharma, HR Manager
    ('EMP003', 45000.00, 13500.00, 4000.00),  -- Anjali Patil, Accountant
    ('EMP004', 85000.00, 25500.00, 8500.00)   -- Amit Verma, Team Lead
ON DUPLICATE KEY UPDATE
    base_salary = VALUES(base_salary),
    hra = VALUES(hra),
    special_allowance = VALUES(special_allowance);

-- Sanity check: this is the join the repository will run
SELECT
    e.employee_id,
    e.employee_name,
    e.designation,
    e.department,
    e.active,
    s.base_salary,
    s.hra,
    s.special_allowance
FROM employees e
JOIN salary_structure s ON e.employee_id = s.employee_id;
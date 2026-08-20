package com.shrija.domain.repository;
import com.shrija.domain.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartmentIgnoreCase(String department);
}

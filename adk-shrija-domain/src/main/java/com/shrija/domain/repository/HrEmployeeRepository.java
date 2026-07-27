package com.shrija.domain.repository;

import com.shrija.domain.model.HrEmployee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link HrEmployee}. Kept as a plain Spring Data interface - no custom
 * implementation needed yet, so there's nothing here to reinvent.
 */
public interface HrEmployeeRepository extends JpaRepository<HrEmployee, Long> {

  Optional<HrEmployee> findByEmployeeCodeIgnoreCase(String employeeCode);

  List<HrEmployee> findByDepartmentIgnoreCase(String department);

  boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

  boolean existsByEmailIgnoreCase(String email);
}

package com.hrms.mcpserver.repository;

import com.hrms.mcpserver.domain.SalarySlip;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalarySlipRepository extends JpaRepository<SalarySlip, Long> {
  Optional<SalarySlip> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
}

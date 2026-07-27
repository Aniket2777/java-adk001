package com.shrija.domain.repository;

import com.shrija.domain.model.EmployeeLeaveBalance;
import com.shrija.domain.model.LeaveType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeLeaveBalanceRepository extends JpaRepository<EmployeeLeaveBalance, Long> {

  Optional<EmployeeLeaveBalance> findByEmployeeCodeIgnoreCaseAndLeaveType(
      String employeeCode, LeaveType leaveType);
}

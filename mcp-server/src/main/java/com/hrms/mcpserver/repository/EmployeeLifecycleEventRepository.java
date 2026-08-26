package com.hrms.mcpserver.repository;

import com.hrms.mcpserver.domain.EmployeeLifecycleEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeLifecycleEventRepository
    extends JpaRepository<EmployeeLifecycleEvent, Long> {
  List<EmployeeLifecycleEvent> findByEmployeeId(Long employeeId);
}

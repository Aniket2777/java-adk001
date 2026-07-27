package com.shrija.domain.repository;

import com.shrija.domain.model.EmployeeLifecycleTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeLifecycleTaskRepository
    extends JpaRepository<EmployeeLifecycleTask, Long> {

  List<EmployeeLifecycleTask> findByEmployeeCodeIgnoreCase(String employeeCode);
}

package com.shrija.domain.repository;
import com.shrija.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LifecycleTaskRepository extends JpaRepository<LifecycleTask,Long> {
    List<LifecycleTask> findByEmployeeId(Long employeeId);
    List<LifecycleTask> findByStatusIgnoreCase(String status);
}

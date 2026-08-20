package com.shrija.domain.repository;
import com.shrija.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface BudgetRecordRepository extends JpaRepository<BudgetRecord,Long> {
    List<BudgetRecord> findByDepartmentIgnoreCase(String department);
}

package com.shrija.domain.repository;
import com.shrija.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord,Long> {
    Optional<PayrollRecord> findByEmployeeIdAndPayMonth(Long employeeId,String payMonth);
    List<PayrollRecord> findByEmployeeIdOrderByPayMonthDesc(Long employeeId);
}

package com.shrija.domain.repository;
import com.shrija.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord,Long> {
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(Long employeeId, LocalDate from, LocalDate to);
}

package com.shrija.domain.repository;

import com.shrija.domain.model.LeaveRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

  List<LeaveRequest> findByEmployeeCodeIgnoreCaseOrderByAppliedAtDesc(String employeeCode);

  List<LeaveRequest> findByStatusOrderByAppliedAtAsc(LeaveRequest.Status status);
}

package com.shrija.domain.repository;

import com.shrija.domain.model.DocumentRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRequestRepository extends JpaRepository<DocumentRequest, Long> {

  List<DocumentRequest> findByEmployeeCodeIgnoreCaseOrderByRequestedAtDesc(String employeeCode);

  List<DocumentRequest> findByStatusOrderByRequestedAtAsc(DocumentRequest.Status status);
}

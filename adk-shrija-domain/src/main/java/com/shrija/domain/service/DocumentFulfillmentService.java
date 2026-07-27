package com.shrija.domain.service;

import com.shrija.domain.dto.DocumentRequestDto;
import com.shrija.domain.exception.DocumentRequestNotActionableException;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.mapper.EmployeeSelfServiceMapper;
import com.shrija.domain.model.DocumentRequest;
import com.shrija.domain.repository.DocumentRequestRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HR-side fulfillment of document requests an employee has submitted (offer letter, joining letter,
 * experience letter, salary certificate, ID proof) - separate from {@code
 * EmployeeSelfServiceService}, same reasoning as {@code LeaveApprovalService}: fulfilling a request
 * is a different actor's job than submitting one, and keeping the services apart means an
 * employee's tools have no path to fulfillment logic.
 *
 * <p>Scope for this pass: the queue and a single "mark ready" action, plus an explicit "mark
 * delivered" once handed over. No rejection path yet (unlike leave) - flag if a document request
 * needs to be declined, e.g. for an employee who was never actually hired.
 */
@Service
public class DocumentFulfillmentService {

  private static final Logger log = LoggerFactory.getLogger(DocumentFulfillmentService.class);

  private final DocumentRequestRepository documentRequestRepository;
  private final EmployeeSelfServiceMapper mapper;

  public DocumentFulfillmentService(
      DocumentRequestRepository documentRequestRepository, EmployeeSelfServiceMapper mapper) {
    this.documentRequestRepository = documentRequestRepository;
    this.mapper = mapper;
  }

  public List<DocumentRequestDto> listPendingDocumentRequests() {
    return documentRequestRepository
        .findByStatusOrderByRequestedAtAsc(DocumentRequest.Status.REQUESTED)
        .stream()
        .map(mapper::toDto)
        .toList();
  }

  @Transactional
  public DocumentRequestDto markDocumentReady(Long requestId) {
    DocumentRequest request = findOrThrow(requestId);
    if (request.getStatus() != DocumentRequest.Status.REQUESTED) {
      throw new DocumentRequestNotActionableException(
          requestId, request.getStatus().name(), DocumentRequest.Status.REQUESTED.name());
    }
    request.markReady();
    DocumentRequest saved = documentRequestRepository.save(request);
    log.info(
        "Document request {} marked READY for employee={}", requestId, request.getEmployeeCode());
    return mapper.toDto(saved);
  }

  @Transactional
  public DocumentRequestDto markDocumentDelivered(Long requestId) {
    DocumentRequest request = findOrThrow(requestId);
    if (request.getStatus() != DocumentRequest.Status.READY) {
      throw new DocumentRequestNotActionableException(
          requestId, request.getStatus().name(), DocumentRequest.Status.READY.name());
    }
    request.markDelivered();
    DocumentRequest saved = documentRequestRepository.save(request);
    log.info(
        "Document request {} marked DELIVERED for employee={}",
        requestId,
        request.getEmployeeCode());
    return mapper.toDto(saved);
  }

  private DocumentRequest findOrThrow(Long requestId) {
    return documentRequestRepository
        .findById(requestId)
        .orElseThrow(
            () -> new ResourceNotFoundException("No document request found with id " + requestId));
  }
}

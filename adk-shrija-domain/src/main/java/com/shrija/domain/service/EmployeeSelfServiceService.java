package com.shrija.domain.service;

import com.shrija.domain.dto.DocumentRequestDto;
import com.shrija.domain.dto.LeaveBalanceDto;
import com.shrija.domain.dto.LeaveRequestDto;
import com.shrija.domain.dto.LifecycleTaskDto;
import com.shrija.domain.exception.InvalidLeaveRequestException;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.mapper.EmployeeSelfServiceMapper;
import com.shrija.domain.model.DocumentRequest;
import com.shrija.domain.model.DocumentType;
import com.shrija.domain.model.EmployeeLeaveBalance;
import com.shrija.domain.model.LeaveRequest;
import com.shrija.domain.model.LeaveType;
import com.shrija.domain.repository.DocumentRequestRepository;
import com.shrija.domain.repository.EmployeeLeaveBalanceRepository;
import com.shrija.domain.repository.EmployeeLifecycleTaskRepository;
import com.shrija.domain.repository.LeaveRequestRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for the Employee Agent's self-service domain: leave balance/application,
 * onboarding/offboarding status, and document requests. Kept as one service (mirroring {@code
 * EmployeeSelfServiceMapper}'s reasoning) since these are one cohesive bounded context the Employee
 * Agent owns end to end, not four unrelated features.
 */
@Service
public class EmployeeSelfServiceService {

  private static final Logger log = LoggerFactory.getLogger(EmployeeSelfServiceService.class);

  private final EmployeeLeaveBalanceRepository leaveBalanceRepository;
  private final LeaveRequestRepository leaveRequestRepository;
  private final EmployeeLifecycleTaskRepository lifecycleTaskRepository;
  private final DocumentRequestRepository documentRequestRepository;
  private final EmployeeSelfServiceMapper mapper;

  public EmployeeSelfServiceService(
      EmployeeLeaveBalanceRepository leaveBalanceRepository,
      LeaveRequestRepository leaveRequestRepository,
      EmployeeLifecycleTaskRepository lifecycleTaskRepository,
      DocumentRequestRepository documentRequestRepository,
      EmployeeSelfServiceMapper mapper) {
    this.leaveBalanceRepository = leaveBalanceRepository;
    this.leaveRequestRepository = leaveRequestRepository;
    this.lifecycleTaskRepository = lifecycleTaskRepository;
    this.documentRequestRepository = documentRequestRepository;
    this.mapper = mapper;
  }

  public LeaveBalanceDto getLeaveBalance(String employeeCode, LeaveType leaveType) {
    EmployeeLeaveBalance balance =
        leaveBalanceRepository
            .findByEmployeeCodeIgnoreCaseAndLeaveType(employeeCode, leaveType)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No "
                            + leaveType
                            + " leave balance found for employee '"
                            + employeeCode
                            + "'"));
    return mapper.toDto(balance);
  }

  /**
   * Applies for leave. Validates the date range and available balance before creating the request;
   * the request is always created as {@code PENDING} - approval is a separate workflow this agent
   * doesn't own.
   *
   * @param startDateIso ISO-8601 date (yyyy-MM-dd) - see class-level note on why dates are strings,
   *     not {@code LocalDate}, at the tool boundary
   */
  @Transactional
  public LeaveRequestDto applyForLeave(
      String employeeCode, LeaveType leaveType, String startDateIso, String endDateIso) {
    LocalDate startDate = parseDate(startDateIso, "startDate");
    LocalDate endDate = parseDate(endDateIso, "endDate");

    if (endDate.isBefore(startDate)) {
      throw new InvalidLeaveRequestException("endDate cannot be before startDate");
    }
    if (startDate.isBefore(LocalDate.now())) {
      throw new InvalidLeaveRequestException("startDate cannot be in the past");
    }

    EmployeeLeaveBalance balance =
        leaveBalanceRepository
            .findByEmployeeCodeIgnoreCaseAndLeaveType(employeeCode, leaveType)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No "
                            + leaveType
                            + " leave balance found for employee '"
                            + employeeCode
                            + "'"));

    long requestedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    if (requestedDays > balance.getRemainingDays()) {
      throw new InvalidLeaveRequestException(
          "Requested "
              + requestedDays
              + " day(s) of "
              + leaveType
              + " leave but only "
              + balance.getRemainingDays()
              + " day(s) remain");
    }

    LeaveRequest request =
        new LeaveRequest(
            employeeCode,
            leaveType,
            startDate,
            endDate,
            LeaveRequest.Status.PENDING,
            Instant.now());
    LeaveRequest saved = leaveRequestRepository.save(request);

    balance.addUsedDays((int) requestedDays);
    leaveBalanceRepository.save(balance);

    log.info(
        "Leave request {} created for employee={} type={} days={}",
        saved.getId(),
        employeeCode,
        leaveType,
        requestedDays);
    return mapper.toDto(saved);
  }

  public java.util.List<LifecycleTaskDto> getLifecycleStatus(String employeeCode) {
    return lifecycleTaskRepository.findByEmployeeCodeIgnoreCase(employeeCode).stream()
        .map(mapper::toDto)
        .toList();
  }

  @Transactional
  public DocumentRequestDto requestDocument(String employeeCode, DocumentType documentType) {
    DocumentRequest request =
        new DocumentRequest(
            employeeCode, documentType, DocumentRequest.Status.REQUESTED, Instant.now());
    DocumentRequest saved = documentRequestRepository.save(request);
    log.info(
        "Document request {} created for employee={} type={}",
        saved.getId(),
        employeeCode,
        documentType);
    return mapper.toDto(saved);
  }

  public java.util.List<DocumentRequestDto> getDocumentRequestStatus(String employeeCode) {
    return documentRequestRepository
        .findByEmployeeCodeIgnoreCaseOrderByRequestedAtDesc(employeeCode)
        .stream()
        .map(mapper::toDto)
        .toList();
  }

  private LocalDate parseDate(String iso, String fieldName) {
    try {
      return LocalDate.parse(iso);
    } catch (DateTimeParseException ex) {
      throw new InvalidLeaveRequestException(
          fieldName + " must be in yyyy-MM-dd format, got '" + iso + "'");
    }
  }
}

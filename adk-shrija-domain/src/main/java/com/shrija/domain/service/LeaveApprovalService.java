package com.shrija.domain.service;

import com.shrija.domain.dto.LeaveRequestDto;
import com.shrija.domain.exception.LeaveRequestNotPendingException;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.mapper.EmployeeSelfServiceMapper;
import com.shrija.domain.model.EmployeeLeaveBalance;
import com.shrija.domain.model.LeaveRequest;
import com.shrija.domain.repository.EmployeeLeaveBalanceRepository;
import com.shrija.domain.repository.LeaveRequestRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HR-side approval workflow for leave requests {@code EmployeeSelfServiceService} creates.
 * Deliberately a separate service from {@code EmployeeSelfServiceService} even though both operate
 * on {@code LeaveRequest} - approval is an HR responsibility (a different agent, a different actor)
 * reviewing a request an employee submitted, not the same actor completing their own workflow.
 * Keeping them apart also means an employee's tools have no way to call approval logic, even
 * accidentally.
 *
 * <p>Balance accounting: {@code EmployeeSelfServiceService.applyForLeave} already decrements the
 * balance at *application* time (treating it as a reservation). Approval therefore doesn't touch
 * the balance again; rejection must give the reserved days back.
 */
@Service
public class LeaveApprovalService {

  private static final Logger log = LoggerFactory.getLogger(LeaveApprovalService.class);

  private final LeaveRequestRepository leaveRequestRepository;
  private final EmployeeLeaveBalanceRepository leaveBalanceRepository;
  private final EmployeeSelfServiceMapper mapper;

  public LeaveApprovalService(
      LeaveRequestRepository leaveRequestRepository,
      EmployeeLeaveBalanceRepository leaveBalanceRepository,
      EmployeeSelfServiceMapper mapper) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveBalanceRepository = leaveBalanceRepository;
    this.mapper = mapper;
  }

  public List<LeaveRequestDto> listPendingLeaveRequests() {
    return leaveRequestRepository
        .findByStatusOrderByAppliedAtAsc(LeaveRequest.Status.PENDING)
        .stream()
        .map(mapper::toDto)
        .toList();
  }

  @Transactional
  public LeaveRequestDto approveLeaveRequest(Long requestId) {
    LeaveRequest request = findPendingOrThrow(requestId);
    request.markApproved();
    LeaveRequest saved = leaveRequestRepository.save(request);
    log.info("Leave request {} approved for employee={}", requestId, request.getEmployeeCode());
    return mapper.toDto(saved);
  }

  @Transactional
  public LeaveRequestDto rejectLeaveRequest(Long requestId, String reason) {
    LeaveRequest request = findPendingOrThrow(requestId);

    EmployeeLeaveBalance balance =
        leaveBalanceRepository
            .findByEmployeeCodeIgnoreCaseAndLeaveType(
                request.getEmployeeCode(), request.getLeaveType())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No "
                            + request.getLeaveType()
                            + " leave balance found for employee '"
                            + request.getEmployeeCode()
                            + "' to refund"));
    balance.refundDays((int) request.requestedDayCount());
    leaveBalanceRepository.save(balance);

    request.markRejected(reason);
    LeaveRequest saved = leaveRequestRepository.save(request);
    log.info(
        "Leave request {} rejected for employee={}: {}",
        requestId,
        request.getEmployeeCode(),
        reason);
    return mapper.toDto(saved);
  }

  private LeaveRequest findPendingOrThrow(Long requestId) {
    LeaveRequest request =
        leaveRequestRepository
            .findById(requestId)
            .orElseThrow(
                () -> new ResourceNotFoundException("No leave request found with id " + requestId));
    if (request.getStatus() != LeaveRequest.Status.PENDING) {
      throw new LeaveRequestNotPendingException(requestId, request.getStatus().name());
    }
    return request;
  }
}

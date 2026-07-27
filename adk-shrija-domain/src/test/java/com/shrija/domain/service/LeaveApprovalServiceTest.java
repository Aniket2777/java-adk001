package com.shrija.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.shrija.domain.dto.LeaveRequestDto;
import com.shrija.domain.exception.LeaveRequestNotPendingException;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.mapper.EmployeeSelfServiceMapper;
import com.shrija.domain.model.EmployeeLeaveBalance;
import com.shrija.domain.model.LeaveRequest;
import com.shrija.domain.model.LeaveType;
import com.shrija.domain.repository.EmployeeLeaveBalanceRepository;
import com.shrija.domain.repository.LeaveRequestRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveApprovalServiceTest {

  @Mock private LeaveRequestRepository leaveRequestRepository;
  @Mock private EmployeeLeaveBalanceRepository leaveBalanceRepository;
  @Mock private EmployeeSelfServiceMapper mapper;

  private LeaveApprovalService newService() {
    return new LeaveApprovalService(leaveRequestRepository, leaveBalanceRepository, mapper);
  }

  private LeaveRequest pendingRequest() {
    return new LeaveRequest(
        "EMP1024",
        LeaveType.ANNUAL,
        LocalDate.now().plusDays(1),
        LocalDate.now().plusDays(2),
        LeaveRequest.Status.PENDING,
        Instant.now());
  }

  @Test
  void approveLeaveRequest_marksApproved_withoutTouchingBalance() {
    LeaveApprovalService service = newService();
    LeaveRequest request = pendingRequest();
    when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));
    when(leaveRequestRepository.save(request)).thenReturn(request);
    LeaveRequestDto dto =
        new LeaveRequestDto(
            1L,
            "EMP1024",
            "ANNUAL",
            request.getStartDate().toString(),
            request.getEndDate().toString(),
            "APPROVED",
            null);
    when(mapper.toDto(request)).thenReturn(dto);

    LeaveRequestDto result = service.approveLeaveRequest(1L);

    assertThat(result.status()).isEqualTo("APPROVED");
    assertThat(request.getStatus()).isEqualTo(LeaveRequest.Status.APPROVED);
  }

  @Test
  void rejectLeaveRequest_refundsBalance_andMarksRejected() {
    LeaveApprovalService service = newService();
    LeaveRequest request = pendingRequest(); // 2 days requested
    EmployeeLeaveBalance balance = new EmployeeLeaveBalance("EMP1024", LeaveType.ANNUAL, 10, 5);

    when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));
    when(leaveBalanceRepository.findByEmployeeCodeIgnoreCaseAndLeaveType(
            "EMP1024", LeaveType.ANNUAL))
        .thenReturn(Optional.of(balance));
    when(leaveRequestRepository.save(request)).thenReturn(request);
    LeaveRequestDto dto =
        new LeaveRequestDto(
            1L,
            "EMP1024",
            "ANNUAL",
            request.getStartDate().toString(),
            request.getEndDate().toString(),
            "REJECTED",
            "Team is short-staffed that week");
    when(mapper.toDto(request)).thenReturn(dto);

    LeaveRequestDto result = service.rejectLeaveRequest(1L, "Team is short-staffed that week");

    assertThat(result.status()).isEqualTo("REJECTED");
    assertThat(balance.getUsedDays()).isEqualTo(3); // 5 - 2 refunded
  }

  @Test
  void actingOnAlreadyDecidedRequest_throws() {
    LeaveApprovalService service = newService();
    LeaveRequest request = pendingRequest();
    request.markApproved();
    when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(request));

    assertThatThrownBy(() -> service.approveLeaveRequest(1L))
        .isInstanceOf(LeaveRequestNotPendingException.class);
  }

  @Test
  void actingOnUnknownRequest_throwsNotFound() {
    LeaveApprovalService service = newService();
    when(leaveRequestRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.approveLeaveRequest(99L))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}

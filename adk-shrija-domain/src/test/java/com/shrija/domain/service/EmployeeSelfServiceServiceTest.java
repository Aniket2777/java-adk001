package com.shrija.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shrija.domain.dto.LeaveRequestDto;
import com.shrija.domain.exception.InvalidLeaveRequestException;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.mapper.EmployeeSelfServiceMapper;
import com.shrija.domain.model.EmployeeLeaveBalance;
import com.shrija.domain.model.LeaveRequest;
import com.shrija.domain.model.LeaveType;
import com.shrija.domain.repository.DocumentRequestRepository;
import com.shrija.domain.repository.EmployeeLeaveBalanceRepository;
import com.shrija.domain.repository.EmployeeLifecycleTaskRepository;
import com.shrija.domain.repository.LeaveRequestRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeSelfServiceServiceTest {

  @Mock private EmployeeLeaveBalanceRepository leaveBalanceRepository;
  @Mock private LeaveRequestRepository leaveRequestRepository;
  @Mock private EmployeeLifecycleTaskRepository lifecycleTaskRepository;
  @Mock private DocumentRequestRepository documentRequestRepository;
  @Mock private EmployeeSelfServiceMapper mapper;

  private EmployeeSelfServiceService newService() {
    return new EmployeeSelfServiceService(
        leaveBalanceRepository,
        leaveRequestRepository,
        lifecycleTaskRepository,
        documentRequestRepository,
        mapper);
  }

  @Test
  void applyForLeave_rejectsEndDateBeforeStartDate() {
    EmployeeSelfServiceService service = newService();
    LocalDate start = LocalDate.now().plusDays(5);
    LocalDate end = start.minusDays(1);

    assertThatThrownBy(
            () ->
                service.applyForLeave(
                    "EMP1024", LeaveType.ANNUAL, start.toString(), end.toString()))
        .isInstanceOf(InvalidLeaveRequestException.class)
        .hasMessageContaining("endDate cannot be before startDate");
  }

  @Test
  void applyForLeave_rejectsPastStartDate() {
    EmployeeSelfServiceService service = newService();
    LocalDate start = LocalDate.now().minusDays(1);
    LocalDate end = LocalDate.now().plusDays(1);

    assertThatThrownBy(
            () ->
                service.applyForLeave(
                    "EMP1024", LeaveType.ANNUAL, start.toString(), end.toString()))
        .isInstanceOf(InvalidLeaveRequestException.class)
        .hasMessageContaining("cannot be in the past");
  }

  @Test
  void applyForLeave_rejectsWhenBalanceInsufficient() {
    EmployeeSelfServiceService service = newService();
    LocalDate start = LocalDate.now().plusDays(1);
    LocalDate end = start.plusDays(4); // 5 days requested

    EmployeeLeaveBalance balance =
        new EmployeeLeaveBalance("EMP1024", LeaveType.ANNUAL, 10, 8); // 2 remaining
    when(leaveBalanceRepository.findByEmployeeCodeIgnoreCaseAndLeaveType(
            "EMP1024", LeaveType.ANNUAL))
        .thenReturn(Optional.of(balance));

    assertThatThrownBy(
            () ->
                service.applyForLeave(
                    "EMP1024", LeaveType.ANNUAL, start.toString(), end.toString()))
        .isInstanceOf(InvalidLeaveRequestException.class)
        .hasMessageContaining("only 2 day(s) remain");
  }

  @Test
  void applyForLeave_throwsResourceNotFound_whenNoBalanceRecord() {
    EmployeeSelfServiceService service = newService();
    LocalDate start = LocalDate.now().plusDays(1);
    LocalDate end = start.plusDays(1);

    when(leaveBalanceRepository.findByEmployeeCodeIgnoreCaseAndLeaveType("EMP9999", LeaveType.SICK))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.applyForLeave("EMP9999", LeaveType.SICK, start.toString(), end.toString()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void applyForLeave_succeeds_andDecrementsBalance() {
    EmployeeSelfServiceService service = newService();
    LocalDate start = LocalDate.now().plusDays(1);
    LocalDate end = start.plusDays(1); // 2 days requested

    EmployeeLeaveBalance balance = new EmployeeLeaveBalance("EMP1024", LeaveType.CASUAL, 10, 0);
    when(leaveBalanceRepository.findByEmployeeCodeIgnoreCaseAndLeaveType(
            "EMP1024", LeaveType.CASUAL))
        .thenReturn(Optional.of(balance));
    when(leaveRequestRepository.save(any(LeaveRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    LeaveRequestDto dto =
        new LeaveRequestDto(
            1L, "EMP1024", "CASUAL", start.toString(), end.toString(), "PENDING", null);
    when(mapper.toDto(any(LeaveRequest.class))).thenReturn(dto);

    LeaveRequestDto result =
        service.applyForLeave("EMP1024", LeaveType.CASUAL, start.toString(), end.toString());

    assertThat(result).isEqualTo(dto);
    assertThat(balance.getUsedDays()).isEqualTo(2);
  }
}

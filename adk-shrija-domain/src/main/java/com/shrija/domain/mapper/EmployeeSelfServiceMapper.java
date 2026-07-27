package com.shrija.domain.mapper;

import com.shrija.domain.dto.DocumentRequestDto;
import com.shrija.domain.dto.LeaveBalanceDto;
import com.shrija.domain.dto.LeaveRequestDto;
import com.shrija.domain.dto.LifecycleTaskDto;
import com.shrija.domain.model.DocumentRequest;
import com.shrija.domain.model.EmployeeLeaveBalance;
import com.shrija.domain.model.EmployeeLifecycleTask;
import com.shrija.domain.model.LeaveRequest;
import org.springframework.stereotype.Component;

/**
 * One mapper for all four employee self-service entities, unlike HR's one-class-per-entity
 * precedent - four tiny mapper classes for one cohesive bounded context (leave, lifecycle,
 * documents) added more file overhead than clarity. Revisit if this class grows unwieldy.
 */
@Component
public class EmployeeSelfServiceMapper {

  public LeaveBalanceDto toDto(EmployeeLeaveBalance entity) {
    return new LeaveBalanceDto(
        entity.getEmployeeCode(),
        entity.getLeaveType().name(),
        entity.getTotalDays(),
        entity.getUsedDays(),
        entity.getRemainingDays());
  }

  public LeaveRequestDto toDto(LeaveRequest entity) {
    return new LeaveRequestDto(
        entity.getId(),
        entity.getEmployeeCode(),
        entity.getLeaveType().name(),
        entity.getStartDate().toString(),
        entity.getEndDate().toString(),
        entity.getStatus().name(),
        entity.getRejectionReason());
  }

  public LifecycleTaskDto toDto(EmployeeLifecycleTask entity) {
    return new LifecycleTaskDto(
        entity.getTaskName(),
        entity.getTaskType().name(),
        entity.getStatus().name(),
        entity.getDueDate() == null ? null : entity.getDueDate().toString());
  }

  public DocumentRequestDto toDto(DocumentRequest entity) {
    return new DocumentRequestDto(
        entity.getId(),
        entity.getEmployeeCode(),
        entity.getDocumentType().name(),
        entity.getStatus().name());
  }
}

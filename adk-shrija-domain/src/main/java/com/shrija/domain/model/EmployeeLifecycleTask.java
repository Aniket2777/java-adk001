package com.shrija.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * One row per checklist item in an employee's onboarding or offboarding process (e.g. "Laptop
 * provisioning", "Exit interview"). Rows are expected to be created by whatever process kicks off
 * on-/off-boarding (out of this agent's scope) - the Employee Agent only reads status here.
 */
@Entity
@Table(name = "employee_lifecycle_task")
public class EmployeeLifecycleTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_code", nullable = false, length = 32)
  private String employeeCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "task_type", nullable = false, length = 20)
  private TaskType taskType;

  @Column(name = "task_name", nullable = false, length = 150)
  private String taskName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  @Column(name = "due_date")
  private LocalDate dueDate;

  protected EmployeeLifecycleTask() {}

  public EmployeeLifecycleTask(
      String employeeCode, TaskType taskType, String taskName, Status status, LocalDate dueDate) {
    this.employeeCode = employeeCode;
    this.taskType = taskType;
    this.taskName = taskName;
    this.status = status;
    this.dueDate = dueDate;
  }

  public Long getId() {
    return id;
  }

  public String getEmployeeCode() {
    return employeeCode;
  }

  public TaskType getTaskType() {
    return taskType;
  }

  public String getTaskName() {
    return taskName;
  }

  public Status getStatus() {
    return status;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public enum TaskType {
    ONBOARDING,
    OFFBOARDING
  }

  public enum Status {
    PENDING,
    IN_PROGRESS,
    COMPLETED
  }
}

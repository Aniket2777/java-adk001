package com.shrija.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Persistent record for an employee, as far as the HR domain is concerned.
 *
 * <p>Deliberately scoped to HR's own view of an employee (identity, department, designation,
 * employment status) rather than a shared "god" employee entity used by every agent - Payroll,
 * Budget, etc. will likely need their own domain models later even if they refer to the same
 * person, per Separation of Concerns.
 */
@Entity
@Table(
    name = "hr_employee",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = "employee_code"),
      @UniqueConstraint(columnNames = "email")
    })
public class HrEmployee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_code", nullable = false, length = 32)
  private String employeeCode;

  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @Column(nullable = false, length = 150)
  private String email;

  @Column(nullable = false, length = 100)
  private String department;

  @Column(nullable = false, length = 100)
  private String designation;

  @Enumerated(EnumType.STRING)
  @Column(name = "employment_status", nullable = false, length = 20)
  private EmploymentStatus employmentStatus;

  protected HrEmployee() {
    // required by JPA
  }

  public HrEmployee(
      String employeeCode,
      String fullName,
      String email,
      String department,
      String designation,
      EmploymentStatus employmentStatus) {
    this.employeeCode = employeeCode;
    this.fullName = fullName;
    this.email = email;
    this.department = department;
    this.designation = designation;
    this.employmentStatus = employmentStatus;
  }

  public Long getId() {
    return id;
  }

  public String getEmployeeCode() {
    return employeeCode;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public String getDepartment() {
    return department;
  }

  public String getDesignation() {
    return designation;
  }

  public EmploymentStatus getEmploymentStatus() {
    return employmentStatus;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public void setDesignation(String designation) {
    this.designation = designation;
  }

  public void setEmploymentStatus(EmploymentStatus employmentStatus) {
    this.employmentStatus = employmentStatus;
  }

  /** Deliberately small: expand only when a real workflow needs a new state. */
  public enum EmploymentStatus {
    ACTIVE,
    ON_LEAVE,
    TERMINATED
  }
}

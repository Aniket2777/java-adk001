package com.example.agent.Leaves_agent.entity;

import java.time.LocalDate;

/**
 * Plain data holder for one row of the `employees` table. No JPA/Hibernate here on purpose — the
 * DAO layer talks to JDBC directly, so an ORM entity would just be dead weight. Add one later only
 * if you actually introduce Spring Data / Hibernate.
 */
public class Employee {

  private Long id;
  private String employeeId;
  private String employeeName;
  private String password; // plaintext, per explicit request — see README security note
  private String department;
  private String designation;
  private String role;
  private boolean active;
  private LocalDate joiningDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getEmployeeName() {
    return employeeName;
  }

  public void setEmployeeName(String employeeName) {
    this.employeeName = employeeName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getDesignation() {
    return designation;
  }

  public void setDesignation(String designation) {
    this.designation = designation;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public LocalDate getJoiningDate() {
    return joiningDate;
  }

  public void setJoiningDate(LocalDate joiningDate) {
    this.joiningDate = joiningDate;
  }
}

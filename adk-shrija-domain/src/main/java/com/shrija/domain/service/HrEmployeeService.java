package com.shrija.domain.service;

import com.shrija.domain.dto.HrEmployeeDto;
import com.shrija.domain.exception.EmployeeAlreadyExistsException;
import com.shrija.domain.exception.HrEmployeeNotFoundException;
import com.shrija.domain.mapper.HrEmployeeMapper;
import com.shrija.domain.model.HrEmployee;
import com.shrija.domain.repository.HrEmployeeRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HR domain logic: directory lookup plus employee lifecycle management (add, delete, transfer
 * between departments). This is the layer both a future REST controller and the MCP server's HR
 * tools call into - business rules live here exactly once.
 */
@Service
public class HrEmployeeService {

  private static final Logger log = LoggerFactory.getLogger(HrEmployeeService.class);

  private final HrEmployeeRepository repository;
  private final HrEmployeeMapper mapper;

  public HrEmployeeService(HrEmployeeRepository repository, HrEmployeeMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  public HrEmployeeDto getByEmployeeCode(String employeeCode) {
    log.debug("Looking up employee by code: {}", employeeCode);
    HrEmployee employee =
        repository
            .findByEmployeeCodeIgnoreCase(employeeCode)
            .orElseThrow(() -> new HrEmployeeNotFoundException(employeeCode));
    return mapper.toDto(employee);
  }

  public List<HrEmployeeDto> listByDepartment(String department) {
    log.debug("Listing employees in department: {}", department);
    return repository.findByDepartmentIgnoreCase(department).stream().map(mapper::toDto).toList();
  }

  /**
   * Adds a new employee. Rejects a duplicate employee code or email rather than silently
   * overwriting - HR corrects a typo by deleting and re-adding, not by this method quietly updating
   * an existing row.
   */
  @Transactional
  public HrEmployeeDto addEmployee(
      String employeeCode, String fullName, String email, String department, String designation) {
    if (repository.existsByEmployeeCodeIgnoreCase(employeeCode)
        || repository.existsByEmailIgnoreCase(email)) {
      throw new EmployeeAlreadyExistsException(employeeCode);
    }
    HrEmployee employee =
        new HrEmployee(
            employeeCode,
            fullName,
            email,
            department,
            designation,
            HrEmployee.EmploymentStatus.ACTIVE);
    HrEmployee saved = repository.save(employee);
    log.info("Added employee {} ({}) to department {}", employeeCode, fullName, department);
    return mapper.toDto(saved);
  }

  /**
   * Removes an employee record entirely (hard delete). Enterprise systems more often prefer a soft
   * delete (set employmentStatus to TERMINATED and keep history for audit/reporting) - the master
   * prompt asked for "delete" specifically, so this does a real delete, but flagging the
   * alternative here since it's the more common choice once leave/document history needs to be
   * retained for a departed employee.
   */
  @Transactional
  public void deleteEmployee(String employeeCode) {
    HrEmployee employee =
        repository
            .findByEmployeeCodeIgnoreCase(employeeCode)
            .orElseThrow(() -> new HrEmployeeNotFoundException(employeeCode));
    repository.delete(employee);
    log.info("Deleted employee {}", employeeCode);
  }

  /**
   * Moves an employee to a different department. Designation is left untouched - a transfer is not
   * automatically a promotion/demotion; change designation via a separate action if that's ever
   * needed.
   */
  @Transactional
  public HrEmployeeDto transferEmployee(String employeeCode, String newDepartment) {
    HrEmployee employee =
        repository
            .findByEmployeeCodeIgnoreCase(employeeCode)
            .orElseThrow(() -> new HrEmployeeNotFoundException(employeeCode));
    String previousDepartment = employee.getDepartment();
    employee.setDepartment(newDepartment);
    HrEmployee saved = repository.save(employee);
    log.info(
        "Transferred employee {} from {} to {}", employeeCode, previousDepartment, newDepartment);
    return mapper.toDto(saved);
  }
}

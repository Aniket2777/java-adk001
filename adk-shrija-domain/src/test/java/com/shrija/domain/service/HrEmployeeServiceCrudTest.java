package com.shrija.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shrija.domain.dto.HrEmployeeDto;
import com.shrija.domain.exception.EmployeeAlreadyExistsException;
import com.shrija.domain.exception.HrEmployeeNotFoundException;
import com.shrija.domain.mapper.HrEmployeeMapper;
import com.shrija.domain.model.HrEmployee;
import com.shrija.domain.repository.HrEmployeeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HrEmployeeServiceCrudTest {

  @Mock private HrEmployeeRepository repository;
  @Mock private HrEmployeeMapper mapper;

  private HrEmployeeService newService() {
    return new HrEmployeeService(repository, mapper);
  }

  @Test
  void addEmployee_rejectsDuplicateEmployeeCode() {
    HrEmployeeService service = newService();
    when(repository.existsByEmployeeCodeIgnoreCase("EMP1024")).thenReturn(true);

    assertThatThrownBy(
            () ->
                service.addEmployee(
                    "EMP1024", "New Person", "new@shrija.ai", "Engineering", "Engineer"))
        .isInstanceOf(EmployeeAlreadyExistsException.class);
  }

  @Test
  void addEmployee_savesNewActiveEmployee() {
    HrEmployeeService service = newService();
    when(repository.existsByEmployeeCodeIgnoreCase("EMP4000")).thenReturn(false);
    when(repository.existsByEmailIgnoreCase("new@shrija.ai")).thenReturn(false);
    when(repository.save(any(HrEmployee.class))).thenAnswer(inv -> inv.getArgument(0));
    HrEmployeeDto dto =
        new HrEmployeeDto(
            "EMP4000", "New Person", "new@shrija.ai", "Engineering", "Engineer", "ACTIVE");
    when(mapper.toDto(any(HrEmployee.class))).thenReturn(dto);

    HrEmployeeDto result =
        service.addEmployee("EMP4000", "New Person", "new@shrija.ai", "Engineering", "Engineer");

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void deleteEmployee_throwsWhenNotFound() {
    HrEmployeeService service = newService();
    when(repository.findByEmployeeCodeIgnoreCase("MISSING")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.deleteEmployee("MISSING"))
        .isInstanceOf(HrEmployeeNotFoundException.class);
  }

  @Test
  void deleteEmployee_removesExistingEmployee() {
    HrEmployeeService service = newService();
    HrEmployee employee =
        new HrEmployee(
            "EMP1024",
            "Asha Rao",
            "asha.rao@shrija.ai",
            "Engineering",
            "Senior Engineer",
            HrEmployee.EmploymentStatus.ACTIVE);
    when(repository.findByEmployeeCodeIgnoreCase("EMP1024")).thenReturn(Optional.of(employee));

    service.deleteEmployee("EMP1024");

    verify(repository).delete(employee);
  }

  @Test
  void transferEmployee_updatesDepartment() {
    HrEmployeeService service = newService();
    HrEmployee employee =
        new HrEmployee(
            "EMP1024",
            "Asha Rao",
            "asha.rao@shrija.ai",
            "Engineering",
            "Senior Engineer",
            HrEmployee.EmploymentStatus.ACTIVE);
    when(repository.findByEmployeeCodeIgnoreCase("EMP1024")).thenReturn(Optional.of(employee));
    when(repository.save(employee)).thenReturn(employee);
    HrEmployeeDto dto =
        new HrEmployeeDto(
            "EMP1024", "Asha Rao", "asha.rao@shrija.ai", "Finance", "Senior Engineer", "ACTIVE");
    when(mapper.toDto(employee)).thenReturn(dto);

    HrEmployeeDto result = service.transferEmployee("EMP1024", "Finance");

    assertThat(employee.getDepartment()).isEqualTo("Finance");
    assertThat(result.department()).isEqualTo("Finance");
  }
}

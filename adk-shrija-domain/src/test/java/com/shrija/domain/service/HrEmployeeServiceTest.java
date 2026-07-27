package com.shrija.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.shrija.domain.dto.HrEmployeeDto;
import com.shrija.domain.exception.HrEmployeeNotFoundException;
import com.shrija.domain.mapper.HrEmployeeMapper;
import com.shrija.domain.model.HrEmployee;
import com.shrija.domain.repository.HrEmployeeRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Exercises {@link HrEmployeeService} in isolation - repository and mapper are mocked, per the
 * project's "every agent independently testable" guideline. No Spring context needed for these
 * cases.
 */
@ExtendWith(MockitoExtension.class)
class HrEmployeeServiceTest {

  @Mock private HrEmployeeRepository repository;

  @Mock private HrEmployeeMapper mapper;

  @Test
  void getByEmployeeCode_returnsDto_whenEmployeeExists() {
    HrEmployeeService service = new HrEmployeeService(repository, mapper);
    HrEmployee entity =
        new HrEmployee(
            "EMP1024",
            "Asha Rao",
            "asha.rao@shrija.ai",
            "Engineering",
            "Senior Engineer",
            HrEmployee.EmploymentStatus.ACTIVE);
    HrEmployeeDto dto =
        new HrEmployeeDto(
            "EMP1024",
            "Asha Rao",
            "asha.rao@shrija.ai",
            "Engineering",
            "Senior Engineer",
            "ACTIVE");

    when(repository.findByEmployeeCodeIgnoreCase("EMP1024")).thenReturn(Optional.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    HrEmployeeDto result = service.getByEmployeeCode("EMP1024");

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void getByEmployeeCode_throws_whenEmployeeMissing() {
    HrEmployeeService service = new HrEmployeeService(repository, mapper);
    when(repository.findByEmployeeCodeIgnoreCase("MISSING")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getByEmployeeCode("MISSING"))
        .isInstanceOf(HrEmployeeNotFoundException.class)
        .hasMessageContaining("MISSING");
  }

  @Test
  void listByDepartment_mapsEveryMatchingEntity() {
    HrEmployeeService service = new HrEmployeeService(repository, mapper);
    HrEmployee entity =
        new HrEmployee(
            "EMP2001",
            "Vikram Shah",
            "vikram.shah@shrija.ai",
            "Finance",
            "Analyst",
            HrEmployee.EmploymentStatus.ACTIVE);
    HrEmployeeDto dto =
        new HrEmployeeDto(
            "EMP2001", "Vikram Shah", "vikram.shah@shrija.ai", "Finance", "Analyst", "ACTIVE");

    when(repository.findByDepartmentIgnoreCase("Finance")).thenReturn(List.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    List<HrEmployeeDto> result = service.listByDepartment("Finance");

    assertThat(result).containsExactly(dto);
  }
}

package com.shrija.domain.mapper;

import com.shrija.domain.dto.HrEmployeeDto;
import com.shrija.domain.model.HrEmployee;
import org.springframework.stereotype.Component;

/**
 * Manual mapper, not MapStruct: at one entity/one DTO, generating an annotation processor for this
 * is more machinery than the problem warrants. Worth revisiting if HR (or other agents) end up with
 * several entity/DTO pairs - flagging that now rather than adding the dependency speculatively.
 */
@Component
public class HrEmployeeMapper {

  public HrEmployeeDto toDto(HrEmployee entity) {
    return new HrEmployeeDto(
        entity.getEmployeeCode(),
        entity.getFullName(),
        entity.getEmail(),
        entity.getDepartment(),
        entity.getDesignation(),
        entity.getEmploymentStatus().name());
  }
}

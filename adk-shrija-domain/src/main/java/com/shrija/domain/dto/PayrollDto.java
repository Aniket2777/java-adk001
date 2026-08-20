package com.shrija.domain.dto;
import java.math.BigDecimal;
public record PayrollDto(Long id,Long employeeId,String employeeCode,String payMonth,
                         BigDecimal basicSalary,BigDecimal allowances,BigDecimal deductions,
                         BigDecimal netSalary,String paymentStatus) {}

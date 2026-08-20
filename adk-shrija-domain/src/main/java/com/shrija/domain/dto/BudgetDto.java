package com.shrija.domain.dto;
import java.math.BigDecimal;
public record BudgetDto(Long id,String department,String budgetYear,BigDecimal allocatedAmount,
                        BigDecimal spentAmount,String status) {}

package com.shrija.domain.service;
import com.shrija.domain.dto.BudgetDto;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.model.BudgetRecord;
import com.shrija.domain.repository.BudgetRecordRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
@Service
public class BudgetService {
    private final BudgetRecordRepository repo;
    public BudgetService(BudgetRecordRepository repo){this.repo=repo;}
    public BudgetDto create(String dept,String year,BigDecimal allocated){BudgetRecord b=new BudgetRecord();b.setDepartment(dept);b.setBudgetYear(year);b.setAllocatedAmount(allocated);b.setSpentAmount(BigDecimal.ZERO);return dto(repo.save(b));}
    public List<BudgetDto> byDepartment(String dept){return repo.findByDepartmentIgnoreCase(dept).stream().map(this::dto).toList();}
    public BudgetDto updateSpent(Long id,BigDecimal amount){BudgetRecord b=repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Budget not found: "+id));b.setSpentAmount(amount);return dto(repo.save(b));}
    private BudgetDto dto(BudgetRecord b){return new BudgetDto(b.getId(),b.getDepartment(),b.getBudgetYear(),b.getAllocatedAmount(),b.getSpentAmount(),b.getStatus());}
}

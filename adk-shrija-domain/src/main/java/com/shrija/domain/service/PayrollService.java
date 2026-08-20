package com.shrija.domain.service;
import com.shrija.domain.dto.PayrollDto;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.model.*;
import com.shrija.domain.repository.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
@Service
public class PayrollService {
    private final PayrollRecordRepository repo; private final EmployeeService employees;
    public PayrollService(PayrollRecordRepository repo,EmployeeService employees){this.repo=repo;this.employees=employees;}
    public PayrollDto generate(Long employeeId,String month,BigDecimal basic,BigDecimal allowances,BigDecimal deductions){
        PayrollRecord r=repo.findByEmployeeIdAndPayMonth(employeeId,month).orElseGet(PayrollRecord::new);
        r.setEmployee(employees.getEntity(employeeId)); r.setPayMonth(month); r.setBasicSalary(basic);
        r.setAllowances(allowances==null?BigDecimal.ZERO:allowances); r.setDeductions(deductions==null?BigDecimal.ZERO:deductions);
        r.setNetSalary(r.getBasicSalary().add(r.getAllowances()).subtract(r.getDeductions())); r.setPaymentStatus("GENERATED");
        return dto(repo.save(r));
    }
    public List<PayrollDto> byEmployee(Long id){return repo.findByEmployeeIdOrderByPayMonthDesc(id).stream().map(this::dto).toList();}
    public PayrollDto get(Long id){return dto(repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Payroll record not found: "+id)));}
    private PayrollDto dto(PayrollRecord r){Employee e=r.getEmployee();return new PayrollDto(r.getId(),e.getId(),e.getEmployeeCode(),r.getPayMonth(),r.getBasicSalary(),r.getAllowances(),r.getDeductions(),r.getNetSalary(),r.getPaymentStatus());}
}

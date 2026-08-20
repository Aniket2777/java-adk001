package com.shrija.domain.service;
import com.shrija.domain.dto.LeaveRequestDto;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.model.*;
import com.shrija.domain.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
@Service
public class LeaveService {
    private final LeaveRequestRepository repo; private final EmployeeService employees;
    public LeaveService(LeaveRequestRepository repo,EmployeeService employees){this.repo=repo;this.employees=employees;}
    public LeaveRequestDto create(Long employeeId,String type,String from,String to,String reason){
        LocalDate f=LocalDate.parse(from), t=LocalDate.parse(to);
        if(t.isBefore(f)) throw new IllegalArgumentException("toDate cannot be before fromDate");
        LeaveRequest r=new LeaveRequest(); r.setEmployee(employees.getEntity(employeeId)); r.setLeaveType(type);
        r.setFromDate(f); r.setToDate(t); r.setReason(reason); r.setStatus("PENDING");
        return dto(repo.save(r));
    }
    public List<LeaveRequestDto> byEmployee(Long id){return repo.findByEmployeeIdOrderByFromDateDesc(id).stream().map(this::dto).toList();}
    public List<LeaveRequestDto> pending(){return repo.findByStatusIgnoreCase("PENDING").stream().map(this::dto).toList();}
    public LeaveRequestDto approve(Long id,String approver,boolean approve){
        LeaveRequest r=repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Leave request not found: "+id));
        if(!"PENDING".equalsIgnoreCase(r.getStatus())) throw new IllegalStateException("Leave request is not pending");
        r.setStatus(approve?"APPROVED":"REJECTED"); r.setApprovedBy(approver); return dto(repo.save(r));
    }
    private LeaveRequestDto dto(LeaveRequest r){Employee e=r.getEmployee();return new LeaveRequestDto(r.getId(),e.getId(),e.getEmployeeCode(),r.getLeaveType(),String.valueOf(r.getFromDate()),String.valueOf(r.getToDate()),r.getReason(),r.getStatus(),r.getApprovedBy());}
}

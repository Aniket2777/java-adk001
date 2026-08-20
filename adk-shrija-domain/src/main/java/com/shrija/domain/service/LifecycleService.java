package com.shrija.domain.service;
import com.shrija.domain.dto.LifecycleTaskDto;
import com.shrija.domain.model.*;
import com.shrija.domain.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
@Service
public class LifecycleService {
    private final LifecycleTaskRepository repo; private final EmployeeService employees;
    public LifecycleService(LifecycleTaskRepository repo,EmployeeService employees){this.repo=repo;this.employees=employees;}
    public LifecycleTaskDto create(Long employeeId,String type,String dueDate){LifecycleTask t=new LifecycleTask();t.setEmployee(employees.getEntity(employeeId));t.setTaskType(type);if(dueDate!=null&&!dueDate.isBlank())t.setDueDate(LocalDateTime.parse(dueDate));return dto(repo.save(t));}
    public List<LifecycleTaskDto> byEmployee(Long id){return repo.findByEmployeeId(id).stream().map(this::dto).toList();}
    public LifecycleTaskDto updateStatus(Long id,String status){LifecycleTask t=repo.findById(id).orElseThrow();t.setStatus(status);return dto(repo.save(t));}
    private LifecycleTaskDto dto(LifecycleTask t){Employee e=t.getEmployee();return new LifecycleTaskDto(t.getId(),e.getId(),e.getEmployeeCode(),t.getTaskType(),t.getStatus(),String.valueOf(t.getDueDate()));}
}

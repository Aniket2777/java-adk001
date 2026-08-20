package com.shrija.domain.service;

import com.shrija.domain.dto.EmployeeDto;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.model.Employee;
import com.shrija.domain.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class EmployeeService {
    private final EmployeeRepository repo;
    public EmployeeService(EmployeeRepository repo){this.repo=repo;}

    public EmployeeDto create(String code,String firstName,String lastName,String email,String department,String designation){
        if(repo.findByEmployeeCode(code).isPresent()) throw new IllegalArgumentException("Employee code already exists");
        if(repo.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email already exists");
        Employee e=new Employee(code,firstName,lastName,email);
        e.setDepartment(department); e.setDesignation(designation); e.setJoiningDate(LocalDate.now());
        return toDto(repo.save(e));
    }
    public EmployeeDto get(Long id){return toDto(repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Employee not found: "+id)));}
    public EmployeeDto getByCode(String code){return toDto(repo.findByEmployeeCode(code).orElseThrow(()->new ResourceNotFoundException("Employee not found: "+code)));}
    public List<EmployeeDto> byDepartment(String department){return repo.findByDepartmentIgnoreCase(department).stream().map(this::toDto).toList();}
    public List<EmployeeDto> all(){return repo.findAll().stream().map(this::toDto).toList();}
    public Employee getEntity(Long id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Employee not found: "+id));}
    private EmployeeDto toDto(Employee e){return new EmployeeDto(e.getId(),e.getEmployeeCode(),e.getFirstName(),e.getLastName(),e.getEmail(),e.getPhone(),e.getDepartment(),e.getDesignation(),e.getManagerName(),String.valueOf(e.getJoiningDate()),e.getEmploymentStatus());}
}

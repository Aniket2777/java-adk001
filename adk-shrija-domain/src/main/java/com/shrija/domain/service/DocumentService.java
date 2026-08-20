package com.shrija.domain.service;
import com.shrija.domain.dto.DocumentDto;
import com.shrija.domain.model.*;
import com.shrija.domain.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class DocumentService {
    private final DocumentRequestRepository repo; private final EmployeeService employees;
    public DocumentService(DocumentRequestRepository repo,EmployeeService employees){this.repo=repo;this.employees=employees;}
    public DocumentDto create(Long employeeId,String type,String description){DocumentRequest d=new DocumentRequest();d.setEmployee(employees.getEntity(employeeId));d.setDocumentType(type);d.setDescription(description);return dto(repo.save(d));}
    public List<DocumentDto> byEmployee(Long id){return repo.findByEmployeeIdOrderByRequestedAtDesc(id).stream().map(this::dto).toList();}
    public DocumentDto updateStatus(Long id,String status){DocumentRequest d=repo.findById(id).orElseThrow();d.setStatus(status);return dto(repo.save(d));}
    private DocumentDto dto(DocumentRequest d){Employee e=d.getEmployee();return new DocumentDto(d.getId(),e.getId(),e.getEmployeeCode(),d.getDocumentType(),d.getDescription(),d.getStatus(),String.valueOf(d.getRequestedAt()));}
}

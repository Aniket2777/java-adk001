package com.shrija.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="lifecycle_tasks")
public class LifecycleTask {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="employee_id")
    private Employee employee;
    private String taskType;
    private String status = "OPEN";
    private LocalDateTime dueDate;

    public LifecycleTask(){}
    public Long getId(){return id;} public Employee getEmployee(){return employee;}
    public String getTaskType(){return taskType;} public String getStatus(){return status;}
    public LocalDateTime getDueDate(){return dueDate;}
    public void setId(Long v){id=v;} public void setEmployee(Employee v){employee=v;}
    public void setTaskType(String v){taskType=v;} public void setStatus(String v){status=v;}
    public void setDueDate(LocalDateTime v){dueDate=v;}
}

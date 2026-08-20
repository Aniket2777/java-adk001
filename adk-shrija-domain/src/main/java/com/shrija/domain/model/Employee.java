package com.shrija.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true)
    private String employeeCode;
    @Column(nullable=false)
    private String firstName;
    @Column(nullable=false)
    private String lastName;
    @Column(nullable=false, unique=true)
    private String email;
    private String phone;
    private String department;
    private String designation;
    private String managerName;
    private LocalDate joiningDate;
    private String employmentStatus = "ACTIVE";

    public Employee() {}
    public Employee(String employeeCode, String firstName, String lastName, String email) {
        this.employeeCode=employeeCode; this.firstName=firstName; this.lastName=lastName; this.email=email;
    }
    public Long getId(){return id;} public String getEmployeeCode(){return employeeCode;}
    public String getFirstName(){return firstName;} public String getLastName(){return lastName;}
    public String getEmail(){return email;} public String getPhone(){return phone;}
    public String getDepartment(){return department;} public String getDesignation(){return designation;}
    public String getManagerName(){return managerName;} public LocalDate getJoiningDate(){return joiningDate;}
    public String getEmploymentStatus(){return employmentStatus;}
    public void setId(Long v){id=v;} public void setEmployeeCode(String v){employeeCode=v;}
    public void setFirstName(String v){firstName=v;} public void setLastName(String v){lastName=v;}
    public void setEmail(String v){email=v;} public void setPhone(String v){phone=v;}
    public void setDepartment(String v){department=v;} public void setDesignation(String v){designation=v;}
    public void setManagerName(String v){managerName=v;} public void setJoiningDate(LocalDate v){joiningDate=v;}
    public void setEmploymentStatus(String v){employmentStatus=v;}
}

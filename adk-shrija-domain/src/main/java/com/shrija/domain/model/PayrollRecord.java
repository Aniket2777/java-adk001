package com.shrija.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="payroll_records")
public class PayrollRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="employee_id")
    private Employee employee;
    @Column(nullable=false) private String payMonth;
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private String paymentStatus = "PENDING";

    public PayrollRecord(){}
    public Long getId(){return id;} public Employee getEmployee(){return employee;}
    public String getPayMonth(){return payMonth;} public BigDecimal getBasicSalary(){return basicSalary;}
    public BigDecimal getAllowances(){return allowances;} public BigDecimal getDeductions(){return deductions;}
    public BigDecimal getNetSalary(){return netSalary;} public String getPaymentStatus(){return paymentStatus;}
    public void setId(Long v){id=v;} public void setEmployee(Employee v){employee=v;}
    public void setPayMonth(String v){payMonth=v;} public void setBasicSalary(BigDecimal v){basicSalary=v;}
    public void setAllowances(BigDecimal v){allowances=v;} public void setDeductions(BigDecimal v){deductions=v;}
    public void setNetSalary(BigDecimal v){netSalary=v;} public void setPaymentStatus(String v){paymentStatus=v;}
}

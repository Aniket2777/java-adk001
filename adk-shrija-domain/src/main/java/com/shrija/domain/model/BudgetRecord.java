package com.shrija.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="budget_records")
public class BudgetRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false) private String department;
    @Column(nullable=false) private String budgetYear;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;
    private String status = "ACTIVE";

    public BudgetRecord(){}
    public Long getId(){return id;} public String getDepartment(){return department;}
    public String getBudgetYear(){return budgetYear;} public BigDecimal getAllocatedAmount(){return allocatedAmount;}
    public BigDecimal getSpentAmount(){return spentAmount;} public String getStatus(){return status;}
    public void setId(Long v){id=v;} public void setDepartment(String v){department=v;}
    public void setBudgetYear(String v){budgetYear=v;} public void setAllocatedAmount(BigDecimal v){allocatedAmount=v;}
    public void setSpentAmount(BigDecimal v){spentAmount=v;} public void setStatus(String v){status=v;}
}

package com.shrija.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="attendance_records", uniqueConstraints=@UniqueConstraint(columnNames={"employee_id","attendance_date"}))
public class AttendanceRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="employee_id")
    private Employee employee;
    @Column(name="attendance_date", nullable=false) private LocalDate attendanceDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private Double workingHours;

    public AttendanceRecord(){}
    public Long getId(){return id;} public Employee getEmployee(){return employee;}
    public LocalDate getAttendanceDate(){return attendanceDate;} public LocalDateTime getCheckIn(){return checkIn;}
    public LocalDateTime getCheckOut(){return checkOut;} public String getStatus(){return status;}
    public Double getWorkingHours(){return workingHours;}
    public void setId(Long v){id=v;} public void setEmployee(Employee v){employee=v;}
    public void setAttendanceDate(LocalDate v){attendanceDate=v;} public void setCheckIn(LocalDateTime v){checkIn=v;}
    public void setCheckOut(LocalDateTime v){checkOut=v;} public void setStatus(String v){status=v;}
    public void setWorkingHours(Double v){workingHours=v;}
}

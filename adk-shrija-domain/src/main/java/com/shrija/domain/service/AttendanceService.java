package com.shrija.domain.service;
import com.shrija.domain.dto.AttendanceDto;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.model.*;
import com.shrija.domain.repository.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;
@Service
public class AttendanceService {
    private final AttendanceRecordRepository repo; private final EmployeeService employees;
    public AttendanceService(AttendanceRecordRepository repo,EmployeeService employees){this.repo=repo;this.employees=employees;}
    public AttendanceDto mark(Long employeeId,String date,String status,String checkIn,String checkOut){
        LocalDate d=LocalDate.parse(date);
        AttendanceRecord r=repo.findByEmployeeIdAndAttendanceDate(employeeId,d).orElseGet(AttendanceRecord::new);
        r.setEmployee(employees.getEntity(employeeId)); r.setAttendanceDate(d); r.setStatus(status);
        if(checkIn!=null&&!checkIn.isBlank()) r.setCheckIn(LocalDateTime.parse(checkIn));
        if(checkOut!=null&&!checkOut.isBlank()) r.setCheckOut(LocalDateTime.parse(checkOut));
        if(r.getCheckIn()!=null&&r.getCheckOut()!=null) r.setWorkingHours(Duration.between(r.getCheckIn(),r.getCheckOut()).toMinutes()/60.0);
        return dto(repo.save(r));
    }
    public List<AttendanceDto> range(Long employeeId,String from,String to){return repo.findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(employeeId,LocalDate.parse(from),LocalDate.parse(to)).stream().map(this::dto).toList();}
    private AttendanceDto dto(AttendanceRecord r){Employee e=r.getEmployee();return new AttendanceDto(r.getId(),e.getId(),e.getEmployeeCode(),String.valueOf(r.getAttendanceDate()),String.valueOf(r.getCheckIn()),String.valueOf(r.getCheckOut()),r.getStatus(),r.getWorkingHours());}
}

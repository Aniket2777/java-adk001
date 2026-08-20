package com.shrija.domain.dto;
public record AttendanceDto(Long id,Long employeeId,String employeeCode,String attendanceDate,
                            String checkIn,String checkOut,String status,Double workingHours) {}

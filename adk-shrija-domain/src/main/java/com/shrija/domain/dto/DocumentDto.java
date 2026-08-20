package com.shrija.domain.dto;
public record DocumentDto(Long id,Long employeeId,String employeeCode,String documentType,
                          String description,String status,String requestedAt) {}

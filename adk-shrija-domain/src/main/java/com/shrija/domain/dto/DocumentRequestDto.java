package com.shrija.domain.dto;

public record DocumentRequestDto(
    Long id, String employeeCode, String documentType, String status) {}

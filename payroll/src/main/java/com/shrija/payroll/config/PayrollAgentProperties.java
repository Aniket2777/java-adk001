package com.shrija.payroll.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "payroll.agent")
@Validated
public record PayrollAgentProperties(
    @NotBlank String geminiApiKey, @NotBlank String geminiModel, @NotBlank String mcpServerUrl) {}

package com.ai.shrija.leave.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Leave Agent service.
 *
 * This service exposes an ADK-style agent (see agent/LeaveAgent.java) that can
 * apply for leave, cancel leave, check leave balance, and route approvals,
 * either directly via REST (controller/LeaveController.java) or via the A2A
 * protocol (config/A2AConfig.java) so other agents (Manager, Payroll, Employee)
 * can collaborate with it.
 */
@SpringBootApplication
public class LeaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeaveApplication.class, args);
    }
}

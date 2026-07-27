package com.shrija.mcpserver.tools;

import com.shrija.domain.service.EmployeeSelfServiceService;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class LifecycleMcpTools {

  private final EmployeeSelfServiceService employeeSelfServiceService;

  public LifecycleMcpTools(EmployeeSelfServiceService employeeSelfServiceService) {
    this.employeeSelfServiceService = employeeSelfServiceService;
  }

  @Tool(description = "Check the status of an employee's onboarding/offboarding checklist tasks")
  public Map<String, Object> checkOnboardingOffboardingStatus(
      @ToolParam(description = "The employee's unique code, e.g. EMP1024") String employeeCode) {
    var tasks = employeeSelfServiceService.getLifecycleStatus(employeeCode);
    return Map.of("employeeCode", employeeCode, "taskCount", tasks.size(), "tasks", tasks);
  }
}

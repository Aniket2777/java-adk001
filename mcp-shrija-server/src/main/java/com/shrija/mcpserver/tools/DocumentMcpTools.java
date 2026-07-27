package com.shrija.mcpserver.tools;

import com.shrija.domain.dto.DocumentRequestDto;
import com.shrija.domain.exception.ShrijaAiException;
import com.shrija.domain.model.DocumentType;
import com.shrija.domain.service.DocumentFulfillmentService;
import com.shrija.domain.service.EmployeeSelfServiceService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tools for document requests: employee-side (request, check status) via {@link
 * EmployeeSelfServiceService}, HR-side fulfillment via {@link DocumentFulfillmentService}. Same
 * split-service/one-tool-class pattern as {@code LeaveMcpTools}.
 */
@Component
public class DocumentMcpTools {

  private static final Logger log = LoggerFactory.getLogger(DocumentMcpTools.class);

  private final EmployeeSelfServiceService employeeSelfServiceService;
  private final DocumentFulfillmentService documentFulfillmentService;

  public DocumentMcpTools(
      EmployeeSelfServiceService employeeSelfServiceService,
      DocumentFulfillmentService documentFulfillmentService) {
    this.employeeSelfServiceService = employeeSelfServiceService;
    this.documentFulfillmentService = documentFulfillmentService;
  }

  @Tool(
      description =
          "Request a document (offer letter, joining letter, experience letter, "
              + "salary certificate, or ID proof). Always creates a REQUESTED request - fulfillment is separate.")
  public Map<String, Object> requestDocument(
      @ToolParam(description = "The employee's unique code, e.g. EMP1024") String employeeCode,
      @ToolParam(description = "Which document to request") DocumentType documentType) {
    DocumentRequestDto request =
        employeeSelfServiceService.requestDocument(employeeCode, documentType);
    return Map.of("success", true, "request", request);
  }

  @Tool(description = "Check the status of an employee's document requests, most recent first")
  public Map<String, Object> checkDocumentRequestStatus(
      @ToolParam(description = "The employee's unique code, e.g. EMP1024") String employeeCode) {
    var requests = employeeSelfServiceService.getDocumentRequestStatus(employeeCode);
    return Map.of("employeeCode", employeeCode, "count", requests.size(), "requests", requests);
  }

  @Tool(description = "List every REQUESTED document request, oldest first - for HR review")
  public Map<String, Object> listPendingDocumentRequests() {
    var pending = documentFulfillmentService.listPendingDocumentRequests();
    return Map.of("count", pending.size(), "requests", pending);
  }

  @Tool(description = "Mark a document request READY once it has been prepared")
  public Map<String, Object> markDocumentReady(
      @ToolParam(description = "The document request's id, from listPendingDocumentRequests")
          long requestId) {
    try {
      DocumentRequestDto updated = documentFulfillmentService.markDocumentReady(requestId);
      return Map.of("success", true, "request", updated);
    } catch (ShrijaAiException ex) {
      log.debug("markDocumentReady failed for {}: {}", requestId, ex.getMessage());
      return Map.of("success", false, "message", ex.getMessage());
    }
  }

  @Tool(description = "Mark a READY document request as DELIVERED once handed over")
  public Map<String, Object> markDocumentDelivered(
      @ToolParam(description = "The document request's id, from listPendingDocumentRequests")
          long requestId) {
    try {
      DocumentRequestDto updated = documentFulfillmentService.markDocumentDelivered(requestId);
      return Map.of("success", true, "request", updated);
    } catch (ShrijaAiException ex) {
      log.debug("markDocumentDelivered failed for {}: {}", requestId, ex.getMessage());
      return Map.of("success", false, "message", ex.getMessage());
    }
  }
}

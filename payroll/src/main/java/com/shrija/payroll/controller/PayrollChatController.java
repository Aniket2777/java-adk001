package com.shrija.payroll.controller;

import com.shrija.payroll.auth.AuthenticatedUser;
import com.shrija.payroll.dto.ChatRequest;
import com.shrija.payroll.dto.ChatResponse;
import com.shrija.payroll.service.PayrollConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollChatController {
  private final PayrollConversationService conversationService;

  public PayrollChatController(PayrollConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping("/chat")
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    AuthenticatedUser user =
        new AuthenticatedUser(request.userId(), request.role(), request.employeeCode());
    var result = conversationService.converse(user, request.sessionId(), request.message());
    return ResponseEntity.ok(new ChatResponse(result.sessionId(), result.responseText()));
  }
}

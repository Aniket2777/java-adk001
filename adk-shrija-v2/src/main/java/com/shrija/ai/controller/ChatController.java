package com.shrija.ai.controller;

import com.shrija.ai.auth.AuthenticatedUser;
import com.shrija.ai.dto.ChatRequest;
import com.shrija.ai.dto.ChatResponse;
import com.shrija.ai.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
  private final ConversationService conversationService;

  public ChatController(ConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping
  public ResponseEntity<ChatResponse> chat(
      @AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ChatRequest request) {
    var result = conversationService.converse(user, request.sessionId(), request.message());
    return ResponseEntity.ok(new ChatResponse(result.sessionId(), result.responseText()));
  }
}

package com.shrija.ai.controller;

import com.shrija.ai.dto.ChatRequest;
import com.shrija.ai.dto.ChatResponse;
import com.shrija.ai.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point for talking to Shrija AI. One endpoint, one job: hand the message to the Manager
 * Agent and return its response. Department routing, delegation, and aggregation all happen inside
 * the agent layer - this controller stays a thin adapter by design, per the project's
 * layered-architecture / separation-of-concerns principles.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

  private final ConversationService conversationService;

  public ChatController(ConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @PostMapping
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    ConversationService.ConversationResult result =
        conversationService.converse(request.userId(), request.sessionId(), request.message());

    return ResponseEntity.ok(new ChatResponse(result.sessionId(), result.responseText()));
  }
}

package com.shrija.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.shrija.domain.dto.DocumentRequestDto;
import com.shrija.domain.exception.DocumentRequestNotActionableException;
import com.shrija.domain.exception.ResourceNotFoundException;
import com.shrija.domain.mapper.EmployeeSelfServiceMapper;
import com.shrija.domain.model.DocumentRequest;
import com.shrija.domain.model.DocumentType;
import com.shrija.domain.repository.DocumentRequestRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentFulfillmentServiceTest {

  @Mock private DocumentRequestRepository documentRequestRepository;
  @Mock private EmployeeSelfServiceMapper mapper;

  private DocumentFulfillmentService newService() {
    return new DocumentFulfillmentService(documentRequestRepository, mapper);
  }

  @Test
  void markDocumentReady_succeeds_fromRequested() {
    DocumentFulfillmentService service = newService();
    DocumentRequest request =
        new DocumentRequest(
            "EMP1024", DocumentType.OFFER_LETTER, DocumentRequest.Status.REQUESTED, Instant.now());
    when(documentRequestRepository.findById(1L)).thenReturn(Optional.of(request));
    when(documentRequestRepository.save(request)).thenReturn(request);
    DocumentRequestDto dto = new DocumentRequestDto(1L, "EMP1024", "OFFER_LETTER", "READY");
    when(mapper.toDto(request)).thenReturn(dto);

    DocumentRequestDto result = service.markDocumentReady(1L);

    assertThat(result.status()).isEqualTo("READY");
    assertThat(request.getStatus()).isEqualTo(DocumentRequest.Status.READY);
  }

  @Test
  void markDocumentReady_rejectsWhenAlreadyReady() {
    DocumentFulfillmentService service = newService();
    DocumentRequest request =
        new DocumentRequest(
            "EMP1024", DocumentType.JOINING_LETTER, DocumentRequest.Status.READY, Instant.now());
    when(documentRequestRepository.findById(1L)).thenReturn(Optional.of(request));

    assertThatThrownBy(() -> service.markDocumentReady(1L))
        .isInstanceOf(DocumentRequestNotActionableException.class);
  }

  @Test
  void markDocumentDelivered_requiresReadyFirst() {
    DocumentFulfillmentService service = newService();
    DocumentRequest request =
        new DocumentRequest(
            "EMP1024",
            DocumentType.JOINING_LETTER,
            DocumentRequest.Status.REQUESTED,
            Instant.now());
    when(documentRequestRepository.findById(1L)).thenReturn(Optional.of(request));

    assertThatThrownBy(() -> service.markDocumentDelivered(1L))
        .isInstanceOf(DocumentRequestNotActionableException.class);
  }

  @Test
  void actingOnUnknownRequest_throwsNotFound() {
    DocumentFulfillmentService service = newService();
    when(documentRequestRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.markDocumentReady(99L))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}

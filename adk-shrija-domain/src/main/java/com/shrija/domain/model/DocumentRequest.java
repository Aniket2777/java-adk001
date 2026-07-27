package com.shrija.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "document_request")
public class DocumentRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "employee_code", nullable = false, length = 32)
  private String employeeCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "document_type", nullable = false, length = 30)
  private DocumentType documentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  protected DocumentRequest() {}

  public DocumentRequest(
      String employeeCode, DocumentType documentType, Status status, Instant requestedAt) {
    this.employeeCode = employeeCode;
    this.documentType = documentType;
    this.status = status;
    this.requestedAt = requestedAt;
  }

  public Long getId() {
    return id;
  }

  public String getEmployeeCode() {
    return employeeCode;
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  public Status getStatus() {
    return status;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  /**
   * Same reasoning as {@code LeaveRequest.markApproved}/{@code markRejected}: this only performs
   * the mutation. Whether the transition is valid from the current status is {@code
   * DocumentFulfillmentService}'s business rule to check and reject with {@code
   * DocumentRequestNotActionableException}.
   */
  public void markReady() {
    this.status = Status.READY;
  }

  public void markDelivered() {
    this.status = Status.DELIVERED;
  }

  public enum Status {
    REQUESTED,
    IN_PROGRESS,
    READY,
    DELIVERED
  }
}

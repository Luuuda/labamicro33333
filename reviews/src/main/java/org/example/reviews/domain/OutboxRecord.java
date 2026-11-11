package org.example.reviews.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox") // Ось вона - наша таблиця!
public class OutboxRecord {

    @Id
    private UUID id;
    private String eventType; // Тип події, наприклад: "REVIEW_CREATED"
    private String payload;   // Самі дані (відгук) у форматі JSON
    private String status;    // "PENDING" (чекає відправки) або "SENT" (відправлено)
    private LocalDateTime createdAt;

    protected OutboxRecord() {}

    public OutboxRecord(String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.payload = payload;
        this.status = "PENDING"; // Відразу при створенні статус "в очікуванні"
        this.createdAt = LocalDateTime.now();
    }

    // Геттери та сеттери
    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
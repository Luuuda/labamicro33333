package org.example.reviews.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxRecord, UUID> {
    // Цей метод потрібен нашому майбутньому "Поштарю", щоб знаходити невідправлені події
    List<OutboxRecord> findAllByStatus(String status);
}
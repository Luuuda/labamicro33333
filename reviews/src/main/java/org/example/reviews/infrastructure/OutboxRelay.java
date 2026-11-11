package org.example.reviews.infrastructure;

import org.example.reviews.domain.OutboxRecord;
import org.example.reviews.domain.OutboxRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxRelay(OutboxRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        List<OutboxRecord> pendingRecords = outboxRepository.findAllByStatus("PENDING");

        for (OutboxRecord record : pendingRecords) {
            try {
                rabbitTemplate.convertAndSend("review.events.queue", record.getPayload());

                record.setStatus("SENT");
                outboxRepository.save(record);

                System.out.println("[Поштар] Відправлено подію в RabbitMQ: " + record.getId());
            } catch (Exception e) {
                System.err.println("[Поштар] Помилка відправки. RabbitMQ лежить? " + e.getMessage());
            }
        }
    }
}
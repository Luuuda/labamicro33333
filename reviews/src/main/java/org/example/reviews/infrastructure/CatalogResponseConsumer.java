package org.example.reviews.infrastructure; // або твій пакет

import org.example.reviews.domain.Review;
import org.example.reviews.domain.ReviewRepository;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CatalogResponseConsumer {

    private final ReviewRepository reviewRepository;

    public CatalogResponseConsumer(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Слухаємо нову чергу з відповідями від Каталогу
    @RabbitListener(queuesToDeclare = @Queue(name = "catalog.events.queue", durable = "true"))
    public void handleCatalogResponse(String message) {
        System.out.println("📩 [ВІДГУКИ] Отримано відповідь від Каталогу: " + message);

        String reviewIdStr = extractValue(message, "reviewId");
        String status = extractValue(message, "status");

        // Знаходимо наш відгук в базі
        reviewRepository.findById(UUID.fromString(reviewIdStr)).ifPresent(review -> {
            // Змінюємо статус (ЦЕ І Є КОМПЕНСАЦІЯ АБО ПІДТВЕРДЖЕННЯ)
            review.setStatus(status);
            reviewRepository.save(review);

            if ("REJECTED".equals(status)) {
                System.out.println("⏪ [ВІДГУКИ] КОМПЕНСАЦІЯ: Відгук " + reviewIdStr + " відхилено!");
            } else {
                System.out.println("✅ [ВІДГУКИ] Відгук " + reviewIdStr + " успішно підтверджено!");
            }
        });
    }

    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey) + searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        return json.substring(startIndex, endIndex);
    }
}
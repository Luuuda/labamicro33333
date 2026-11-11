package org.example.catalog.infrastructure;

import org.example.catalog.application.RestaurantService;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewConsumer {

    private final RestaurantService restaurantService;
    private final RabbitTemplate rabbitTemplate;

    public ReviewConsumer(RestaurantService restaurantService, RabbitTemplate rabbitTemplate) {
        this.restaurantService = restaurantService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "review.events.queue", durable = "true"))
    public void handleReviewCreated(String message) {
        System.out.println("📩 [КАТАЛОГ] Обробка повідомлення: " + message);

        // Для простоти парсимо JSON вручну (в реальному проекті використовують ObjectMapper)
        String reviewId = extractValue(message, "reviewId");
        String restaurantIdStr = extractValue(message, "restaurantId");

        try {
            // Спробуємо знайти ресторан
            restaurantService.getRestaurantById(UUID.fromString(restaurantIdStr));

            // Якщо помилки немає - ресторан існує, ПІДТВЕРДЖУЄМО відгук
            String reply = String.format("{\"reviewId\":\"%s\", \"status\":\"APPROVED\"}", reviewId);
            rabbitTemplate.convertAndSend("catalog.events.queue", reply);
            System.out.println(" Ресторан знайдено. Відправлено APPROVED.");

        } catch (Exception e) {
            // Якщо ресторан не знайдено (або інша помилка) - ВІДХИЛЯЄМО відгук
            String reply = String.format("{\"reviewId\":\"%s\", \"status\":\"REJECTED\"}", reviewId);
            rabbitTemplate.convertAndSend("catalog.events.queue", reply);
            System.out.println(" Помилка або ресторан не знайдено. Відправлено REJECTED.");
        }
    }

    // Допоміжний метод для витягування даних з JSON рядка
    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey) + searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        return json.substring(startIndex, endIndex);
    }
}
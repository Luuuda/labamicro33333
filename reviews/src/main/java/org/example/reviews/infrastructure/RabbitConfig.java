package org.example.reviews.infrastructure;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Створюємо чергу з назвою "review.events.queue"
    @Bean
    public Queue reviewEventsQueue() {
        return new Queue("review.events.queue", true); // true означає, що черга надійна
    }
}